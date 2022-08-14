package asynchronous;

import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TypeStats implements Runnable
{
	private MessageReceivedEvent event;
	private MessageChannel channel;
	private String idStr;
	public TypeStats(MessageReceivedEvent event, String id)
	{
		this.event = event;
		this.channel = event.getChannel();
		this.idStr = id;
	}
	
	@Override
	public void run()
	{
		channel.sendTyping();
		try
		{
			Long id = Long.parseLong(idStr);
			String jsonString = TypingApiHandler.requestData(id, "stats");
			JSONObject json = (JSONObject) JSONValue.parse(jsonString);
			
			int testsTaken = Integer.parseInt(json.get("tests").toString());
			double averageWpm = Double.parseDouble(json.get("averageWpm").toString());
			double averageAcc = Double.parseDouble(json.get("averageAcc").toString());
			double bestWpm = Double.parseDouble(json.get("bestWpm").toString());
			double deviation = Double.parseDouble(json.get("deviation").toString());
			String rank = json.get("rank").toString();
			
			channel.sendMessageEmbeds(new EmbedBuilder()
					.addField("Stats for " + event.getJDA().retrieveUserById(id).submit().get().getAsTag(),
							String.format("Tests Taken: **`%d`**%n"
									+ "Best WPM: **`%.2f`**%n"
									+ "Average WPM: **`%.2f`**%n"
									+ "Deviation: **`%.2f`**%n"
									+ "Average Accuracy: **`%.2f%%`**%n"
									+ "Rank: **`%s`**",
									testsTaken, bestWpm, averageWpm, deviation, averageAcc, rank), false)
					.setColor(new Color(180, 50, 80))
					.build())
			.queue();
		}
		catch (IOException | InterruptedException | NumberFormatException e)
		{
			channel.sendMessageEmbeds(new EmbedBuilder()
					.addField("Error: Cannot fetch user stats.", "Reason: User not in database.", false)
					.build())
			.queue();
		}
		catch (ExecutionException e)
		{
			channel.sendMessageEmbeds(new EmbedBuilder()
					.addField("Error: Cannot fetch user stats.", "Reason: User does not exist.", false)
					.build())
			.queue();
		}
	}
}
