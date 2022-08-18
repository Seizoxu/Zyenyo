package asynchronous;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import dataStructures.InfoCard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Leaderboard implements Runnable
{
	private MessageReceivedEvent event;
	private JDA jda;
	private String[] args;
	private Runnable sendHelp = new Runnable()
	{@Override public void run() {event.getMessage().replyEmbeds(InfoCard.INCORRECT_SYNTAX.build()).queue();}};
	
	public Leaderboard(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.jda = event.getJDA();
		this.args = args;
	}
	
	@Override
	public void run()
	{
		event.getChannel().sendTyping().queue();
		
		String statisticType="";
		String leaderboardScope = "Average";
		if (args.length == 1) {statisticType = "wpm";}
		else if (args.length == 2)
		{
			if (args[1].equalsIgnoreCase("-acc")) {statisticType = "acc";}
			else if (args[1].equalsIgnoreCase("-wpm")) {statisticType = "wpm";}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("-wpm") && args[2].equalsIgnoreCase("-best"))
		{
			statisticType = "wpm";
			leaderboardScope = "Best";
		}
		else {sendHelp.run(); return;}
		
		try
		{
			JSONArray json = (JSONArray) ((JSONObject)JSONValue.parse(TypingApiHandler.requestData(
					String.format("leaderboards/%s/%s", statisticType, leaderboardScope)))).get("lb");
			
			EmbedBuilder leaderboardEmbed = new EmbedBuilder()
					.setTitle(String.format("Global %s %s Leaderboards",
							leaderboardScope, ((statisticType.equals("wpm")) ? "WPM" : "Accuracy")));
			JSONObject leaderboardMember;
			String userTag;
			double wordsPerMinute, averageAcc;
			boolean isBest = (leaderboardScope.equals("Best")) ? true : false;
			for (int i = 0; (i < json.size() && i < 10);)
			{
				leaderboardMember = (JSONObject) json.get(i);
				userTag = jda.retrieveUserById(leaderboardMember.get("_id").toString()).complete().getAsTag();
				
				if (isBest) // Not the most optimised; will be cleared later.
				{
					wordsPerMinute = Double.parseDouble(leaderboardMember.get("bestWpm").toString());
					leaderboardEmbed.appendDescription(String.format("%n**#%d | %s**: `%.2f WPM`", ++i, userTag, wordsPerMinute));
					continue;
				}
				
				wordsPerMinute = Double.parseDouble(leaderboardMember.get("averageWpm").toString());
				averageAcc = Double.parseDouble(leaderboardMember.get("averageAcc").toString());
				leaderboardEmbed.appendDescription(String.format("%n**#%d | %s**: `%.2f WPM`, `%.2f%%`", ++i, userTag, wordsPerMinute, averageAcc));
			}
			
			event.getChannel().sendMessageEmbeds(leaderboardEmbed.build()).queue();
		}
		catch (IOException | InterruptedException e)
		{
			event.getChannel().sendMessageEmbeds(new EmbedBuilder()
					.addField("Error: Cannot fetch leaderboard.", "Reason: Unknown.", false)
					.build())
			.queue();
		}
	}
}
