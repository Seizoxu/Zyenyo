package asynchronous;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import zyenyo.Database;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;

import org.apache.commons.lang3.StringUtils;  

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
			if (args[1].equalsIgnoreCase("-acc")) {statisticType = "accuracy";}
			else if (args[1].equalsIgnoreCase("-wpm")) {statisticType = "wpm";}
			else if (args[1].equalsIgnoreCase("-tp")) {statisticType = "tp"; leaderboardScope = "Total";}
		}
		else if (args.length == 3)
		{
			if (args[1].equalsIgnoreCase("-wpm") && args[2].equalsIgnoreCase("-best"))
			{
				statisticType = "wpm";
				leaderboardScope = "Best";
			}
			else if (args[1].equalsIgnoreCase("-tp") && args[2].equalsIgnoreCase("-best"))
			{
				statisticType = "tp";
				leaderboardScope = "Best";
			}
			else if (args[1].equalsIgnoreCase("-acc") && args[2].equalsIgnoreCase("-best")) {sendHelp.run(); return;}
		}
		else {sendHelp.run(); return;}

		AggregateIterable<Document> lbList = Database.getLeaderboards(statisticType, leaderboardScope);

		EmbedBuilder leaderboardEmbed = new EmbedBuilder()
				.setTitle(String.format("Global %s %s Leaderboards",
						leaderboardScope, StringUtils.capitalize(statisticType)));
		JSONObject leaderboardMember;

		int position = 0;
		String userTag;
		double statistic;

		for (Document user : lbList) {
			System.out.println(user.toJson());
			userTag = jda.retrieveUserById(user.getString("_id")).complete().getAsTag();
			statistic = user.getDouble(statisticType);

			leaderboardEmbed.appendDescription(String.format("%n**#%d | %s**: `%.2f`", ++position, userTag, statistic));
			System.out.println(String.format("%n**#%d | %s**: `%.2f`", position, userTag, statistic));
		}

		event.getChannel().sendMessageEmbeds(leaderboardEmbed.build()).queue();
		System.out.println("done");
	}
}
