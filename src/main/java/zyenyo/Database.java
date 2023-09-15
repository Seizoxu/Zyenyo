package zyenyo;

import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.ascending;

import java.io.ObjectStreamClass;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;

import dataStructures.AchievementDetails;
import dataStructures.AddTestResult;
import dataStructures.LeaderboardConfig;
import dataStructures.RefreshUserNamesResult;
import dataStructures.TypingSubmission;
import dataStructures.streakStatus;
import dataStructures.streakStatusResult;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


class CommandMonitor implements CommandListener {
	@Override
	public void commandFailed(final CommandFailedEvent event) {
		System.out.println(String.format("Failed execution of command '%s' cause: %s",
				event.getCommandName(),
				event.getThrowable()));
	}
}


public class Database
{
	private static String DB_NAME;
	private static MongoClient client;
	private static MongoCollection<Document> tests;
	private static MongoCollection<Document> testsV2;
	private static MongoCollection<Document> usersV2;
	private static MongoCollection<Document> users;
	private static MongoCollection<Document> prompts;
	private static MongoCollection<Document> achievements;
	
	private static UpdateOptions upsertTrue = new UpdateOptions().upsert(true);

	/**
	 * Connects to a mongo database, sets up the necessary indexes and generates a MongoClient.
	 * @param uri : the connection URI for the database.
	 * @param ENVIRONMENT : The environment that this is running. This should be "development" for dev bots.
	 */
	public static void connect(String uri, String ENVIRONMENT)
	{

                DB_NAME = ENVIRONMENT.equals("development") ? "ZyenyoStaging" : "MyDatabase";

		MongoClientSettings settings =
				MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(uri))
				.addCommandListener(new CommandMonitor())
				.build();

		client = MongoClients.create(settings);

		tests = client.getDatabase(DB_NAME).getCollection("tests");
		testsV2 = client.getDatabase(DB_NAME).getCollection("testsv2");

		users = client.getDatabase(DB_NAME).getCollection("users");
		usersV2 = client.getDatabase(DB_NAME).getCollection("usersv2");

		prompts = client.getDatabase(DB_NAME).getCollection("prompts");
		achievements = client.getDatabase(DB_NAME).getCollection("achievements");
			
		// Indexes for common statistics in order to speed up leaderboard commands
		testsV2.createIndex(Indexes.descending("tp"));
		testsV2.createIndex(Indexes.descending("wpm"));
		testsV2.createIndex(Indexes.descending("accuracy"));
		testsV2.createIndex(Indexes.descending("date"));

		usersV2.createIndex(Indexes.descending("totalTp"));
		usersV2.createIndex(Indexes.descending("playtime"));

	}

	/**
	 * Adds a typing submission to the database. Includes daily streak checks and user updates.
	 * @param submission : {@link TypingSubmission}
	 * @return {@link AddTestResult}
	 */
	public static AddTestResult addTestV2(TypingSubmission submission) {
		double initialWeightedTp = getWeightedTp(submission.userID());
		ArrayList<Bson> userUpdates = new ArrayList<Bson>();

		InsertOneResult result = testsV2.insertOne(new Document()
				.append("_id", new ObjectId())
				.append("discordId", String.valueOf(submission.userID()))
				.append("wpm", submission.wordsPerMinute())
				.append("accuracy", submission.accuracy())
				.append("tp", submission.typingPoints())
				.append("timeTaken", submission.timeTakenMillis())
				.append("prompt", submission.promptTitle())
				.append("submittedText", submission.userTypingSubmission())
				.append("date", LocalDateTime.now())
				);

		double newWeightedTp = getWeightedTp(submission.userID());
		userUpdates.add(Updates.set("totalTp", newWeightedTp));

		streakStatusResult streak = getStreakStatus(String.valueOf(submission.userID()));
		switch (streak.status()) {
			case AVAILABLE:
				userUpdates.add(Updates.inc("daily.currentStreak", 1));
				// some other time :)
				// userUpdates.add(Updates.max("daily.maxStreak", ));
				userUpdates.add(Updates.set("daily.test", result.getInsertedId()));
				// storing an instant instead of a normal date here otherwise it would push the server's local timezone to the db instead of UTC.
				userUpdates.add(Updates.set("daily.updatedAt", new Date().toInstant().toString()));
				break;
			case CLAIMED:
				break;
			case INITIAL:
				userUpdates.add(Updates.set("daily", new Document()
					.append("currentStreak", 1)
					.append("maxStreak", 1)
					.append("test", result.getInsertedId())
					.append("updatedAt", new Date().toInstant().toString())
				));
				break;
			case MISSED:
				userUpdates.add(Updates.set("daily.currentStreak", 1));
				userUpdates.add(Updates.set("daily.test", result.getInsertedId()));
				userUpdates.add(Updates.set("daily.updatedAt", new Date().toInstant().toString()));
				break;
			default:
				break;

		}

		userUpdates.add(Updates.inc("playtime", submission.timeTakenMillis()));

		FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
		options.returnDocument(ReturnDocument.AFTER);
		options.upsert(true);

		Document user = usersV2.findOneAndUpdate(
			Filters.eq("discordId", String.valueOf(submission.userID())),
			Updates.combine(
				userUpdates
				),
			options
		);

		double rawTp = newWeightedTp - initialWeightedTp;

		List<AchievementDetails> achievements = checkForAchievements(submission, user, rawTp);

		return new AddTestResult(rawTp, streak.currentStreak(), achievements);

	}

	public static streakStatusResult getStreakStatus(String discordId) {
		try {
			Document daily = usersV2.find(Filters.eq("discordId", discordId)).first().get("daily", Document.class);
			Date testDate = Date.from(Instant.parse(daily.getString("updatedAt")));

			Calendar lockedUntilDate = Calendar.getInstance();
			lockedUntilDate.setTime(testDate);
			lockedUntilDate.add(Calendar.HOUR_OF_DAY, 24);

			Calendar expiryDate = Calendar.getInstance();
			expiryDate.setTime(testDate);
			expiryDate.add(Calendar.HOUR_OF_DAY, 48);

			if (new Date().before(lockedUntilDate.getTime())) {
				return new streakStatusResult(0, streakStatus.CLAIMED, lockedUntilDate);
			}

			if (new Date().after(expiryDate.getTime())) {
				return new streakStatusResult(1, streakStatus.MISSED, expiryDate);
			}

			return new streakStatusResult(daily.getInteger("currentStreak") + 1, streakStatus.AVAILABLE, expiryDate);
		} catch (Exception e) {
			System.out.println(e);
			return new streakStatusResult(1, streakStatus.INITIAL, null);
		}

	}

	public static double addPrompt(String title, String text)
	{
		double rating = CalculatePromptDifficulty.calculateSinglePrompt(text.toCharArray()).typeRating();
		prompts.insertOne(new Document()
				.append("_id", new ObjectId())
				.append("title", title)
				.append("text", text)
				.append("rating", rating));

		return rating;
	}

	public static ArrayList<Document> getPrompts() {
		ArrayList<Document> documents = new ArrayList<Document>();

		prompts.aggregate(Arrays.asList()).forEach(doc -> documents.add(doc));

		return documents;
	}

	public static void recalcPrompts() {
		ArrayList<Document> docs = getPrompts();

		for (Document prompt : docs) {
			String text = prompt.getString("text");
			double rating = CalculatePromptDifficulty.calculateSinglePrompt(text.toCharArray()).typeRating();

			System.out.println(String.format("%f -> %f", rating, prompt.getDouble("rating")));
			prompts.updateOne(Filters.eq("text", text), Updates.set("rating", rating));
		}
	}

	public static RefreshUserNamesResult refreshUserNames(MessageReceivedEvent event) {
		JDA jda = event.getJDA();
		long start = System.currentTimeMillis();
		int outdatedTagsCount = 0;

		for (Document user : users.find()) {
			//TODO: this needs to be updated soon when discriminators no longer exist in tags. for now, if the tag ends with #0000 that most likely means that the user has updated their tag.
			String userTag = jda.retrieveUserById( user.getString("discordId") ).complete().getAsTag();
			if (userTag.endsWith("#0000")) userTag = userTag.substring(0, userTag.length() - 5);

			outdatedTagsCount += users.updateOne(Filters.eq("_id", user.getObjectId("_id")), Updates.set("userTag", userTag)).getModifiedCount();
		}
		
		long timeTakenMillis = System.currentTimeMillis() - start;

		return new RefreshUserNamesResult(timeTakenMillis, outdatedTagsCount);
	}

	public static String getStats(String discordId, Boolean old)
	{
		
		MongoCollection<Document> tests = client.getDatabase(DB_NAME).getCollection(old ? "tests" : "testsv2");
		MongoCollection<Document> users = client.getDatabase(DB_NAME).getCollection(old ? "users" : "usersv2");

		tests.find(Filters.eq("discordId", discordId)).sort(descending("tp"));

		Document stats = tests.aggregate(Arrays.asList(
				Aggregates.match(Filters.eq("discordId", discordId)),
				Aggregates.sort(descending("date")),
				Aggregates.limit(10),
				Aggregates.group("$discordId", 
						Accumulators.avg("averageWpm", "$wpm"),
						Accumulators.avg("averageAcc", "$accuracy"),
						Accumulators.max("bestWpm", "$wpm"),
						Accumulators.stdDevPop("deviation", "$wpm")
						),
				Aggregates.set(new Field<Double>("weightedTp", getWeightedTp(Long.valueOf(discordId)))),
				Aggregates.set(new Field<Double>("playtime", users.find(Filters.eq("discordId", discordId)).first().getDouble("playtime")))
				)).first();


		return stats.toJson();
	}

	//TODO: merge this into just one stats command
	public static String getGlobalStats(String discordId) {return "";}

	public static AggregateIterable<Document> getLeaderboards(LeaderboardConfig lbConfig) {
		MongoCollection<Document> collection = client.getDatabase(DB_NAME).getCollection(lbConfig.getCollection());

		return collection.aggregate(Arrays.asList(
			Aggregates.group("$discordId", 
				lbConfig.getAccumulationStrategies()
					),
			Aggregates.sort(descending(lbConfig.getStatistic()))
		));

	}

	private static double getWeightedTp(long id)
	{
		AggregateIterable<Document> tpList = testsV2.aggregate(Arrays.asList(
				Aggregates.match(Filters.eq("discordId", String.valueOf(id))),
				Aggregates.match(Filters.exists("tp")),
				Aggregates.sort(descending("tp")),
				Aggregates.limit(100)
				));

		double weightedTp = 0;
		int index = 0;
		for (Document test : tpList) {weightedTp += (test.getDouble("tp") * Math.pow(0.95, index++));}

		return weightedTp;
	}

	/**
	 * Get an achievement by the title.
	 * @param title : Name of the achievement
	 * @return {@link AchievementDetails}
	 */
	public static AchievementDetails getAchievement(String title) {
		try {
		Document doc = achievements.find(Filters.regex("title", Pattern.compile(title, Pattern.CASE_INSENSITIVE))).first();
		return new AchievementDetails(doc.getString("title"), doc.getString("description"), doc.getString("thumbnail"));
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
	}

	/**
	 * Get a progressive achievement by the level of the achievement
	 * @param levelField : the group of achivements, eg "wpmLevel", "tpLevel"
	 * @param level : the level of the achievement
	 * @return {@link AchievementDetails}
	 */
	public static AchievementDetails getAchievement(String levelField, int level) {
		try {
		Document doc = achievements.find(Filters.eq(levelField, level)).first();
		return new AchievementDetails(doc.getString("title"), doc.getString("description"), doc.getString("thumbnail"));
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
	}

	/**
	 * Get a list of paginated achievements
	 * @param page : page number of achievements
	 * @return {@link AggregateIterable}
	 */
	public static AggregateIterable<Document> getAchievementList(int page) {
		return achievements.aggregate(Arrays.asList(
			Aggregates.sort(ascending("_id")),
			Aggregates.skip(10*(page - 1)),
			Aggregates.limit(10)
		));
	}


	/**
	 * Checks whether a user has received any achievements, after a test submission. 
	 * @param submission : {@link TypingSubmission}
	 * @param user : The updated user document
	 * @param rawTp : rawTp that has been gained as a result of the test
	 * @return a List of {@link AchievementDetails}
	 */
	private static List<AchievementDetails> checkForAchievements(TypingSubmission submission, Document user, double rawTp) {

		ArrayList<AchievementDetails> achievedList = new ArrayList<AchievementDetails>();
		ArrayList<Bson> userUpdates = new ArrayList<Bson>();
		Document currentUserAchievements = user.get("achievements", Document.class);

		// check if achievement object exists, if not, create it
		if (currentUserAchievements == null) {
			usersV2.updateOne(
				Filters.eq("discordId", String.valueOf(submission.userID())),
				Updates.set("achievements", new Document())
			);
			currentUserAchievements = new Document();
		}

		int wpmLevel = 0;

		// WPM ACHIEVEMENTS
		if (submission.wordsPerMinute() >= 150)	wpmLevel = 8;
		else if (submission.wordsPerMinute() >= 140) wpmLevel = 7;
		else if (submission.wordsPerMinute() >= 130) wpmLevel = 6;
		else if (submission.wordsPerMinute() >= 120) wpmLevel = 5;
		else if (submission.wordsPerMinute() >= 110) wpmLevel = 4;
		else if (submission.wordsPerMinute() >= 100) wpmLevel = 3;
		else if (submission.wordsPerMinute() >= 80) wpmLevel = 2;
		else if (submission.wordsPerMinute() >= 60) wpmLevel = 1;
		
		// TODO: this looks like it can easily be abstracted into a function, since it's the same thing for every single achievement
		if (Objects.requireNonNullElse(currentUserAchievements.getInteger("wpmLevel"), 0) < wpmLevel) {
			userUpdates.add(Updates.set("achievements.wpmLevel", wpmLevel));
			achievedList.add(getAchievement("wpmLevel", wpmLevel));
		}

		//TP ACHIEVEMENTS
		double tp = user.getDouble("totalTp");
		int tpLevel = 0;

		if (tp >= 9000) tpLevel = 9;
		else if (tp >= 8000) tpLevel = 8;
		else if (tp >= 7000) tpLevel = 7;
		else if (tp >= 6000) tpLevel = 6;
		else if (tp >= 5000) tpLevel = 5;
		else if (tp >= 4000) tpLevel = 4;
		else if (tp >= 3000) tpLevel = 3;
		else if (tp >= 2000) tpLevel = 2;
		else if (tp >= 1000) tpLevel = 1;

		if (Objects.requireNonNullElse(currentUserAchievements.getInteger("tpLevel"), 0) < tpLevel) {
			userUpdates.add(Updates.set("achievements.tpLevel", tpLevel));
			achievedList.add(getAchievement("tpLevel", tpLevel));
		}

		// PLAYTIME ACHIEVEMENTS
		double playtimeHours = user.getDouble("playtime") / (1000 * 60 * 60);
		int playtimeLevel = 0;

		if (playtimeHours >= 500) playtimeLevel = 4;
		else if (playtimeHours >= 250) playtimeLevel = 3;
		else if (playtimeHours >= 100) playtimeLevel = 2;
		else if (playtimeHours >= 50) playtimeLevel = 1;

		if (Objects.requireNonNullElse(currentUserAchievements.getInteger("playtimeLevel"), 0) < playtimeLevel) {
			userUpdates.add(Updates.set("achievements.playtimeLevel", playtimeLevel));
			achievedList.add(getAchievement("playtimeLevel", playtimeLevel));
		}
		
		// STATISTICAL SYMMETRY
		if (submission.wordsPerMinute() == submission.accuracy()) {

			if (!Objects.requireNonNullElse(currentUserAchievements.getBoolean("statisticalSymmetry"), false)) {
				userUpdates.add(Updates.set("achievements.statisticalSymmetry", true));
				achievedList.add(getAchievement("Statistical Symmetry"));
			}
			
		}

		// SSGOD
		if (submission.accuracy() == 100.0) {
			AggregateIterable<Document> stats = testsV2.aggregate(Arrays.asList(
					Aggregates.match(Filters.eq("discordId", String.valueOf(submission.userID()))),
					Aggregates.sort(descending("date")),
					Aggregates.limit(10)
					));

			boolean allSS = true;
			int count = 0;
			for (Document test : stats) {
				if (test.getDouble("accuracy") != 100.0) {
					allSS = false;
				}
				count++;
			}

			if (allSS && count == 10) {
				if (!Objects.requireNonNullElse(currentUserAchievements.getBoolean("SSGod"), false)) {
					userUpdates.add(Updates.set("achievements.SSGod", true));
					achievedList.add(getAchievement("SSGod"));
				}
			}
			
		}

		// PROMPT CRITICAL
		// skip over this if no TP is gained, save us an unnecessary db call
		if (rawTp != 0.0) {
			Document topUser = usersV2.find().sort(descending("totalTp")).limit(1).first();

			if (String.valueOf(submission.userID()).equals(topUser.getString("discordId"))) {
				if (!Objects.requireNonNullElse(currentUserAchievements.getBoolean("promptCritical"), false)) {
					userUpdates.add(Updates.set("achievements.promptCritical", true));
					achievedList.add(getAchievement("Prompt Critical"));
				}
			}

		}

		// MAX DEDICATION
		if (submission.promptTitle().equals("Chromebook Chronicles")) {
			if (!Objects.requireNonNullElse(currentUserAchievements.getBoolean("maximumDedication"), false)) {
				userUpdates.add(Updates.set("achievements.maximumDedication", true));
				achievedList.add(getAchievement("Maximum Dedication"));
			}
		}

		if (userUpdates.size() > 0) {
			usersV2.updateOne(
				Filters.eq("discordId", String.valueOf(submission.userID())),
				Updates.combine(
					userUpdates
					),
				upsertTrue
			);
		}
		
		return achievedList;
	}

}
