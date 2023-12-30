package asynchronous.typing;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import dataStructures.LongestCommonSubstring;
import dataStructures.PromptHeadings;
import dataStructures.StringSimilarityPair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.BotConfig;

public class TypeList implements Runnable
{
	private static final int NUM_PAGES = (BotConfig.NUM_PROMPTS / 10) + 1;
	private MessageReceivedEvent event;
	private String[] args;
	
	private int pageNumber = 1;
	private int numResults = 10;
	private String searchString = "";
	private EmbedBuilder embed = new EmbedBuilder();
	
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
		
		// Search.
		int promptOffset = numResults * (pageNumber-1);
		Map<String, String> searchResults = searchPrompts(promptOffset);
		embed.setFooter(String.format("Page %d of %d", pageNumber, NUM_PAGES));
		
		// Add results to embed.
		for (Map.Entry<String, String> entry : searchResults.entrySet())
		{
			embed.addField(
					entry.getKey(),
					entry.getValue(),
					false
					);
		}
		
		event.getChannel().sendMessageEmbeds(embed.build()).queue();
	}
	
	
	/**
	 * Parses command arguments from args[].
	 */
	private void parseArguments()
	{
		List<String> commandAliases = List.of("-page", "-p", "-search", "-s");
		
		for (int i = 0; i < args.length; i++)
		{
			String cmd = args[i];
			
			switch(cmd)
			{
			case "-page": case "-p":
				try
				{
					if (args.length-1 == i) {continue;}

					int pageNumberInt = Integer.parseInt(args[i+1]);
					if (pageNumberInt > NUM_PAGES) {continue;}
					if (pageNumberInt <= 0) {continue;}

					pageNumber = pageNumberInt;
				}
				catch(NumberFormatException e) {e.printStackTrace();}
				break;
			case "-search": case "-s":
				if (!searchString.isBlank()) {continue;}
				if (args.length-1 == i) {continue;}
				
				for (int j = i+1; j < args.length; j++)
				{
					if (commandAliases.contains(args[j])) {break;}
					searchString += args[j] + " ";
				}
				searchString = searchString.strip();
				break;
			}
		}
	}
	
	
	private Map<String, String> searchPrompts(int promptOffset)
	{
		Map<String, String> searchResults = new LinkedHashMap<>(numResults);
		
		if (!searchString.isBlank())
		{
			embed.setTitle(String.format("Returning %d most relevant results for \"%s\"",
					numResults,
					searchString));
			//search and fill map; account for page number.
			PriorityQueue<StringSimilarityPair> relevantResults = new PriorityQueue<>(
					Comparator.comparingDouble(p -> -p.similarityScore));
			
			for (Map.Entry<Integer, String> entry : BotConfig.promptMap.entrySet())
			{
				String promptTitleAndBody = PromptHeadings.get(entry.getKey()) + " " + BotConfig.promptMap.get(entry.getKey());
				double similarityScore = (double)(LongestCommonSubstring.find(searchString, promptTitleAndBody).length())
						/ (double)(promptTitleAndBody.length());

				relevantResults.offer(new StringSimilarityPair(entry.getKey(), similarityScore));
			}
			
			//account for page num later
			for (int i = 0; i < promptOffset; i++) {relevantResults.poll();}
			for (int i = 0; i < numResults; i++)
			{
				
				StringSimilarityPair s = relevantResults.poll();
				searchResults.put(
						String.format(
								"[%d] %s",
								s.promptId,
								PromptHeadings.get(s.promptId)),
						BotConfig.promptMap.get(s.promptId).substring(0, 150) + "..."
						);
			}
		}
		else
		{
			embed.setTitle("Prompts List");
			for (int i = 0; i < numResults; i++)
			{
				searchResults.put(
						String.format(
								"[%d] %s",
								i + promptOffset,
								PromptHeadings.get(i + promptOffset)),
						BotConfig.promptMap.get(i + promptOffset).substring(0, 150));
			}
		}
		
		return searchResults;
	}
}