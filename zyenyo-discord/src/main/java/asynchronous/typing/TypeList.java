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
	
	private int pageNumber = 1;
	private String searchString = "";
	
	public TypeList(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.args = args;
	}
	
	
	@Override
	public void run()
	{
		event.getChannel().sendTyping().queue();
		
		parseArguments();
		
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
	
	
	/**
	 * Parses command arguments from args[].
	 */
	private void parseArguments()
	{
		for (int i = 0; i < args.length; i++)
		{
			String cmd = args[i];
			
			switch(cmd)
			{
			case "-page": case "-p":
				try
				{
					int pageNumberInt = Integer.parseInt(args[i+1]);
					if (pageNumberInt > NUM_PAGES) {continue;}
					if (pageNumberInt <= 0) {continue;}
					pageNumber = pageNumberInt;
				}
				catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {e.printStackTrace();}
				break;
			case "-search": case "-s":
				try
				{
					if (!searchString.isBlank()) {continue;}
					searchString = args[i+1];
				}
				catch(ArrayIndexOutOfBoundsException e) {e.printStackTrace();}
				break;
			}
		}
	}
}
