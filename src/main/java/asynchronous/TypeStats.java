package asynchronous;

import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import dataStructures.InfoCard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TypeStats implements Runnable
{
	private MessageReceivedEvent event;
	private MessageChannel channel;
	private String idStr;
	private String[] args;
	private boolean requestGlobal = false;
	private Runnable sendHelp = new Runnable()
		{@Override public void run() {channel.sendMessageEmbeds(InfoCard.INCORRECT_SYNTAX.build()).queue();}};
	public TypeStats(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.channel = event.getChannel();
		this.args = args;
	}
	
	@Override
	public void run()
	{
		channel.sendTyping();
		try
		{
			// Get command parameters.
			if (args.length == 1) {idStr = event.getAuthor().getId();}
			else if (args.length == 2)
			{
				if (args[1].equals("-g"))
				{
					idStr = event.getAuthor().getId();
					requestGlobal = true;
				}
				else {idStr = args[1].subSequence(2, args[1].length()-1).toString();}
			}
			else if (args.length == 3 && args[2].equals("-g"))
			{
				idStr = args[1].subSequence(2, args[1].length()-1).toString();
				requestGlobal = true;
			}
			else {sendHelp.run(); return;}
			// Yes, the above line blocks the curr. thread, but this class is already another thread, and it doesn't need to go further.
			
			Long id = Long.parseLong(idStr); // Used for error checking; will change later.
			String testsTaken="", title;
			JSONObject json;
			
			if (requestGlobal)
			{
				json = (JSONObject) JSONValue.parse(TypingApiHandler.requestData("stats/global/" + id));
				testsTaken = String.format("Tests Taken: **`%s`**%n", json.get("tests").toString());
				title = "Global Typing Statistics for " + event.getJDA().retrieveUserById(id).submit().get().getAsTag();
			}
			else
			{
				json = (JSONObject) JSONValue.parse(TypingApiHandler.requestData("stats/recent/" + id));
				title = "Recent Typing Statistics for " + event.getJDA().retrieveUserById(id).submit().get().getAsTag();
			}
			
			double averageWpm = Double.parseDouble(json.get("averageWpm").toString());
			double averageAcc = Double.parseDouble(json.get("averageAcc").toString());
			double bestWpm = Double.parseDouble(json.get("bestWpm").toString());
			double deviation = Double.parseDouble(json.get("deviation").toString());
			String rank = json.get("rank").toString();
			
			channel.sendMessageEmbeds(new EmbedBuilder()
					.addField(title,
							String.format("%s"
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
