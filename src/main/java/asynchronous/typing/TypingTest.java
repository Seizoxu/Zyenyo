package asynchronous.typing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import dataStructures.PromptHeadings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.BotConfig;

public class TypingTest extends TypingTestTemplate
{
	private final static Set<String> DIFFICULTIES = Set.of("easy", "medium", "hard", "diabolical");
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
		String difficulty;
		if ( (args.length == 1) || !DIFFICULTIES.contains(args[1].toLowerCase()) ) {difficulty = "none";}
		else {difficulty = args[1].toLowerCase();}

		constructAndSendTest(difficulty);
		event.getJDA().addEventListener(this);
	}

	protected void constructAndSendTest(String difficulty)
	{
		// Sets a random prompt number, based on the difficulty.
		int promptNumber = 1;
		Random r = ThreadLocalRandom.current();
		
		switch(difficulty) // Gets prompt number from the appropriate ArrayList in promptDifficultyList
		{
		case "none":
			promptNumber = Math.abs(r.nextInt()) % BotConfig.NUM_PROMPTS;
			break;
		case "easy":
			promptNumber = BotConfig.promptDifficultyList.get(0).get(Math.abs(r.nextInt()) % NUM_PROMPTS_EASY);
			break;
		case "medium":
			promptNumber = BotConfig.promptDifficultyList.get(1).get(Math.abs(r.nextInt()) % NUM_PROMPTS_MEDIUM);
			break;
		case "hard":
			promptNumber = BotConfig.promptDifficultyList.get(2).get(Math.abs(r.nextInt()) % NUM_PROMPTS_HARD);
			break;
		case "diabolical":
			promptNumber = BotConfig.promptDifficultyList.get(3).get(Math.abs(r.nextInt()) % NUM_PROMPTS_DIABOLICAL);
			break;
		}
		
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

			promptTitle = PromptHeadings.get(promptNumber-1);
			
			EmbedBuilder embed = new EmbedBuilder()
					.setTitle(String.format("%s", promptTitle))
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
}
