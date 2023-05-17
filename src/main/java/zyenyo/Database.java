package zyenyo;

import static com.mongodb.client.model.Sorts.descending;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandSucceededEvent;

import dataStructures.LeaderboardConfig;

class CommandMonitor implements CommandListener {
	@Override
	public synchronized void commandSucceeded(final CommandSucceededEvent event) {
		System.out.println(event.getCommandName() + " command " + event.getRequestId());
	}
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
		users = client.getDatabase(DB_NAME).getCollection("users");
		prompts = client.getDatabase(DB_NAME).getCollection("prompts");
	}

	public static double addTest(long discordId, double wpm, double accuracy, double tp)
	{
		double initialWeightedTp = getWeightedTp(discordId);

		tests.insertOne(new Document()
				.append("_id", new ObjectId())
				.append("discordId", String.valueOf(discordId))
				.append("wpm", wpm)
				.append("accuracy", accuracy)
				.append("tp", tp)
				.append("date", LocalDateTime.now())
				);
		
		double newWeightedTp = getWeightedTp(discordId);
		
		users.updateOne(
				Filters.eq("discordId", String.valueOf(discordId)),
				Updates.set("totalTp", newWeightedTp),
				upsertTrue
				);
		
		return newWeightedTp - initialWeightedTp;
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
				)).first();


		return stats.toJson();
	}

	//TODO
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
