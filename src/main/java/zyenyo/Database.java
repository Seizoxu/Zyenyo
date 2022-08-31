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

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

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
}
