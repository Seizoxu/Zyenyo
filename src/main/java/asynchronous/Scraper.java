package asynchronous;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Scraper implements Runnable
{
	// This is not a command class; it is a separate class used by the MesticsScrape command class.
	private String serverName;
	private GuildChannel channelName;
	private int limit;
	private MessageReceivedEvent event;
	
	private final String filepath = "ZBotData\\";
	private final String indexCountsName = "COUNTS.zbif"; //ZBIF = ZyenyoBotIndexFile.
	private final String indexIDsName = "IDs.zbif";
	
	
	public Scraper(String serverName, GuildChannel channelName, int limit, MessageReceivedEvent event)
	{
		this.serverName = serverName;
		this.channelName = channelName;
		this.limit = limit;
		this.event = event;
	}
	
	@SuppressWarnings("unchecked") // Unavoidable unchecked cast.
	public void run()
	{
		MessageChannel scrapeChannel = (MessageChannel) channelName;
		MessageChannel channel = event.getChannel();
		CompletableFuture<List<Message>> cf = scrapeChannel.getIterableHistory().takeAsync(limit);
		
		String location = String.format("%s - %s", serverName, channelName.getName());
		int id;
		
		try
		{
			// If files do not exist, make and format one.
			File indexCountsFile = new File(filepath + indexCountsName);
			File indexIDsFile = new File(filepath + indexIDsName);
			if (!indexCountsFile.exists())
			{
				indexCountsFile.createNewFile();
				indexIDsFile.createNewFile();
				ObjectOutputStream createIndexCountsOOS = new ObjectOutputStream(new FileOutputStream(indexCountsFile));
				ObjectOutputStream createIndexIDsOOS = new ObjectOutputStream(new FileOutputStream(indexIDsFile));
				
				Hashtable<String, Integer> ht1 = new Hashtable<>();
				Hashtable<Integer, String> ht2 = new Hashtable<>();
				ht1.put("PLACEHOLDER", 0);
				ht2.put(0, "PLACEHOLDER");
				
				createIndexCountsOOS.writeObject(ht1);
				createIndexCountsOOS.close();
				createIndexIDsOOS.writeObject(ht2);
				createIndexIDsOOS.close();
			}
			
			// Read Hashtable objects from files.
			ObjectInputStream readIndexCountsOIS = new ObjectInputStream(new FileInputStream(indexCountsFile));
			ObjectInputStream readIndexIDsOIS = new ObjectInputStream(new FileInputStream(indexIDsFile));
			
			Hashtable<String, Integer> indexCounts = new Hashtable<>();
			Hashtable<Integer, String> indexIDs = new Hashtable<>();
			indexCounts = (Hashtable<String, Integer>) readIndexCountsOIS.readObject();
			indexIDs = (Hashtable<Integer, String>) readIndexIDsOIS.readObject();
			
			readIndexCountsOIS.close();
			readIndexIDsOIS.close();
			
			// Update Index Hashtables.
			if (indexCounts.containsKey("Total")) {indexCounts.replace("Total", indexCounts.get("Total") + 1);}
			else {indexCounts.put("Total", 1);} // In case the file is fresh.
			id = indexCounts.get("Total");
			
			if (indexCounts.containsKey(location)) {indexCounts.replace(location, indexCounts.get(location) + 1);}
			else {indexCounts.put(location, 1);}
			
			
			indexIDs.put(indexCounts.get("Total"), String.format("%s (%d)", location, indexCounts.get(location)));
			
			// Get result from cf, make it a stream, filter by messages from command author, change back to list.
			List<Message> list = cf.get().stream().collect(Collectors.toList());
			
			Message m;
			String memberID;
			int charCount;
			Hashtable<String,Integer> data = new Hashtable<>();
			
			// Update Scrape Hashtables.
			for (int i = 0; i < list.size(); i++)
			{
				m = list.get(i);
				memberID = m.getAuthor().getId();
				charCount = m.getContentRaw().length();
				
				if (m.getAuthor().isBot()) {continue;}
				
				if (data.containsKey(memberID)) {data.replace(memberID, data.get(memberID) + charCount);}
				else {data.put(memberID, charCount);}
				
				if (data.containsKey(memberID + "m")) {data.replace(memberID + "m", data.get(memberID + "m") + 1);}
				else {data.put(memberID + "m", 1);}
			}
			
			// Write the updated hashtable to the Index file.
			ObjectOutputStream indexCountsOOS = new ObjectOutputStream(new FileOutputStream(indexCountsFile));
			ObjectOutputStream indexIDsOOS = new ObjectOutputStream(new FileOutputStream(indexIDsFile));
			
			indexCountsOOS.writeObject(indexCounts);
			indexIDsOOS.writeObject(indexIDs);
			indexCountsOOS.close();
			indexIDsOOS.close();
			
			// Write the Scrape hashtable to the scrape file.
			File scrapeFile = new File(String.format("%s%s (%d).zbsf", filepath, location, indexCounts.get(location))); // ZBSF = ZyenyoBotScrapeFile.
			scrapeFile.createNewFile();
			ObjectOutputStream scrapeOOS = new ObjectOutputStream(new FileOutputStream(scrapeFile));
			scrapeOOS.writeObject(data);
			scrapeOOS.close();
			
			channel.sendTyping().queue();
			channel.sendMessageFormat("Done! <@%s>%nScrape size: `%d messages`.%nMSRecall-ID: `%d`.",
					event.getAuthor().getId(), limit, id).queue();
			return;
		}
		catch (IOException e) {channel.sendMessage("Error: `IOException`.").queue();e.printStackTrace(); return;}
		catch (InterruptedException | ExecutionException e) {System.out.println("[Scraper] Interrupted."); return;}
		catch (ClassNotFoundException e) {System.out.println("[Scraper] Class not found."); return;}
		catch (Exception e) {System.out.println("[Scraper] Unknown Exception."); return;}
	}
}