package asynchronous.typing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	private record DoubleRange(double lower, double upper) {}
	private record IntegerRange(int lower, int upper) {}

	private static final int MAX_PAGES			= (BotConfig.NUM_PROMPTS / 10) + 1;
	private static final int NUM_RESULTS		= 10;
	private static final Pattern RANGE_PATTERN	= Pattern.compile("([<>]?)([0-9]+(?:\\.?[0-9]+)?)\\-?([0-9]+(?:\\.?[0-9]+)?)?");
	private String embedDescription				= "";
	private String argSearchString				= "";
	private int argPageNumber					= 1;
	private int filteredPages					= MAX_PAGES;

	private MessageReceivedEvent event;
	private String[] args;
	private IntegerRange lengthRange;
	private DoubleRange trRange;
	private List<Integer> filteredResults;

	private static final Set<String> COMMAND_ALIASES = Set.of(
			"-page", "-p",
			"-search", "-s",
			"-typerating", "-tr",
			"-length", "-l");
		
	
	public TypeList(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.args = args.clone();

		this.filteredResults = new ArrayList<>(BotConfig.promptList.size());
		filteredResults = IntStream.range(0, BotConfig.promptList.size())
				.boxed()
				.collect(Collectors.toList());
	}
	
	
	@Override
	public void run()
	{
		// Page Filter has to be final.
		if (!parseArguments()) {return;}
		trFilter();
		lengthFilter();
		searchFilter();
		pageFilter();
		
		if (filteredResults.isEmpty())
		{
			event.getChannel().sendMessage("Nothing found.").queue();
			return;
		}

		Prompt prompt;
		EmbedBuilder embed = new EmbedBuilder();
		event.getChannel().sendTyping().queue();
		for (int i : filteredResults)
		{
			prompt = BotConfig.promptList.get(i);
			embed.addField(
					String.format(
							"`[#%d | %.2fTR]` %s",
							prompt.number(),
							prompt.typeRating(),
							prompt.title()),
					prompt.body().substring(0, 150) + "...",
					false);
		}
		
		embed.setFooter(String.format("Page %d of %d", argPageNumber, filteredPages));
		embed.setTitle("Prompts List");
		embed.setDescription(embedDescription);
		
		event.getChannel().sendMessageEmbeds(embed.build()).queue();
	}
	
	
	/**
	 * Parses command arguments from args[].
	 */
	private boolean parseArguments()
	{
		for (int i = 0; i < args.length; i++)
		{
			String cmd = args[i];
			
			switch(cmd)
			{
			case "-page": case "-p":
				if (!parsePage(i++)) {return false;}
				break;
			case "-length": case "-l":
				if (!parseLength(i++)) {return false;}
				break;
			case "-typerating": case "-tr":
				if (!parseTypeRating(i++)) {return false;}
				break;
			case "-search": case "-s":
				if (!parseSearch(i++)) {return false;}
				break;
			}
		}
		
		return true;
	}
	
	
	private boolean parsePage(int index)
	{
		try
		{
			if (args.length - 1 == index) {return true;}

			int pageNumberInt = Integer.parseInt(args[index + 1]);
			if (pageNumberInt > MAX_PAGES || pageNumberInt <= 0) {return true;}

			argPageNumber = pageNumberInt;
		}
		catch(NumberFormatException e)
		{
			// TODO: need proper error handling w/ enums; Exception extensions for users, RuntimeExceptions for devs.
			event.getChannel().sendMessageFormat("[Syntax Error] | `%s` is not a valid integer.", args[index + 1]).queue();
			return false;
		}
		
		return true;
	}
	
	
	private boolean parseLength(int index)
	{
		try
		{
			if (args.length - 1 == index) {return true;}
			
			String lengthString = "";
			for (int j = index + 1; j < args.length; j++)
			{
				if (COMMAND_ALIASES.contains(args[j])) {return true;}
				lengthString += args[j];
			}
			
			DoubleRange dr = parseRange(lengthString, true);
			lengthRange = new IntegerRange((int)dr.lower(), (int)dr.upper());
		}
		catch(NumberFormatException e)
		{
			event.getChannel().sendMessageFormat("[Syntax Error] | `%s` is not a valid length range.", args[index + 1]).queue();
			return false;
		}
		
		return true;
	}
	
	
	private boolean parseTypeRating(int index)
	{
		try
		{
			if (args.length - 1 == index) {return true;}
			
			String lengthString = "";
			for (int j = index + 1; j < args.length; j++)
			{
				if (COMMAND_ALIASES.contains(args[j])) {break;}
				lengthString += args[j];
			}
			
			trRange = parseRange(lengthString, false);
		}
		catch(NumberFormatException e)
		{
			event.getChannel().sendMessageFormat("[Syntax Error] | `%s` is not a valid TypeRating range.", args[index + 1]).queue();;
			return false;
		}

		return true;
	}
	
	
	// Currently no way to return false.
	private boolean parseSearch(int index)
	{
		if (!argSearchString.isBlank()) {return true;}
		if (args.length-1 == index) {return true;}
		
		for (int j = index + 1; j < args.length; j++)
		{
			if (COMMAND_ALIASES.contains(args[j])) {break;}
			argSearchString += args[j] + " ";
		}
		
		argSearchString = argSearchString.strip();
		return true;
	}
	
	
	/**
	 * Parses the range String given.
	 * @param rangeString: The string which contains the range to be parsed (ex: ">4.5", "0.94-1.3", "<453" etc).
	 * @param isDiscrete: is a discrete value (integer), rather than continuous (float).
	 * @return
	 */
	private DoubleRange parseRange(String rangeString, boolean isDiscrete)
	{
		if (rangeString == null || rangeString.isBlank()) {return null;}
		
		final double defaultBuffer =  (isDiscrete) ? 50d : 0.1d;
		Matcher matcher = RANGE_PATTERN.matcher(rangeString);
		
		if (!matcher.matches()) {return null;}

		String sign = matcher.group(1);	// May be null or "" if not present.
		String aStr = matcher.group(2);	// Guaranteed if .matches();
		String bStr = matcher.group(3);	// May be null if no upper bound.

		double lower;
		double upper;
		
		try {lower = Double.parseDouble(aStr);}
		catch (NumberFormatException e) {return null;}

		if (">".equals(sign)) // lower : infinity
		{
			upper = Double.POSITIVE_INFINITY;
		}
		else if ("<".equals(sign)) // 0 : upper
		{
			upper = lower;
			lower = 0;
		}
		else if (bStr == null) // Single value; create a window around it.
		{
			upper = lower + defaultBuffer;
			lower = Math.max(0d, lower - defaultBuffer);
		}
		else // Full range given.
		{
			try {upper = Double.parseDouble(bStr);}
			catch (NumberFormatException e) {return null;}
			
			if (lower > upper) // In case the user inputs reverse ranges like 3-2.
			{
				double tmp = lower;
				lower = upper;
				upper = tmp;
			}
		}
		
		if (isDiscrete)
		{
			int lowInt = (int) Math.max(0, Math.floor(lower));
			int highInt = (int) Math.min(Integer.MAX_VALUE, Math.ceil(upper));
			
			return new DoubleRange(lowInt, highInt);
		}

		return new DoubleRange(lower, upper);
	}
	
	
	private void trFilter()
	{
		if (trRange == null) {return;}
		
		embedDescription += String.format("`TR Range:` %.2f-%s%n",
				trRange.lower(),
				(trRange.upper() == Double.POSITIVE_INFINITY) ? "infinity" : String.format("%.2f",trRange.upper()));
		
		filteredResults.removeIf(promptId -> (
				BotConfig.promptList.get(promptId).typeRating() < trRange.lower()
				|| BotConfig.promptList.get(promptId).typeRating() > trRange.upper()));
	}
	
	
	private void lengthFilter()
	{
		if (lengthRange == null) {return;}
		
		embedDescription += String.format("`Length Range:` %d-%s%n",
				lengthRange.lower(),
				(lengthRange.upper() == Integer.MAX_VALUE) ? "infinity" : lengthRange.upper());
		
		filteredResults.removeIf(promptId -> (
				BotConfig.promptList.get(promptId).length() < lengthRange.lower()
				|| BotConfig.promptList.get(promptId).length() > lengthRange.upper()));
	}
	
	
	private void searchFilter()
	{
		record StringSimilarityPair(int id, int stringDifference) {}

		if (argSearchString.isBlank()) {return;}
		
		embedDescription += String.format("`Search String:` %s%n", argSearchString);
		PriorityQueue<StringSimilarityPair> relevantResults = new PriorityQueue<>(Comparator.comparingInt(StringSimilarityPair::stringDifference));
		Prompt prompt;
		for (int i : filteredResults)
		{
			prompt = BotConfig.promptList.get(i);
			String promptTitleAndBody = prompt.title() + " " + prompt.body();
			int stringDifference = argSearchString.length() - 
					LongestCommonSubstring.find(argSearchString.toLowerCase(), promptTitleAndBody.toLowerCase()).length();

			if (stringDifference < argSearchString.length())
			{
				relevantResults.offer(new StringSimilarityPair(i, stringDifference));
			}
		}
		
		List<Integer> tempList = new ArrayList<>(relevantResults.size());
		StringSimilarityPair s;
		while((s = relevantResults.poll()) != null)
		{
			tempList.add(s.id());
		}
		
		// To preserve reference to original filteredResults object, instead of immediately replacing it above; for potential future refactors.
		filteredResults.clear();
		filteredResults.addAll(tempList);
	}
	
	
	private void pageFilter()
	{
		filteredPages = (int) Math.ceil(filteredResults.size() / (double)NUM_RESULTS);
		filteredPages = Math.max(filteredPages, 1);

		argPageNumber = (filteredPages < argPageNumber) ? 1 : argPageNumber;
		int startPointer = NUM_RESULTS * (argPageNumber-1);
		int endPointer = Math.min(startPointer + NUM_RESULTS, filteredResults.size());
		
		embedDescription += String.format("`Page:` %d%n", argPageNumber);
		
		filteredResults = filteredResults.subList(startPointer, endPointer);
	}
}