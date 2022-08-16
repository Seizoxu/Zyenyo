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
		String statisticType="";
		String leaderboardScope = "average"; // Average vs best, future implementation.
		if (args.length == 1) {statisticType = "wpm";}
		else if (args[1].equals("-wpm")) {statisticType = "wpm";}
		else if (args[1].equals("-acc")) {statisticType = "acc";}
		else {sendHelp.run(); return;}
		
		try
		{
			JSONArray json = (JSONArray) ((JSONObject)JSONValue.parse(TypingApiHandler.requestData(
					String.format("leaderboards/%s/%s", statisticType, leaderboardScope)))).get("lb");
			
			EmbedBuilder leaderboardEmbed = new EmbedBuilder()
					.setTitle("Global " + ((statisticType.equals("wpm")) ? "WPM" : "Accuracy") + " Leaderboards");
			JSONObject leaderboardMember;
			String userTag;
			double averageWPM, averageAcc;
			for (int i = 0; (i < json.size() && i < 10);)
			{
				leaderboardMember = (JSONObject) json.get(i);
				userTag = jda.retrieveUserById(leaderboardMember.get("_id").toString()).complete().getAsTag();
				averageWPM = Double.parseDouble(leaderboardMember.get("averageWpm").toString());
				averageAcc = Double.parseDouble(leaderboardMember.get("averageAcc").toString());
				
				leaderboardEmbed.appendDescription(String.format("%n**#%d | %s**: `%.2f WPM`, `%.2f%%`", ++i, userTag, averageWPM, averageAcc));
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
