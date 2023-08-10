package asynchronous.typing;

import java.util.Arrays;

import org.bson.Document;

import com.google.common.collect.Iterables;
import com.mongodb.client.AggregateIterable;

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
		Boolean old = false;
		
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
				case "-p": lbPage = getValueArg(args, "-p"); break;
				case "-page": lbPage = getValueArg(args, "-page"); break;
				case "-tests": lbStat = LeaderboardStatisticType.TESTS; break;
				case "-old": old = true; break;
			}
		}
		
		LeaderboardConfig lbConfig = new LeaderboardConfig(lbStat, lbScope, old);

		AggregateIterable<Document> lbList = Database.getLeaderboards(lbConfig);

		EmbedBuilder leaderboardEmbed = new EmbedBuilder()
				.setTitle(lbConfig.getLeaderboardTitle());

		final int initialPosition = (lbPage -1) * 20;
		int position = (lbPage -1) * 20;
		String userTag;
		double statistic = 0;

		for (Document user : Iterables.skip(lbList, (lbPage - 1) * 20))
		{
			if (position - 20 >= initialPosition) {break;}
			
			try {
				//TODO: preferably get rid of jda call altogether as it is astonishingly slow
				userTag = !(user.getString("userTag") == null) ? user.getString("userTag") : jda.retrieveUserById( user.getString("_id") ).complete().getAsTag();
				statistic = user.getDouble( lbConfig.getStatistic() );
				leaderboardEmbed.appendDescription(String.format("%n**#%d | %s**: `%.2f`", ++position, userTag, statistic));
			} catch (Exception e) {
				System.out.println(e);
				// The error might just be one faulty doc so just continue constructing the leaderboard
				continue;
			}

		}

		leaderboardEmbed.setFooter(
				String.format("Showing user %d to %d on page %d.",
						initialPosition+1, initialPosition+20, lbPage)
		);
		
		event.getChannel().sendMessageEmbeds( leaderboardEmbed.build() ).queue();
	}
	
	
	public int getValueArg(String[] args, String checkArg)
	{
		String lbPageArg;
		int lbPage;
		
		try
		{
			lbPageArg = args[ Arrays.asList(args).indexOf(checkArg) + 1 ];
			lbPage = Integer.parseInt(lbPageArg);
			
			// Avoid ridiculously high page numbers.
			if ( lbPage >= 500 ) {return 1;}
		}
		catch (ArrayIndexOutOfBoundsException | NullPointerException | NumberFormatException e)
		{
			return 1;
		}
		
		return lbPage;
	}
}
