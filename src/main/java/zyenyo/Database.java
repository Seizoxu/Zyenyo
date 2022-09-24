package zyenyo;

import static com.mongodb.client.model.Sorts.descending;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class Database
{
	static final String DB_NAME = "MyDatabase";

	private static MongoClient client;
	private static MongoCollection<Document> tests;
	private static MongoCollection<Document> users;
	private static MongoCollection<Document> prompts;

	public static void connect(String uri)
	{
		client = MongoClients.create(uri);
		tests = client.getDatabase(DB_NAME).getCollection("tests");
		users = client.getDatabase(DB_NAME).getCollection("users");
		prompts = client.getDatabase(DB_NAME).getCollection("prompts");
	}

	public static double addTest(long discordId, double wpm, double accuracy, double tp)
	{
		tests.insertOne(new Document()
				.append("_id", new ObjectId())
				.append("discordId", String.valueOf(discordId))
				.append("wpm", wpm)
				.append("accuracy", accuracy)
				.append("tp", tp)
				.append("date", LocalDateTime.now())
				);

		// Update users collection.
		double weightedTp = getWeightedTp(discordId);

		Document usr = users.findOneAndUpdate(Filters.eq("discordId", String.valueOf(discordId)), Updates.set("totalTp", weightedTp));
		if (usr == null)
		{
			users.insertOne(new Document()
					.append("_id", new ObjectId())
					.append("discordId", String.valueOf(discordId))
					.append("totalTp", weightedTp));
		}

		return weightedTp - usr.getDouble("totalTp");

	}

	public static void addPrompt(String title, String text)
	{
		prompts.insertOne(new Document()
				.append("_id", new ObjectId())
				.append("title", title)
				.append("text", text)
				.append("rating", CalculatePromptDifficulty.calculateSinglePrompt(text.toCharArray())));
	}

	public static String getStats(String discordId)
	{
		double userTp = users.find(Filters.eq("discordId", discordId)).first().getDouble("totalTp");

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
				Aggregates.set(new Field<Double>("weightedTp", userTp))
				)).first();


		return stats.toJson();
	}

	//TODO
	public static String getGlobalStats(String discordId) {return "";}

	public static AggregateIterable<Document> getLeaderboards(String statisticType, String leaderboardScope)
	{
		if (leaderboardScope.equals("Best"))
		{
			return tests.aggregate(Arrays.asList(
					Aggregates.match(Filters.exists(statisticType)),
					Aggregates.group("$discordId", 
							Accumulators.max(statisticType, "$" + statisticType)
							),
					Aggregates.sort(descending(statisticType))
					));
		}
		else if (leaderboardScope.equals("Total"))
		{
			return users.aggregate(Arrays.asList(
					Aggregates.match(Filters.exists("totalTp")),
					Aggregates.group("$discordId", 
							Accumulators.sum(statisticType, "$totalTp")
							),
					Aggregates.sort(descending(statisticType))
					));

		}
		else // Average
		{
			return tests.aggregate(Arrays.asList(
					Aggregates.match(Filters.exists(statisticType)),
					Aggregates.group("$discordId", 
							Accumulators.avg(statisticType, "$" + statisticType)
							),
					Aggregates.sort(descending(statisticType))
					));

		}

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
