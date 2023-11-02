package asynchronous.typing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import dataStructures.PromptHeadings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.BotConfig;

public class TypeList implements Runnable
{
	private static final int NUM_PAGES = (BotConfig.NUM_PROMPTS / 10) + 1;
	private static final String TEST_PROMPTS_FILEPATH = "ZBotData/TypingPrompts/";
	private MessageReceivedEvent event;
	private String[] args;
	
	public TypeList(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.args = args;
	}
	
	
	@Override
	public void run()
	{
		event.getChannel().sendTyping();
		
		int pageNumber = parsePageNumber(args, 1);
		int promptOffset = 10 * (pageNumber-1);
		EmbedBuilder embed = new EmbedBuilder()
				.setTitle("Prompts List")
				.setFooter(String.format("Page %d of %d", pageNumber, NUM_PAGES));
		
		for (int i = 0; i < 10; i++)
		{
			try (BufferedReader reader = new BufferedReader(new FileReader(
					String.format("%sprompt%d.txt", TEST_PROMPTS_FILEPATH, i + promptOffset)));)
			{
				embed.addField(
						String.format("[%d] %s", i + promptOffset, PromptHeadings.get(i + promptOffset)),
						reader.readLine().substring(0, 150) + "...",
						false
						);
			}
			catch (IndexOutOfBoundsException | IOException e) {}
		}
		
		event.getChannel().sendMessageEmbeds(embed.build()).queue();
	}
	
	
	private int parsePageNumber(String[] args, int argumentIndex)
	{
		try
		{
			int pageNumberInt = Integer.parseInt(args[argumentIndex]);

			if (pageNumberInt > NUM_PAGES) {return 1;}
			return pageNumberInt;
		}
		catch(NumberFormatException | ArrayIndexOutOfBoundsException e)
		{
			return 1;
		}
	}
}
