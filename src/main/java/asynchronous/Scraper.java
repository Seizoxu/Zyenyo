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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.BotConfig;

public class Scraper implements Runnable
{
	private String serverName;
	private MessageChannel channelToScrape;
	private int limit;
	private String[] args;
	private MessageReceivedEvent event;
	
	public Scraper(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.args = args;
	}
	
	@Override
	@SuppressWarnings("unchecked") // Unavoidable unchecked cast.
	public void run()
	{
		ObjectOutputStream	createIndexCountsOOS = null,
							createIndexIDsOOS = null,
							indexCountsOOS = null,
							indexIDsOOS = null,
							scrapeOOS = null;
		ObjectInputStream	readIndexCountsOIS = null,
							readIndexIDsOIS = null;
		
		Hashtable<String, Integer> indexCounts;
		Hashtable<Integer, String> indexIDs;
		
		try
		{
			channelToScrape = (MessageChannel) event.getGuild().getGuildChannelById(Long.parseLong(args[1].substring(2, args[1].length() - 1)));
			serverName = event.getGuild().getName();
			limit = Integer.parseInt(args[2]);
			if (limit > 100000)
			{
				event.getMessage().replyEmbeds(new EmbedBuilder().setDescription("Limit cannot be over 100,000.").build()).queue();
				return;
			}
			CompletableFuture<List<Message>> cf = channelToScrape.getIterableHistory().takeAsync(limit);
			String location = String.format("%s - %s", serverName, channelToScrape.getName());
			int id;
		
			// If files do not exist, make and format one.
			File indexCountsFile = new File(BotConfig.INDEX_COUNTS_FILEPATH);
			File indexIDsFile = new File(BotConfig.INDEX_IDS_FILEPATH);
			if (!indexCountsFile.exists() || !indexIDsFile.exists())
			{
				new File(BotConfig.SCRAPE_DATA_FILEPATH).mkdirs();
				indexCountsFile.createNewFile();
				indexIDsFile.createNewFile();
				createIndexCountsOOS = new ObjectOutputStream(new FileOutputStream(indexCountsFile));
				createIndexIDsOOS = new ObjectOutputStream(new FileOutputStream(indexIDsFile));
				
				Hashtable<String, Integer> ht1 = new Hashtable<>();
				Hashtable<Integer, String> ht2 = new Hashtable<>();
				ht1.put("PLACEHOLDER", 0);
				ht2.put(0, "PLACEHOLDER");
				
				createIndexCountsOOS.writeObject(ht1);
				createIndexIDsOOS.writeObject(ht2);
			}
			
			// Read Hashtable objects from files.
			readIndexCountsOIS = new ObjectInputStream(new FileInputStream(indexCountsFile));
			readIndexIDsOIS = new ObjectInputStream(new FileInputStream(indexIDsFile));
			
			indexCounts = (Hashtable<String, Integer>) readIndexCountsOIS.readObject();
			indexIDs = (Hashtable<Integer, String>) readIndexIDsOIS.readObject();
			
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
			indexCountsOOS = new ObjectOutputStream(new FileOutputStream(indexCountsFile));
			indexIDsOOS = new ObjectOutputStream(new FileOutputStream(indexIDsFile));
			
			indexCountsOOS.writeObject(indexCounts);
			indexIDsOOS.writeObject(indexIDs);
			
			// Write the Scrape hashtable to the scrape file.
			// ZBSF = ZyenyoBotScrapeFile.
			File scrapeFile = new File(String.format("%s%s (%d).zbsf", BotConfig.SCRAPE_DATA_FILEPATH, location, indexCounts.get(location)));
			scrapeFile.createNewFile();
			scrapeOOS = new ObjectOutputStream(new FileOutputStream(scrapeFile));
			scrapeOOS.writeObject(data);
			
			event.getMessage().replyFormat("Done!%nScrape size: `%d messages`.%nMSRecall-ID: `%d`.",
					limit, id).queue();
			return;
		}
		catch (IOException e) {event.getChannel().sendMessage("Error: `IOException`.").queue();e.printStackTrace(); return;}
		catch (InterruptedException | ExecutionException e) {System.out.println("[Scraper] Interrupted."); return;}
		catch (ClassNotFoundException e) {System.out.println("[Scraper] Class not found."); return;}
		catch (Exception e) {System.out.println("[Scraper] Unknown Exception."); e.printStackTrace(); return;}
		finally
		{
			try
			{
				if (createIndexCountsOOS != null) {createIndexCountsOOS.close();}
				if (createIndexIDsOOS != null) {createIndexIDsOOS.close();}
				if (indexCountsOOS != null) {indexCountsOOS.close();}
				if (indexIDsOOS != null) {indexIDsOOS.close();}
				if (scrapeOOS != null) {scrapeOOS.close();}
				
				if (readIndexCountsOIS != null) {readIndexCountsOIS.close();}
				if (readIndexIDsOIS != null) {readIndexIDsOIS.close();}
			}
			catch (IOException e) {e.printStackTrace();}
		}
	}
}