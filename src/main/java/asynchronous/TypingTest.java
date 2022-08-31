package asynchronous;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;

import commands.Typing;
import dataStructures.TypingSubmission;
import dataStructures.TypingTestLeaderboard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import zyenyo.BotConfig;
import zyenyo.Database;

public class TypingTest extends ListenerAdapter implements Runnable
{
	private MessageReceivedEvent event;
	private MessageChannel channel;
	private Message message;
	private String[] args;
	private String prompt;
	private double promptRating;
	private String fakePrompt;
	private int numChars;
	private long startTime = -1;
	private TypingTest thisInstance = this;
	private TypingTestLeaderboard submissions = new TypingTestLeaderboard();
	
	private final static String TEST_PROMPTS_FILEPATH = BotConfig.BOT_DATA_FILEPATH + "TypingPrompts/";
	private final static String ZERO_WIDTH_NON_JOINER = "‌";
	private final static short WPM_EASY = 30;
	private final static short WPM_MEDIUM = 60;
	private final static short WPM_HARD = 90;
	private final static short WPM_DIABOLICAL = 120;
	private final static short NUM_CHARS_IN_WORD = 5;
	
	private ScheduledExecutorService schedulePool = Executors.newSingleThreadScheduledExecutor();
	private Future<?> scheduledStop;
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
						.sorted((a,b)->Double.compare(submissions.getSubmission(b).getTypingPoints(), submissions.getSubmission(a).getTypingPoints()))
						.collect(Collectors.toList());
				
				for (int i = 0; i < submissions.getNumSubmissions(); i++)
				{
					TypingSubmission s = submissions.getSubmission(lbOrder.get(i));
					embed.addField(
							String.format("#%d.) %s", i+1, s.getUserTag()),
							String.format(
									"TP: **`%.2f`**%n"
									+ "WPM: **`%.2f`**%n"
									+ "Accuracy: **`%.2f`**%%",
									s.getTypingPoints(), s.getWPM(), s.getAccuracy()),
							false);
					Database.addTest(s.getUserID(), s.getWPM(), s.getAccuracy(), s.getTypingPoints());
				}
				message.replyEmbeds(embed.build()).queue();
				Typing.guildTestList.remove(event.getGuild().getIdLong());
		}
	};
	
	public TypingTest(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.channel = event.getChannel();
		this.args = args;
		this.message = event.getMessage();
	}
	
	@Override
	public void run() throws NumberFormatException
	{
		Set<String> difficulties = new HashSet<>();
		{
			difficulties.add("easy");
			difficulties.add("medium");
			difficulties.add("hard");
			difficulties.add("diabolical");
		}
		
		String difficulty;
		if ( (args.length == 1) || !difficulties.contains(args[1].toLowerCase()) ) {difficulty = "easy";}
		else {difficulty = args[1].toLowerCase();}
		
		constructAndSendTest(difficulty);
		event.getJDA().addEventListener(this);
	}
	
	private void constructAndSendTest(String difficulty)
	{
		int promptNumber = (int) (BotConfig.NUM_PROMPTS*Math.random() + 1);
		promptRating = BotConfig.promptRatingMap.get(promptNumber);
		
		try (BufferedReader reader = new BufferedReader(new FileReader(String.format("%sprompt%d.txt", TEST_PROMPTS_FILEPATH, promptNumber)));)
		{
			prompt = reader.readLine();
			numChars = prompt.length();
			
			// Sets ending time based on difficulty.
			long endTime = System.currentTimeMillis() / 1000;
			switch(difficulty)
			{
			case "easy":
				endTime += (long) (60*numChars / (WPM_EASY * NUM_CHARS_IN_WORD));
				break;
			case "medium":
				endTime += (long) (60*numChars / (WPM_MEDIUM * NUM_CHARS_IN_WORD));
				break;
			case "hard":
				endTime += (long) (60*numChars / (WPM_HARD * NUM_CHARS_IN_WORD));
				break;
			case "diabolical":
				endTime += (long) (60*numChars / (WPM_DIABOLICAL * NUM_CHARS_IN_WORD));
				break;
			}
			
			// Sends typing test.
			fakePrompt = prompt.substring(0, prompt.length()/2) + ZERO_WIDTH_NON_JOINER + prompt.substring(prompt.length()/2, prompt.length());
			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("Typing Prompt:")
					.setDescription(fakePrompt)
					.addField("Time", String.format("Test will end <t:%d:R>.", endTime), false);
			channel.sendMessageEmbeds(embed.build()).queue();
			
			
			// Makes sure the typing test finishes on time.
			long delay = endTime*1000 - System.currentTimeMillis();
			scheduledStop = schedulePool.schedule(concludeTest, delay, TimeUnit.MILLISECONDS);
		}
		catch (IOException e)
		{
			channel.sendMessageEmbeds(new EmbedBuilder()
					.addField("Error", "Internal error — contact developer", false).build()).queue();
		}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		// Sets starting time when the message has been sent. TODO: Make a better system for this.
		long userID = event.getAuthor().getIdLong();
		MessageChannel answerChannel = event.getChannel();
		
		if (answerChannel.getIdLong() != channel.getIdLong()) {return;}
		else if (userID == BotConfig.BOT_USER_ID && startTime == -1) {startTime = System.currentTimeMillis(); return;}
		else if (submissions.getUserIDs().contains(userID)) {return;}
		else if (event.getAuthor().isBot()) {return;}
		
		// Definitions.
		String userTypingSubmission = event.getMessage().getContentRaw();
		String userTag = event.getAuthor().getAsTag();
		
		// If Cheated...
		if (userTypingSubmission.contains(ZERO_WIDTH_NON_JOINER))
		{event.getMessage().replyFormat("Cheater detected. -1 Rep.").queue(); return;}
		
		// Metrics calculations.
		double timeTakenMillis = (System.currentTimeMillis()) - startTime;
		double wordsPerMinute = (numChars / timeTakenMillis) * 12000;
		int editDistance = new LevenshteinDistance().apply(prompt, userTypingSubmission);
		double accuracy = 100* (double)(prompt.length() - editDistance) / (double)prompt.length();
		double typingPoints = (wordsPerMinute * promptRating) * Math.pow(0.9, 100-accuracy);
		
		
		// If SS...
		if (prompt.equals(userTypingSubmission) && wordsPerMinute < 200)
			{sendResult(event.getChannel(), new TypingSubmission(userID, userTag, wordsPerMinute, 100.0, typingPoints), timeTakenMillis); return;}
		
		if (accuracy < 75.0) {return;} // Doesn't accept accuracy below 75%.
		if (wordsPerMinute >= 250.0) {event.getMessage().replyFormat("Cheater detected. -1 Rep.").queue(); return;}
		
		sendResult(event.getChannel(), new TypingSubmission(userID, userTag, wordsPerMinute, accuracy, typingPoints), timeTakenMillis);
	}
	
	private void sendResult(MessageChannel channel, TypingSubmission submission, double timeTakenMillis)
	{
		submissions.addSubmission(submission);
		
		channel.sendMessage(
				String.format("<@%s> has completed the prompt in **`%.3f` seconds `[%.2f WPM]`**, "
						+ "with an accuracy of **`%.2f%%`**.%nTyping Points: **`%.2f TP`**.",
				submission.getUserID(), timeTakenMillis/1000, submission.getWPM(), submission.getAccuracy(), submission.getTypingPoints()))
				.queue();
	}
	
	public void quitTest()
	{
		scheduledStop.cancel(true);
		concludeTest.run();
	}
}
