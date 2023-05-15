package asynchronous.typing;

import java.util.Arrays;

import org.bson.Document;

import com.google.common.collect.Iterables;
import com.mongodb.client.AggregateIterable;

import dataStructures.InfoCard;
import dataStructures.LeaderboardConfig;
import dataStructures.LeaderboardScope;
import dataStructures.LeaderboardStatisticType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;

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
		
		// default to tp leaderboard
		LeaderboardStatisticType lbStat = LeaderboardStatisticType.TP;
		LeaderboardScope lbScope = LeaderboardScope.SUM;
		int lbPage = 1;

		for (String cmd : args) {
			switch (cmd.toLowerCase()) {
				case "-tp": lbStat = LeaderboardStatisticType.TP; break;
				case "-wpm": lbStat = LeaderboardStatisticType.WPM; break;
				case "-acc":
				case "-accuracy": lbStat = LeaderboardStatisticType.ACCURACY; break;
				case "-best": lbScope = LeaderboardScope.BEST; break;
				case "-avg": 
				case "-average": lbScope = LeaderboardScope.AVERAGE; break;
				case "-sum": lbScope = LeaderboardScope.SUM; break;
				case "-p": lbPage = Integer.parseInt(args[Arrays.asList(args).indexOf("-p") + 1]); break;
				case "-page": lbPage = Integer.parseInt(args[Arrays.asList(args).indexOf("-page") + 1]); break;
			}
		}
		
		// Avoid ridiculously high page numbers.
		if (lbPage >= 500)
		{
			lbPage = 1;
		}
		
		LeaderboardConfig lbConfig = new LeaderboardConfig(lbStat, lbScope);

		AggregateIterable<Document> lbList = Database.getLeaderboards(lbConfig);

		EmbedBuilder leaderboardEmbed = new EmbedBuilder()
				.setTitle(lbConfig.getLeaderboardTitle());

		final int initialPosition = (lbPage -1) * 20;
		int position = (lbPage -1) * 20;
		String userTag;
		double statistic;

		for (Document user : Iterables.skip(lbList, (lbPage-1) * 20)) {
			if (position - 20 >= initialPosition) {break;}
			
			userTag = jda.retrieveUserById(user.getString("_id")).complete().getAsTag();
			statistic = user.getDouble(lbConfig.getStatistic());

			leaderboardEmbed.appendDescription(String.format("%n**#%d | %s**: `%.2f`", ++position, userTag, statistic));
		}

		leaderboardEmbed.setFooter
		(
				String.format("Showing user %d to %d on page %d.", initialPosition+1, initialPosition+20, lbPage)
		);
		
		event.getChannel().sendMessageEmbeds(leaderboardEmbed.build()).queue();
	}
}
