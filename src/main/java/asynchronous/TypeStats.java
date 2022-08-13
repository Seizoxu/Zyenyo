package asynchronous;

import java.awt.Color;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TypeStats implements Runnable
{
	private MessageReceivedEvent event;
	public TypeStats(MessageReceivedEvent event) {this.event = event;}
	
	@Override
	public void run()
	{
		try
		{
			String jsonString = TypingApiHandler.requestData(event.getAuthor().getIdLong(), "stats");
			JSONObject json = (JSONObject) JSONValue.parse(jsonString);
			
			int testsTaken = Integer.parseInt(json.get("tests").toString());
			double averageWpm = Double.parseDouble(json.get("averageWpm").toString());
			double averageAcc = Double.parseDouble(json.get("averageAcc").toString());
			double bestWpm = Double.parseDouble(json.get("bestWpm").toString());
			double deviation = Double.parseDouble(json.get("deviation").toString());
			String rank = json.get("rank").toString();
			
			event.getChannel().sendMessageEmbeds(new EmbedBuilder()
					.addField("Stats for " + event.getAuthor().getAsTag(),
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
		catch (IOException | InterruptedException e) {e.printStackTrace();}
	}
}
