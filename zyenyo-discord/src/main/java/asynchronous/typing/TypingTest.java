package asynchronous.typing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import dataStructures.PromptHeadings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.BotConfig;

public class TypingTest extends TypingTestTemplate
{
//	private final static Set<String> DIFFICULTIES = Set.of("easy", "medium", "hard", "diabolical");
	private static final int NUM_PROMPTS_EASY = BotConfig.promptDifficultyList.get(0).size();
	private static final int NUM_PROMPTS_MEDIUM = BotConfig.promptDifficultyList.get(1).size();
	private static final int NUM_PROMPTS_HARD = BotConfig.promptDifficultyList.get(2).size();
	private static final int NUM_PROMPTS_DIABOLICAL = BotConfig.promptDifficultyList.get(3).size();
	private final static short WPM_MINIMUM = 30;

	public TypingTest(MessageReceivedEvent event, String[] args)
	{
		super(event, args);
	}

	@Override
	public void run() throws NumberFormatException
	{
		int promptSelect = -1;
		
		//TODO: Implement difficulty.
		if (args.length >=2)
		{
			promptSelect = parsePromptSelect(args[1]);
		
			// Second argument will be EITHER promptSelect or difficulty.
//			String difficulty = parseDifficultySelect(args[1]);
		}
		
		constructAndSendTest(promptSelect, "none");
		event.getJDA().addEventListener(this);
	}

	protected void constructAndSendTest(int promptNumber, String difficulty)
	{
		promptNumber = parseDifficulty(promptNumber, difficulty);
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

			promptTitle = PromptHeadings.get(promptNumber);						
			EmbedBuilder embed = new EmbedBuilder()
					.setTitle(String.format("%s", promptTitle))
					.setDescription(fakePrompt)
					.addField("Time", String.format("Test end time: <t:%d:R>.", endTime), false);
			
			/* .complete() is used here instead of .queue(), since we need to
			 * wait for the message to be sent. */
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
	
	
	private int parsePromptSelect(String promptSelect)
	{
		try
		{
			int promptSelectInt = Integer.parseInt(promptSelect);
			if (promptSelectInt >= BotConfig.NUM_PROMPTS)
			{
				return -1;
			}
			
			return promptSelectInt;
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}
	
	
//	private String parseDifficultySelect(String difficulty)
//	{
//		if (!DIFFICULTIES.contains(difficulty)) {return "none";}
//		
//		return difficulty.toLowerCase();
//	}
	
	
	private int parseDifficulty(int promptNumber, String difficulty)
	{
		ThreadLocalRandom r = ThreadLocalRandom.current();

		if (promptNumber == -1)
		{
			switch(difficulty)
			{
			/* The bounds here don't increment the exclusive high by 1, since NUM_PROMPTS (and others) come from .size();
			 * [0, x) should be <x if x is size.
			 * Finally, no break; statements, as it would be unreachable. */
			case "none":
				return Math.abs(r.nextInt(0, BotConfig.NUM_PROMPTS));
			case "easy":
				return BotConfig.promptDifficultyList.get(0).get(Math.abs(r.nextInt(0, NUM_PROMPTS_EASY)));
			case "medium":
				return BotConfig.promptDifficultyList.get(1).get(Math.abs(r.nextInt(0, NUM_PROMPTS_MEDIUM)));
			case "hard":
				return BotConfig.promptDifficultyList.get(2).get(Math.abs(r.nextInt(0, NUM_PROMPTS_HARD)));
			case "diabolical":
				return BotConfig.promptDifficultyList.get(3).get(Math.abs(r.nextInt(0, NUM_PROMPTS_DIABOLICAL)));
			}
		}
		
		return promptNumber;
	}
}