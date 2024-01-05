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

/**
 * Constructs and returns a list of prompts.
 */
public class TypeList implements Runnable
{
	private static final int NUM_PAGES = (BotConfig.NUM_PROMPTS / 10) + 1;
	private MessageReceivedEvent event;
	private String[] args;
	
	private int numResults = 10;
	private EmbedBuilder embed = new EmbedBuilder();
	
	private String argSearchString = "";
	private int argPageNumber = 1;
	private int argTargetLength = -1;
	private double argTargetTr = -1;
	
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
		int promptOffset = numResults * (argPageNumber-1);
		Map<String, String> searchResults = searchPrompts(promptOffset);
		embed.setFooter(String.format("Page %d of %d", argPageNumber, NUM_PAGES));
		
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
		List<String> commandAliases = List.of(
				"-page", "-p",
				"-search", "-s",
				"-typerating", "-tr",
				"-length", "-l");
		
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
					if (pageNumberInt > NUM_PAGES || pageNumberInt <= 0) {continue;}

					argPageNumber = pageNumberInt;
				}
				catch(NumberFormatException e) {e.printStackTrace();}
				break;
			case "-length": case "-l":
				try
				{
					if (args.length-1 == i) {continue;}
					
					int length = Integer.parseInt(args[i+1]);
					if (length <= 0 || length > 5000) {continue;}
					
					argTargetLength = length;
				}
				catch(NumberFormatException e) {e.printStackTrace();}
				break;
			case "-typerating": case "-tr":
				try
				{
					if (args.length-1 == i) {continue;}
					
					double typeRating = Double.parseDouble(args[i+1]);
					if (typeRating <= 0 || typeRating > 3) {continue;}
					
					argTargetTr = typeRating;
				}
				catch(NumberFormatException e) {e.printStackTrace();}
				break;
			case "-search": case "-s":
				if (!argSearchString.isBlank()) {continue;}
				if (args.length-1 == i) {continue;}
				
				for (int j = i+1; j < args.length; j++)
				{
					if (commandAliases.contains(args[j])) {break;}
					argSearchString += args[j] + " ";
				}
				argSearchString = argSearchString.strip();
				break;
			}
		}
	}
	
	
	/**
	 * Searches through the prompts for the search string.
	 * @param promptOffset
	 * @return
	 */
	private Map<String, String> searchPrompts(int promptOffset)
	{
		Map<String, String> searchResults = new LinkedHashMap<>(numResults);
		PriorityQueue<StringSimilarityPair> relevantResults = new PriorityQueue<>(
				Comparator.comparingDouble(p -> -p.similarityScore));
		embed.setTitle("Prompts List");
		
		if (!argSearchString.isBlank()) // if searching for something...
		{
			embed.setTitle(String.format("Returning %d most relevant results for \"%s\"",
					numResults,
					argSearchString));
			
			
			for (Map.Entry<Integer, String> entry : BotConfig.promptMap.entrySet())
			{
				String promptTitleAndBody = PromptHeadings.get(entry.getKey()) + " " + BotConfig.promptMap.get(entry.getKey());
				double similarityScore = (double)(LongestCommonSubstring.find(argSearchString, promptTitleAndBody).length())
						/ (double)(promptTitleAndBody.length());

				relevantResults.offer(new StringSimilarityPair(entry.getKey(), similarityScore));
			}
			
			// Skip pages.
//			for (int i = 0; i < promptOffset; i++) {relevantResults.poll();}
//			for (int i = 0; i < numResults; i++)
//			{
//				
//				StringSimilarityPair s = relevantResults.poll();
//				searchResults.put(
//						String.format(
//								"`[#%d | %.2fTR]` %s",
//								s.promptId,
//								BotConfig.promptRatingMap.get(i + promptOffset),
//								PromptHeadings.get(s.promptId)),
//						BotConfig.promptMap.get(s.promptId).substring(0, 150) + "..."
//						);
//			}
		}
		else
		{
//			for (int i = 0; i < numResults; i++)
//			{
//				searchResults.put(
//						String.format(
//								"`[#%d | %.2fTR]` %s",
//								i + promptOffset,
//								BotConfig.promptRatingMap.get(i + promptOffset),
//								PromptHeadings.get(i + promptOffset)),
//						BotConfig.promptMap.get(i + promptOffset).substring(0, 150) + "..."
//						);
//			}
		}
		
		
		
		
		return searchResults;
	}
}