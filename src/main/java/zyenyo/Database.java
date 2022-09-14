package zyenyo;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.model.Field;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import java.util.Arrays;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Accumulators;
import org.bson.Document;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Sorts.descending;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class Database {
  static final String DB_NAME = "MyDatabase";

  private static MongoClient client;
  private static MongoCollection<Document> tests;
  private static MongoCollection<Document> users;

  public static void connect(String uri) {
    client = MongoClients.create(uri);
    tests = client.getDatabase(DB_NAME).getCollection("tests");
    users = client.getDatabase(DB_NAME).getCollection("users");
  }

  public static void addTest(long discordId, double wpm, double accuracy, double tp) {
    tests.insertOne(new Document()
          .append("_id", new ObjectId())
          .append("discordId", discordId)
          .append("wpm", wpm)
          .append("accuracy", accuracy)
          .append("tp", tp)
          .append("date", LocalDateTime.now())
          );
  }

  public static String getStats(String discordId) {
    double userTp = users.find(Filters.eq("discordId", discordId)).first().getDouble("totalTp");

    Document stats = tests.aggregate(Arrays.asList(
      Aggregates.match(Filters.eq("discordId", discordId)),
      Aggregates.sort(descending("date")),
      Aggregates.limit(10),
      Aggregates.group("discordId", 
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

}
