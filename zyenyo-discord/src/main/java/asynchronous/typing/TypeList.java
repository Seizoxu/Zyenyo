package asynchronous.typing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import dataStructures.LongestCommonSubstring;
import dataStructures.Prompt;
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
	
	private static final int NUM_RESULTS = 10;
	private EmbedBuilder embed = new EmbedBuilder();
	private String embedTitle = "";
	
	private String argSearchString = "";
	private int argPageNumber = 1;
	private int argTargetLength = -1;
	private double argTargetTr = -1;
	private int totalPages = NUM_PAGES;
	private List<Prompt> filteredResults = new ArrayList<>(BotConfig.promptList);
	
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
		filterPrompts();
		
		if (filteredResults.size() == 0)
		{
			event.getChannel().sendMessage("Nothing found.").queue();
			return;
		}
		
		for (Prompt prompt : filteredResults)
		{
			embed.addField(
					String.format(
							"`[#%d | %.2fTR]` %s",
							prompt.number(),
							prompt.typeRating(),
							prompt.title()),
					prompt.body().substring(0, 150) + "...",
					false);
		}
		
		embed.setFooter(String.format("Page %d of %d", argPageNumber, totalPages));
		embed.setTitle((embedTitle.isEmpty())
				? "Prompts List"
				: embedTitle);
		
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
	 * @return An n=10 List of filtered/sorted prompts.
	 */
	private void filterPrompts()
	{
		trFilter();
		lengthFilter();
		searchFilter();
		pageFilter();
	}
	
	
	private void searchFilter()
	{
		if (argSearchString.isBlank()) {return;}
		
		embedTitle += String.format("Returning %d most relevant results for \"%s\"%n", NUM_RESULTS, argSearchString);
		
		PriorityQueue<StringSimilarityPair> relevantResults = new PriorityQueue<>(
				Comparator.comparingDouble(p -> -p.similarityScore));
		for (Prompt prompt : filteredResults)
		{
			String promptTitleAndBody = prompt.title() + " " + prompt.body();
			double similarityScore = (double)(LongestCommonSubstring.find(argSearchString, promptTitleAndBody).length())
					/ (double)(promptTitleAndBody.length());

			relevantResults.offer(new StringSimilarityPair(prompt.number(), similarityScore));
		}
		
		filteredResults = new ArrayList<Prompt>(relevantResults.size());
		StringSimilarityPair s;
		while((s = relevantResults.poll()) != null)
		{
			Prompt prompt = BotConfig.promptList.get(s.id);
			filteredResults.add(prompt);
		}
	}
	
	
	private void trFilter()
	{
		if (argTargetTr == -1) {return;}
		
		double lowerTrBound = argTargetTr - 0.1;
		double upperTrBound = argTargetTr + 0.1;
		
		filteredResults.removeIf(x -> (x.typeRating() < lowerTrBound || x.typeRating() > upperTrBound));
	}
	
	
	private void lengthFilter()
	{
		if (argTargetLength == -1) {return;}
		
		int lowerLengthBound = argTargetLength - 100;
		int upperLengthBound = argTargetLength + 100;
		
		filteredResults.removeIf(x -> (x.length() < lowerLengthBound || x.length() > upperLengthBound));
	}
	
	
	private void pageFilter()
	{
		totalPages = filteredResults.size()/NUM_RESULTS + 1;
		argPageNumber = (totalPages < argPageNumber) ? 1 : argPageNumber;
		int startPointer = NUM_RESULTS * (argPageNumber-1);
		int endPointer = Math.min(startPointer + 10, filteredResults.size());
		
		filteredResults = filteredResults.subList(startPointer, endPointer);
	}
}