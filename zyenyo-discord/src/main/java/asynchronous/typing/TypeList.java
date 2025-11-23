package asynchronous.typing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dataStructures.LongestCommonSubstring;
import dataStructures.Prompt;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.BotConfig;

/**
 * Constructs and returns a list of prompts. Optionally searches by string, filters by TR and length, and paginates.
 */
public class TypeList implements Runnable
{
	private record DoubleRange(double lowerBound, double upperBound) {}
	private record IntegerRange(int lowerBound, int upperBound) {}

	private static final int NUM_PAGES		= (BotConfig.NUM_PROMPTS / 10) + 1;
	private static final int NUM_RESULTS	= 10;
	private EmbedBuilder embed				= new EmbedBuilder();
	private String embedDescription			= "";
	private String argSearchString			= "";
	private int argPageNumber				= 1;
	private int totalPages					= NUM_PAGES;
	private List<Prompt> filteredResults	= new ArrayList<>(BotConfig.promptList); // probably highly inefficient.

	private MessageReceivedEvent event;
	private String[] args;
	private IntegerRange lengthRange;
	private DoubleRange trRange;
	
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

		// Page Filter has to be final.
		trFilter();
		lengthFilter();
		searchFilter();
		pageFilter();
		
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
		embed.setTitle("Prompts List");
		embed.setDescription(embedDescription);
		
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
				catch(NumberFormatException e)
				{
					// TODO: need proper error handling w/ enums; Exception extensions for users, RuntimeExceptions for devs.
					event.getChannel().sendMessageFormat("[Syntax Error] | %s is not a valid integer.", args[i+1]);
					return;
				}
				break;
			case "-length": case "-l":
				try
				{
					if (args.length-1 == i) {continue;}
					
					String lengthString = "";
					for (int j = i+1; j < args.length; j++)
					{
						if (commandAliases.contains(args[j])) {break;}
						lengthString += args[j];
					}
					
					DoubleRange dr = parseRange(lengthString, true);
					lengthRange = new IntegerRange((int)dr.lowerBound(), (int)dr.upperBound());
				}
				catch(NumberFormatException e) {e.printStackTrace();}
				break;
			case "-typerating": case "-tr":
				try
				{
					if (args.length-1 == i) {continue;}
					
					String lengthString = "";
					for (int j = i+1; j < args.length; j++)
					{
						if (commandAliases.contains(args[j])) {break;}
						lengthString += args[j];
					}
					
					trRange = parseRange(lengthString, false);
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
	 * Parses the range String given.
	 * @param rangeString: The string which contains the range to be parsed (ex: ">4.5", "0.94-1.3", "<453" etc).
	 * @param isDiscrete: is a discrete value (integer), rather than continuous (float).
	 * @return
	 */
	private DoubleRange parseRange(String rangeString, boolean isDiscrete)
	{
		if (rangeString.isBlank()) {return null;}
		
		double addValue =  (isDiscrete) ? 50d : 0.1d;
		Pattern pattern = Pattern.compile("([<>]?)([0-9]+(?:\\.?[0-9]+)?)\\-?([0-9]+(?:\\.?[0-9]+)?)?");
		Matcher matcher = pattern.matcher(rangeString);
		
		if (matcher.matches())
		{
			String sign = matcher.group(1);
			double lowerBound = Optional.ofNullable(matcher.group(2)).map(Double::parseDouble).orElse(-1d);
			double upperBound = Optional.ofNullable(matcher.group(3)).map(Double::parseDouble).orElse(-1d);
			
			if (lowerBound == -1d) {return null;}
			
			if (sign.equals(">"))
			{
				upperBound = Double.POSITIVE_INFINITY;
			}
			else if (sign.equals("<"))
			{
				upperBound = lowerBound;
				lowerBound = 0;
			}
			
			// upperBound would have changed for signs, so we can ask again here, in cases of single values.
			if (upperBound == -1d)
			{
				upperBound = lowerBound + addValue;
				lowerBound = lowerBound - addValue;
			}
			return new DoubleRange(lowerBound, upperBound);
		}
		
		return null;
	}
	
	
	private void searchFilter()
	{
		record StringSimilarityPair(int id, double similarityScore) {}

		if (argSearchString.isBlank()) {return;}
		
		embedDescription += String.format("`Search String:` %s%n", argSearchString);
		PriorityQueue<StringSimilarityPair> relevantResults = new PriorityQueue<>(Comparator.comparingDouble(p -> -p.similarityScore()));
		for (Prompt prompt : filteredResults)
		{
			String promptTitleAndBody = prompt.title() + " " + prompt.body();
			double similarityScore = (double)
					LongestCommonSubstring.find(argSearchString.toLowerCase(), promptTitleAndBody.toLowerCase()).length()
					/ argSearchString.length();

			if (similarityScore > 0)
			{
				relevantResults.offer(new StringSimilarityPair(prompt.number(), similarityScore));
			}
		}
		
		filteredResults = new ArrayList<Prompt>(relevantResults.size());
		StringSimilarityPair s;
		while((s = relevantResults.poll()) != null)
		{
			Prompt prompt = BotConfig.promptList.get(s.id());
			filteredResults.add(prompt);
		}
	}
	
	
	private void trFilter()
	{
		if (trRange == null) {return;}
		
		embedDescription += String.format("`TR Range:` %.2f-%s%n",
				trRange.lowerBound(),
				(trRange.upperBound() == Double.POSITIVE_INFINITY) ? "infinity" : String.format("%.2f",trRange.upperBound()));
		
		filteredResults.removeIf(x -> (x.typeRating() < trRange.lowerBound() || x.typeRating() > trRange.upperBound()));
	}
	
	
	private void lengthFilter()
	{
		if (lengthRange == null) {return;}
		
		embedDescription += String.format("`Length Range:` %d-%s%n",
				lengthRange.lowerBound(),
				(lengthRange.upperBound() == Integer.MAX_VALUE) ? "infinity" : lengthRange.upperBound());
		
		filteredResults.removeIf(x -> (x.length() < lengthRange.lowerBound() || x.length() > lengthRange.upperBound()));
	}
	
	
	private void pageFilter()
	{
		totalPages = filteredResults.size()/NUM_RESULTS + 1;
		argPageNumber = (totalPages < argPageNumber) ? 1 : argPageNumber;
		int startPointer = NUM_RESULTS * (argPageNumber-1);
		int endPointer = Math.min(startPointer + 10, filteredResults.size());
		
		embedDescription += String.format("`Page:` %d%n", argPageNumber);
		
		filteredResults = filteredResults.subList(startPointer, endPointer);
	}
}