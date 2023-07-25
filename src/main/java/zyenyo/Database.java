package zyenyo;

import static com.mongodb.client.model.Sorts.descending;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;

import dataStructures.LeaderboardConfig;
import dataStructures.TypingSubmission;
import dataStructures.AddTestResult;
import dataStructures.streakStatus;
import dataStructures.streakStatusResult;


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
	private static MongoCollection<Document> users;
	private static MongoCollection<Document> prompts;
	
	private static UpdateOptions upsertTrue = new UpdateOptions().upsert(true);

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
		// tests will be queried very often for a user's top 100 tp scores. This needs to be fast.
		testsV2.createIndex(Indexes.descending("tp"));

		users = client.getDatabase(DB_NAME).getCollection("users");
		prompts = client.getDatabase(DB_NAME).getCollection("prompts");
	}

	public static AddTestResult addTest(long discordId, double wpm, double accuracy, double tp)
	{
		double initialWeightedTp = getWeightedTp(discordId);
		ArrayList<Bson> userUpdates = new ArrayList<Bson>();

		InsertOneResult result = tests.insertOne(new Document()
				.append("_id", new ObjectId())
				.append("discordId", String.valueOf(discordId))
				.append("wpm", wpm)
				.append("accuracy", accuracy)
				.append("tp", tp)
				.append("date", LocalDateTime.now())
				);
		
		double newWeightedTp = getWeightedTp(discordId);
		userUpdates.add(Updates.set("totalTp", newWeightedTp));

		streakStatusResult streak = getStreakStatus(String.valueOf(discordId));
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

		users.updateOne(
			Filters.eq("discordId", String.valueOf(discordId)),
			Updates.combine(
				userUpdates
				),
			upsertTrue
		);

		return new AddTestResult(newWeightedTp - initialWeightedTp, streak.currentStreak());
	}


	// TODO: make this the default addtest when tpv2 is done
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

		users.updateOne(
			Filters.eq("discordId", String.valueOf(submission.userID())),
			Updates.combine(
				userUpdates
				),
			upsertTrue
		);

		return new AddTestResult(newWeightedTp - initialWeightedTp, streak.currentStreak());

	}

	public static streakStatusResult getStreakStatus(String discordId) {
		try {
			Document daily = users.find(Filters.eq("discordId", discordId)).first().get("daily", Document.class);
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

	public static String getStats(String discordId)
	{
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
				Aggregates.set(new Field<Double>("weightedTp", getWeightedTp(Long.valueOf(discordId))))
				// For TPv2:
//				Aggregates.set(new Field<Double>("playtime", users.find(Filters.eq("discordId", discordId)).first().getDouble("playtime")))
				)).first();


		return stats.toJson();
	}

	//TODO: merge this into just one stats command
	public static String getGlobalStats(String discordId) {return "";}

	public static AggregateIterable<Document> getLeaderboards(LeaderboardConfig lbConfig) {
		MongoCollection<Document> collection = client.getDatabase(DB_NAME).getCollection(lbConfig.getCollection());

		return collection.aggregate(Arrays.asList(
			Aggregates.group("$discordId", 
				lbConfig.getAccumulationStrategy()
					),
			Aggregates.sort(descending(lbConfig.getStatistic()))
		));

	}

	private static double getWeightedTp(long id)
	{
		AggregateIterable<Document> tpList = tests.aggregate(Arrays.asList(
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

}
