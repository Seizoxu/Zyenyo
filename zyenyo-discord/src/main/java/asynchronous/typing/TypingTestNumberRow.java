package asynchronous.typing;

import java.util.concurrent.TimeUnit;

import dataStructures.Prompt;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TypingTestNumberRow extends TypingTestTemplate
{
	private final static short WPM_MINIMUM = 10;
	private final static short PROMPT_LENGTH = 40;


	public TypingTestNumberRow(MessageReceivedEvent event, String[] args)
	{
		super(event, args);
	}


	@Override
	public void run() throws NumberFormatException
	{
		constructAndSendTest("none");
		event.getJDA().addEventListener(this);
	}
	
	
	protected void constructAndSendTest(String difficulty)
	{
		//TODO: implement difficulty at a later stage		
		
		int wordLength = (int)(Math.random() * 6 + 1);

		String promptText = "";
		for (int charIdx = 0; charIdx < PROMPT_LENGTH; charIdx++)
		{
			promptText += String.valueOf((int)(Math.random() * 9));
			
			if (charIdx % wordLength == 0)
			{
				promptText += " ";
				wordLength = (int)(Math.random() * 6 + 1);
			}
		}
		prompt = new Prompt(-1, "", promptText, promptText.length(), 0);

		fakePrompt = prompt.body().substring(0, prompt.length()/2)
				+ ZERO_WIDTH_NON_JOINER
				+ prompt.body().substring(prompt.length()/2, prompt.length());
		int numChars = prompt.length();
		long endTime = (System.currentTimeMillis() / 1000) + (60*numChars / (WPM_MINIMUM * NUM_CHARS_IN_WORD));

		EmbedBuilder embed = new EmbedBuilder()
				.setTitle("Typing Prompt:")
				.setDescription(fakePrompt)
				.addField("Time", String.format("Test end time: <t:%d:R>.", endTime), false);

		channel.sendMessageEmbeds(embed.build()).complete();

		// Makes sure the typing test finishes on time.
		startTime = System.currentTimeMillis();
		long delay = endTime*1000 - startTime;
		scheduledStop = schedulePool.schedule(concludeTest, delay, TimeUnit.MILLISECONDS);
	}
}