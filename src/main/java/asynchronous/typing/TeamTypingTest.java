package asynchronous.typing;

import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import zyenyo.BotConfig;
import zyenyo.Database;

import commands.Typing;
import dataStructures.TypingSubmission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TeamTypingTest extends TypingTestTemplate
{
	private String[] teamRed;
	private String[] teamBlue;
	private final static short WPM_MINIMUM = 30;

	public TeamTypingTest(MessageReceivedEvent event, String[] args)
	{
		super(event, args);
		String[] players = Arrays.copyOfRange(args, 1, args.length);

		teamRed = Arrays.copyOfRange(players,0, (int)players.length/2);
		teamBlue = Arrays.copyOfRange(players, (int)players.length/2, players.length);
	}

	@Override
	public void run() throws NumberFormatException
	{
		constructAndSendTest();
		event.getJDA().addEventListener(this);
	}
	
	
	protected void constructAndSendTest() // Essentially a duplicate of 
	{
		int promptNumber = (int) (BotConfig.NUM_PROMPTS*Math.random() + 1);
		
		promptRating = BotConfig.promptRatingMap.get(promptNumber);

		try (BufferedReader reader = new BufferedReader(new FileReader(
								String.format("%sprompt%d.txt", TEST_PROMPTS_FILEPATH, promptNumber)));)
		{
			prompt = reader.readLine();
			numChars = prompt.length();

			// Sets ending time and sends typing test.
			long endTime = (System.currentTimeMillis() / 1000) + (60*numChars / (WPM_MINIMUM * NUM_CHARS_IN_WORD));
			fakePrompt = prompt.substring(0, prompt.length()/2)
					+ ZERO_WIDTH_NON_JOINER
					+ prompt.substring(prompt.length()/2, prompt.length());
			
			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("Typing Prompt:")
					.setDescription(fakePrompt)
					.addField("Time", String.format("Test end time: <t:%d:R>.", endTime), false);
			
			channel.sendMessageEmbeds(embed.build()).complete();
			startTime = System.currentTimeMillis();

			// Makes sure the typing test finishes on time.
			long delay = endTime*1000 - startTime;
			scheduledStop = schedulePool.schedule(concludeTest, delay, TimeUnit.MILLISECONDS);
		}
		catch (IOException e)
		{
			channel.sendMessageEmbeds(new EmbedBuilder()
					.addField("Error", "Internal error — contact developer.", false)
					.build())
			.queue();
		}
	}
	
	
	@Override // Override, because the original method will invoke the template's Runnables.
	public void quitTest()
	{
		try
		{
			scheduledStop.cancel(true);
		}
		catch (NullPointerException e)
		{
			System.out.println("[ERROR: TYPINGTEST] Could not quit test. Aborting...");
		}
		finally
		{
			concludeTest.run();
		}
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
					.sorted((a,b)->Double.compare(
							submissions.getSubmission(b).typingPoints(),
							submissions.getSubmission(a).typingPoints()))
					.collect(Collectors.toList());

			double teamRedTotal = 0;
			double teamBlueTotal = 0;

			ArrayList<TypingSubmission> red = new ArrayList<TypingSubmission>();
			ArrayList<TypingSubmission> blue = new ArrayList<TypingSubmission>();

			for (int i = 0; i < submissions.getNumSubmissions(); i++)
			{
				TypingSubmission s = submissions.getSubmission(lbOrder.get(i));
				System.out.println(s.userID());
				Arrays.asList(teamRed).forEach((String a) -> System.out.println(a));
				Arrays.asList(teamBlue).forEach((String a) -> System.out.println(a));
				
				if (Arrays.asList(teamRed).contains(String.format("<@%s>", s.userID())))
				{
					red.add(s);
					teamRedTotal += s.typingPoints();
				}
				else if (Arrays.asList(teamBlue).contains(String.format("<@%s>", s.userID())))
				{
					blue.add(s);
					teamBlueTotal += s.typingPoints();
				}
				else {continue;}
				
				Database.addTest(s.userID(), s.wordsPerMinute(), s.accuracy(), s.typingPoints());
			}

			int i = 0;
			//add submissions strategically so that red and blue teams have their own columns
			while (i < red.size() || i < blue.size())
			{
				if (i < red.size())
				{
					embed.addField(
							String.format("%s %s ", ":red_square:",
									red.get(i).userTag()),
							String.format("TP: **`%.2f`**%n",
									red.get(i).typingPoints()),
							true);
				}
				else {embed.addField("","",true);}
				
				if (i < blue.size())
				{
					embed.addField(
							String.format("%s %s ",
									":blue_square:",
									blue.get(i).userTag()),
							String.format("TP: **`%.2f`**%n",
									blue.get(i).typingPoints()),
							true);
				}
				else {embed.addField("","",true);}
				
				embed.addField("","", true);

				i++;
			}

			embed.addField(
					"Team Results",
					String.format("*Team Red Total:* **`%.2f`**%n"
							+ "*Team Blue Total:* **`%.2f`%n**", 
							teamRedTotal,
							teamBlueTotal),
					false);
			
			double difference = teamRedTotal - teamBlueTotal;
			String winner = difference > 0 ? "Red" : "Blue";

			embed.addField(
					String.format("**Team %s Wins By: `%.2f` Points!**",
							winner,
							Math.abs(difference)),
					"",
					false);

			message.replyEmbeds(embed.build()).queue();
			Typing.guildTestList.remove(event.getGuild().getIdLong());
		}
	};
}
