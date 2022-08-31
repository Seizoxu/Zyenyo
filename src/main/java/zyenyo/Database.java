package zyenyo;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;


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
}
