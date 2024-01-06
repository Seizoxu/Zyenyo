package asynchronous.typing;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
		}
		
		constructAndSendTest(promptSelect, "none");
		event.getJDA().addEventListener(this);
	}

	protected void constructAndSendTest(int promptNumber, String difficulty)
	{
		promptNumber = parseDifficulty(promptNumber, difficulty);
		prompt = BotConfig.promptList.get(promptNumber);
		
		fakePrompt = prompt.body().substring(0, prompt.length()/2)
				+ ZERO_WIDTH_NON_JOINER
				+ prompt.body().substring(prompt.length()/2, prompt.length());
		long endTime = (System.currentTimeMillis() / 1000) + (60*prompt.length() / (WPM_MINIMUM * NUM_CHARS_IN_WORD));

		EmbedBuilder embed = new EmbedBuilder()
				.setTitle(String.format("[#%d | %.2fTR] %s", prompt.number(), prompt.typeRating(), prompt.title()))
				.setDescription(fakePrompt)
				.addField("Time", String.format("Test end time: <t:%d:R>.", endTime), false);
		
		/* .complete() is used here instead of .queue(), since we need to
		 * wait for the message to be sent. */
		channel.sendMessageEmbeds(embed.build()).complete();

		// Makes sure the typing test finishes on time.
		startTime = System.currentTimeMillis();
		long delay = endTime*1000 - startTime;
		scheduledStop = schedulePool.schedule(concludeTest, delay, TimeUnit.MILLISECONDS);
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
