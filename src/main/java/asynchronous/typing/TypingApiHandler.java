package asynchronous.typing;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TypingApiHandler
{
	public static String requestData(String requestStr) throws IOException, InterruptedException
	{
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://vanillaviking-type-api.herokuapp.com/" + requestStr))
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}
	
	public static String sendTest(long discordID, double wordsPerMinute, double accuracy, double typingPoints) throws IOException, InterruptedException
	{
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
			      .uri(URI.create(String.format("https://vanillaviking-type-api.herokuapp.com/test/add/%d", discordID)))
			      .header("Content-Type", "application/json")
			      .POST(HttpRequest.BodyPublishers.ofString(String.format(
			    		  "{\"wpm\": %f, \"accuracy\": %f, \"tp\": %f}", wordsPerMinute, accuracy, typingPoints)))
			      .build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}
}
