package asynchronous.typing;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import zyenyo.Database;

import commands.Typing;
import dataStructures.TypingSubmission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class NumberRowTypingTest extends TypingTest
{
	private String numberPrompt = "";
	private int numChars;
	private String fakePrompt;
	private long startTime = -1;

	private final static short WPM_MINIMUM = 10;
	private final static short NUM_CHARS_IN_WORD = 5;
	private final static short PROMPT_LENGTH = 40;
	private final static String ZERO_WIDTH_NON_JOINER = "‌";

	private ScheduledExecutorService schedulePool = Executors.newSingleThreadScheduledExecutor();

	public NumberRowTypingTest(MessageReceivedEvent event, String[] args)
	{
		super(event, args);
		String[] players = Arrays.copyOfRange(args, 1, args.length);
	}

	@Override
	public void run() throws NumberFormatException
	{
		constructAndSendTest("none");
		event.getJDA().addEventListener(this);
	}
	
	@Override
	protected void constructAndSendTest(String difficulty) {
		//TODO: implement difficulty at a later stage		
		
		int wordLength = (int)(Math.random() * 6 + 1);

		for (int charIdx = 0; charIdx < PROMPT_LENGTH; charIdx++) {
			this.numberPrompt += String.valueOf((int)(Math.random() * 9));
			if (charIdx % wordLength == 0) {
				this.numberPrompt += " ";
				wordLength = (int)(Math.random() * 6 + 1);
			}
		}



		numChars = numberPrompt.length();
		long endTime = (System.currentTimeMillis() / 1000) + (60*numChars / (WPM_MINIMUM * NUM_CHARS_IN_WORD));

		fakePrompt = numberPrompt.substring(0, numberPrompt.length()/2) + ZERO_WIDTH_NON_JOINER + numberPrompt.substring(numberPrompt.length()/2, numberPrompt.length());

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

		// If Cheated...
		if (userTypingSubmission.contains(ZERO_WIDTH_NON_JOINER))
		{event.getMessage().replyFormat("Cheater detected. What a naughty user...").queue(); return;}

		// Data
		double timeTakenMillis = (System.currentTimeMillis()) - startTime;
		double wordsPerMinute = (numChars / timeTakenMillis) * 12000;
		

		int editDistance = new LevenshteinDistance().apply(numberPrompt, userTypingSubmission);
		double accuracy = 100* (double)(numberPrompt.length() - editDistance) / (double)numberPrompt.length();

		// Metrics
		double typingPoints = 0;


		// If SS...
		if (numberPrompt.equals(userTypingSubmission) && wordsPerMinute < 200)
		{sendResult(event.getChannel(), new TypingSubmission(userID, userTag, wordsPerMinute, 100.0, typingPoints), timeTakenMillis); return;}

		if (accuracy < 75.0) {return;} // Doesn't accept accuracy below 75%.
		if (wordsPerMinute >= 250.0) {event.getMessage().replyFormat("Cheater detected. What a naughty user...").queue(); return;}

		sendResult(event.getChannel(), new TypingSubmission(userID, userTag, wordsPerMinute, accuracy, typingPoints), timeTakenMillis);
	}

	private void sendResult(MessageChannel channel, TypingSubmission submission, double timeTakenMillis)
	{
		submissions.addSubmission(submission);

		channel.sendMessage(
				String.format("<@%s> has completed the prompt in **`%.3f` seconds `[%.2f WPM]`**, "
						+ "with an accuracy of **`%.2f%%`**",
						submission.userID(), timeTakenMillis/1000, submission.wordsPerMinute(), submission.accuracy()))
		.queue();
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

			for (int i = 0; i < submissions.getNumSubmissions(); i++)
			{


				TypingSubmission s = submissions.getSubmission(lbOrder.get(i));

				embed.addField(
						String.format("#%d.) %s", i+1, s.userTag()),
						String.format(
								"TP: **`%.2f`**%n"
										+ "WPM: **`%.2f`**%n"
										+ "Accuracy: **`%.2f`**%%%n",
										s.typingPoints(), s.wordsPerMinute(), s.accuracy()),
						false);

			}
			message.replyEmbeds(embed.build()).queue();
			Typing.guildTestList.remove(event.getGuild().getIdLong());
		}
	};
}
