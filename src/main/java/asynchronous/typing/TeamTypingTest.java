package asynchronous.typing;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dataStructures.TypingSubmission;

import zyenyo.Database;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.util.Arrays;
import commands.Typing;
import net.dv8tion.jda.api.EmbedBuilder;

public class TeamTypingTest extends TypingTest
{
  private String[] teamA;
  private String[] teamB;

  public TeamTypingTest(MessageReceivedEvent event, String[] args) {
    super(event, args);
    String[] players = Arrays.copyOfRange(args, 1, args.length);

    teamA = Arrays.copyOfRange(players,0, (int)players.length/2);
    teamB = Arrays.copyOfRange(players, (int)players.length/2, players.length);

  }

  @Override
  public void run() throws NumberFormatException
  {
    constructAndSendTest("none");
    event.getJDA().addEventListener(this);
  }
        @Override
	public void quitTest()
	{
		scheduledStop.cancel(true);
		concludeTest.run();
	}


	private Runnable concludeTest = new Runnable()
	{
		@Override
		public void run()
		{
			// Print leaderboard.
			event.getJDA().removeEventListener(thisInstance);
			channel.sendTyping().queue();
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("Typing Test Results");

			// Sorting by Typing Points.
			HashMap<Short, TypingSubmission> leaderboardMap = submissions.getMap();
			List<Integer> lbOrder = leaderboardMap.keySet().stream()
					.mapToInt(x -> (int)x)
					.boxed()
					.sorted((a,b)->Double.compare(submissions.getSubmission(b).typingPoints(), submissions.getSubmission(a).typingPoints()))
					.collect(Collectors.toList());

                        String team;
                        double teamATotal = 0;
                        double teamBTotal = 0;

			for (int i = 0; i < submissions.getNumSubmissions(); i++)
			{


				TypingSubmission s = submissions.getSubmission(lbOrder.get(i));
                                System.out.println(s.userID());
                                Arrays.asList(teamA).forEach((String a) -> System.out.println(a));
                                Arrays.asList(teamB).forEach((String a) -> System.out.println(a));
                                if (Arrays.asList(teamA).contains(String.format("<@%s>", s.userID()))) {team = "team A"; teamATotal += s.typingPoints();}
                                else if (Arrays.asList(teamB).contains(String.format("<@%s>", s.userID()))) {team = "team B"; teamBTotal += s.typingPoints();}
                                else {continue;}
				//double rawTp = Database.addTest(s.userID(), s.wordsPerMinute(), s.accuracy(), s.typingPoints());

				embed.addField(
						String.format("#%d.) %s **(%s)**", i+1, s.userTag(), team),
						String.format(
								"TP: **`%.2f`**%n"
										+ "WPM: **`%.2f`**%n"
										+ "Accuracy: **`%.2f`**%%%n",
										s.typingPoints(), s.wordsPerMinute(), s.accuracy()),
						false);

			}

                        embed.addField("Team Results", String.format("*Team A Total:* **`%.2f`**%n"
                                                                                                + "*Team B Total:* **`%.2f`%n**", 
                                                                                                teamATotal, teamBTotal), false);
                        double difference = teamATotal - teamBTotal;
                        String winner = difference > 0 ? "A" : "B";

                        embed.addField(String.format("**Team %s Wins By: `%.2f` Points!**", winner, Math.abs(difference)), "", false);

			message.replyEmbeds(embed.build()).queue();
			Typing.guildTestList.remove(event.getGuild().getIdLong());
		}
	};


}
