package asynchronous;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dataStructures.TypingSubmission;
import dataStructures.TypingTestLeaderboard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import zyenyo.Zyenyo;

public class TypingTest extends ListenerAdapter implements Runnable
{
	private MessageReceivedEvent event;
	private MessageChannel channel;
	private Message message;
	private String[] args;
	private String prompt;
	private int numChars;
	private long startTime = -1;
	private TypingTest thisInstance = this;
	private TypingTestLeaderboard submissions = new TypingTestLeaderboard();
	
	private final static String TEST_PROMPTS_FILEPATH = "ZBotData/TypingPrompts/";
	private final static short PROMPT_COUNT = 12;
	private final static short WPM_EASY = 30;
	private final static short WPM_MEDIUM = 60;
	private final static short WPM_HARD = 90;
	private final static short WPM_DIABOLICAL = 120;
	private final static short NUM_CHARS_IN_WORD = 5;
//	private final static 
	
	private ScheduledExecutorService schedulePool = Executors.newScheduledThreadPool(1);
	private Future<?> scheduledStop;
	private Runnable concludeTest = new Runnable()
	{
		@Override
		public void run()
		{
			try
			{
				// Print leaderboard.
				event.getJDA().removeEventListener(thisInstance);
				EmbedBuilder embed = new EmbedBuilder();
				embed.setTitle("Typing Test Results");
				
				for (int i = 1; i < submissions.getNumSubmissions() + 1; i++)
				{
					TypingSubmission s = submissions.getSubmission(i);
					embed.addField(
							String.format("#%d.) %s", i, s.getUserTag()),
							String.format("WPM: **`%.2f`**%nAccuracy: **`%.2f`**%s", s.getWPM(), s.getAccuracy(), "%."),
							false);
					TypingApiHandler.sendTest(s.getUserID(), s.getWPM(), s.getAccuracy());
				}
				message.replyEmbeds(embed.build()).queue();
				Zyenyo.isTypeTestRunning = false;
			}
			catch (IOException | InterruptedException e) {}
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
		if ( (args.length > 2)) {throw new NumberFormatException("Incorrect syntax.");}
		else if ( (args.length == 1) || !difficulties.contains(args[1].toLowerCase()) ) {difficulty = "easy";}
		else {difficulty = args[1].toLowerCase();}
		
		constructAndSendTest(difficulty);
		event.getJDA().addEventListener(this);
	}
	
	private void constructAndSendTest(String difficulty)
	{
		try
		{
			// Reads random file.
			int promptNumber = (int) (PROMPT_COUNT*Math.random()+ 1);
			BufferedReader reader = new BufferedReader(new FileReader(
					String.format("%s%s%d.txt", TEST_PROMPTS_FILEPATH, "prompt", promptNumber)));
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
			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("Typing Prompt:")
					.setDescription(prompt)
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
		if (userID == Zyenyo.BOT_USER_ID && startTime == -1) {startTime = System.currentTimeMillis(); return;}
		else if (event.getAuthor().isBot()) {return;}
		else if (answerChannel.getIdLong() != channel.getIdLong()) {return;}
		else if (submissions.getUserIDs().contains(userID)) {return;}
		
		double timeTakenMillis = (System.currentTimeMillis()) - startTime;
		double wordsPerMinute = (numChars / timeTakenMillis) * 12000;
		
		// Definitions.
		String userTypingSubmission = event.getMessage().getContentRaw();
		String[] promptWordsArray = prompt.split("\\s");
		String[] submissionWordsArray = userTypingSubmission.split("\\s");
		String userTag = event.getAuthor().getAsTag();
		
		// If SS...
		if (prompt.equals(userTypingSubmission))
		{
			sendResult(event.getChannel(), new TypingSubmission(userID, userTag, wordsPerMinute, 100.0), timeTakenMillis);
			return;
		}
		
		int correctCharacters = 0,
			totalWords = promptWordsArray.length,
			totalCharacters = prompt.toCharArray().length - (totalWords - 1),
			leastTotalWords = promptWordsArray.length;
		char[] 	currentPromptWord,
				currentSubmissionWord;
		if (submissionWordsArray.length < leastTotalWords) {leastTotalWords = submissionWordsArray.length;}
		
		// For each word...
		for (int i = 0; i < leastTotalWords; i++)
		{
			currentPromptWord = promptWordsArray[i].toCharArray();
			currentSubmissionWord = submissionWordsArray[i].toCharArray();
			
			// Get the smaller character count.
			int leastTotalCurrentChars;
			if (currentSubmissionWord.length < currentPromptWord.length)
				{leastTotalCurrentChars = currentSubmissionWord.length;}
			else
			{
				leastTotalCurrentChars = currentPromptWord.length;
				correctCharacters -= (currentSubmissionWord.length - currentPromptWord.length);
			}
			
			// Check each character.
			for (int j = 0; j < leastTotalCurrentChars; j++)
				{if (currentPromptWord[j] == currentSubmissionWord[j]) {correctCharacters++;}}
		}
		double accuracy = 100 * (double)correctCharacters / (double)totalCharacters;
		if (accuracy < 50) {return;}
		
		sendResult(event.getChannel(), new TypingSubmission(userID, userTag, wordsPerMinute, accuracy), timeTakenMillis);
	}
	
	private void sendResult(MessageChannel channel, TypingSubmission submission, double timeTakenMillis)
	{
		submissions.addSubmission(submission);
		
		channel.sendMessage(
				String.format("<@%s> has completed the prompt in **`%.3f` seconds `[%.2f WPM]`**, with an accuracy of **`%.2f%s`.**",
				submission.getUserID(), timeTakenMillis/1000, submission.getWPM(), submission.getAccuracy(), "%."))
				.queue();
	}
	
	public void quitTest()
	{
		scheduledStop.cancel(true);
		concludeTest.run();
	}
}
