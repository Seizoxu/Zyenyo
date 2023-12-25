package asynchronous.messageStatistics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.BotConfig;

public class Reader implements Runnable
{
	private String[] args;
	private MessageReceivedEvent event;
	
	public Reader(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.args = args;
	}
	
	@Override
	public void run()
	{
		ObjectInputStream scrapeFileOIS = null;
		MessageChannel channel = event.getChannel();
		
		try (ObjectInputStream idFileOIS = new ObjectInputStream(new FileInputStream(BotConfig.INDEX_IDS_FILEPATH));)
		{
			int msRecallID = Integer.parseInt(args[1]);
			
			// Get ID File.
			@SuppressWarnings("unchecked")
			HashMap<Integer, String> idTable = (HashMap<Integer, String>)idFileOIS.readObject();
			
			// If ID is null...
			if (idTable.get(msRecallID) == null) {channel.sendMessageFormat("ID `%d` does not exist.", msRecallID).queue(); return;}
			
			// Get Scrape File.
			File scrapeFile = new File(String.format("%s%s.zbsf", BotConfig.SCRAPE_DATA_FILEPATH, idTable.get(msRecallID)));
			if (!scrapeFile.exists()) {channel.sendMessage("The specified scrape file no longer exists.").queue(); return;}
			scrapeFileOIS = new ObjectInputStream(new FileInputStream(scrapeFile));
			@SuppressWarnings("unchecked")
			HashMap<String, Integer> scrapeData = (HashMap<String, Integer>)scrapeFileOIS.readObject();
			
			HashMap<Long, Integer> charCounts = new HashMap<>();
			HashMap<Long, Integer> messageCounts = new HashMap<>();
			Set<String> userIDs = scrapeData.keySet();
			
			// For each user ID...
			userIDs.iterator().forEachRemaining(x ->
			{
				char[] idChars = x.toCharArray();
				if (!Character.toString(idChars[idChars.length - 1]).equals("m")) // If NOT user's message count key...
				{
					charCounts.put(Long.parseLong(x), scrapeData.get(x));
					messageCounts.put(Long.parseLong(x), scrapeData.get(x + "m"));
				}
			});
			
			// Construct character LB.
			List<Long> charLB = charCounts.keySet().stream()
			.sorted((a,b) -> Integer.compare(charCounts.get(b), charCounts.get(a)))
			.limit(20)
			.collect(Collectors.toList());
			
			String finalMessage = "Username: **`{character count, message count}`**\n\n";
			short i = 0;
			for (long id : charLB) // For every ID in the leaderboard, construct string section, and append to final string.
			{
				User user = event.getJDA().retrieveUserById(id).submit().get();
				finalMessage += String.format("%d. %s: **`{%d, %d}`**%n", ++i, user.getName(), charCounts.get(id), messageCounts.get(id));
			}
			
			channel.sendMessageFormat("Character leaderboard:%n%s", finalMessage).queue();
		}
		catch (FileNotFoundException e) {System.out.println("[Reader] File Not Found.");}
		catch (IOException e) {System.out.println("[Reader] IOException Occurred.");}
		catch (IndexOutOfBoundsException e) {System.out.println("[Reader] Out of Bounds.");}
		catch (Exception e) {System.out.println("[Reader] Unknown Exception Occurred.");}
		finally
		{
			try {if (scrapeFileOIS != null) {scrapeFileOIS.close();}}
			catch (IOException e) {e.printStackTrace();}
		}
	}
}
