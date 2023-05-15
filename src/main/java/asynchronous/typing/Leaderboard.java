package asynchronous.typing;

import org.bson.Document;

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
			}
		}

		LeaderboardConfig lbConfig = new LeaderboardConfig(lbStat, lbScope);

		AggregateIterable<Document> lbList = Database.getLeaderboards(lbConfig);

		EmbedBuilder leaderboardEmbed = new EmbedBuilder()
				.setTitle(lbConfig.getLeaderboardTitle());

		int position = 0;
		String userTag;
		double statistic;

		for (Document user : lbList) {
			userTag = jda.retrieveUserById(user.getString("_id")).complete().getAsTag();
			statistic = user.getDouble(lbConfig.getStatistic());

			leaderboardEmbed.appendDescription(String.format("%n**#%d | %s**: `%.2f`", ++position, userTag, statistic));
		}

		event.getChannel().sendMessageEmbeds(leaderboardEmbed.build()).queue();
	}
}
