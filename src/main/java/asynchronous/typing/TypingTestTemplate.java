package asynchronous.typing;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;

import commands.Typing;
import dataStructures.AddTestResult;
import dataStructures.TypingSubmission;
import dataStructures.TypingTestLeaderboard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import zyenyo.BotConfig;
import zyenyo.Database;

public abstract class TypingTestTemplate extends ListenerAdapter implements Runnable
{
	protected MessageReceivedEvent event;
	protected MessageChannel channel;
	protected Message message;
	protected String[] args;
	protected String prompt;
	protected double promptRating;
	protected String fakePrompt;
	protected int numChars;
	protected long startTime = -1;
	protected TypingTestTemplate thisInstance = this;
	protected TypingTestLeaderboard submissions = new TypingTestLeaderboard();

	protected final static String TEST_PROMPTS_FILEPATH = BotConfig.BOT_DATA_FILEPATH + "TypingPrompts/";
	protected final static String ZERO_WIDTH_NON_JOINER = "‌";
	protected final static short NUM_CHARS_IN_WORD = 5;
	
	protected ScheduledExecutorService schedulePool = Executors.newSingleThreadScheduledExecutor();
	protected Future<?> scheduledStop;
	
	
	public TypingTestTemplate(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.channel = event.getChannel();
		this.args = args;
		this.message = event.getMessage();
	}
	
	
	@Override
	public abstract void run();
	
	
	/**
	 * Checks whether a typing submission is valid, and calculates its statistics.
	 * @param event
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		long userID = event.getAuthor().getIdLong();
		MessageChannel answerChannel = event.getChannel();

		if (answerChannel.getIdLong() != channel.getIdLong()) {return;}
		else if (submissions.getUserIDs().contains(userID)) {return;}
		else if (event.getAuthor().isBot()) {return;}

		// Definitions.
		String userTypingSubmission = event.getMessage().getContentRaw();
		String userTag = event.getAuthor().getAsTag();

		// Data
		double timeTakenMillis = (System.currentTimeMillis()) - startTime;
		double wordsPerMinute = (numChars / timeTakenMillis) * 12000;
		
		int editDistance = new LevenshteinDistance().apply(prompt, userTypingSubmission);
		double accuracy = 100* (double)(prompt.length() - editDistance) / (double)prompt.length();

		
		if (accuracy < 75.0) {return;}
		if (wordsPerMinute >= 250.0 || userTypingSubmission.contains(ZERO_WIDTH_NON_JOINER))
		{
			event.getMessage()
			.replyFormat("Cheater detected. What a naughty user...")
			.queue();
			
			return;
		}
		
		
		// Metrics
		double wpmMultiplier = 0.2 * Math.pow(wordsPerMinute, 1.5);
		double accuracyMultiplier = Math.pow(0.95, 100-accuracy);
		double typingPoints = (wpmMultiplier * promptRating) * accuracyMultiplier;

		sendResult(
				event.getChannel(),
				new TypingSubmission(
						userID,
						userTag,
						wordsPerMinute,
						accuracy,
						typingPoints
						),
				timeTakenMillis
				);
	}
	
	
	/**
	 * Sends an individual prompt completion result whenever
	 * a user successfully finishes an active prompt.
	 * @param channel
	 * @param submission
	 * @param timeTakenMillis
	 */
	protected void sendResult(MessageChannel channel, TypingSubmission submission, double timeTakenMillis)
	{
		submissions.addSubmission(submission);

		channel.sendMessage(
				String.format(
						"<@%s> has completed the prompt in **`%.3f` seconds `[%.2f WPM]`**, "
						+ "with an accuracy of **`%.2f%%`**.%nTyping Points: **`%.2f TP`**.",
						submission.userID(),
						timeTakenMillis/1000,
						submission.wordsPerMinute(),
						submission.accuracy(),
						submission.typingPoints() ))
		.queue();
	}
	
	
	/** Quits the current test. */
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
	
	
	/**
	 * <p>A Runnable object that concludes the current running typing test.</p>
	 * <p> Intended to be scheduled, at minimum.</p>
	 * @since 0.1.0-beta
	 */
	protected Runnable concludeTest = new Runnable()
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

			for (int i = 0; i < submissions.getNumSubmissions(); i++)
			{
				TypingSubmission s = submissions.getSubmission(lbOrder.get(i));
				AddTestResult result = Database.addTest(s.userID(), s.wordsPerMinute(), s.accuracy(), s.typingPoints());
				String dailyStreak = "";
				if (result.dailyStreak() > 0) {
					dailyStreak = String.format("Daily Streak: **`%d`**", result.dailyStreak());
				}

				embed.addField(
						String.format("#%d.) %s", i+1, s.userTag()),
						String.format(
								"TP: **`%.2f`**%n"
										+ "WPM: **`%.2f`**%n"
										+ "Accuracy: **`%.2f`**%%%n"
										+ "Raw TP gained: **`%.2f`**%n"
										+ dailyStreak,
										s.typingPoints(), s.wordsPerMinute(), s.accuracy(), result.rawTp()),
						false);
			}
			
			message.replyEmbeds(embed.build()).queue();
			Typing.guildTestList.remove(event.getGuild().getIdLong());
		}
	};
}
