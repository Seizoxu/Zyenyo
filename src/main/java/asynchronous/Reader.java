package asynchronous;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Reader implements Runnable
{
	private int msRecallID;
	private MessageReceivedEvent event;
	private JDA jda;
	
	private String idFile = "ZBotData/IDs.zbif";
	
	public Reader(int msRecallID, MessageReceivedEvent event, JDA jda)
	{
		this.msRecallID = msRecallID;
		this.event = event;
		this.jda = jda;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void run()
	{
		MessageChannel channel = event.getChannel();
		
		try
		{
			ObjectInputStream idFileOIS = new ObjectInputStream(new FileInputStream(new File(idFile)));
			Hashtable<Integer, String> idTable = (Hashtable<Integer, String>) idFileOIS.readObject();
			if (idTable.get(msRecallID) != null)
			{
				String filename = String.format("%s.zbsf", idTable.get(msRecallID));
				ObjectInputStream scrapeFileOIS = new ObjectInputStream(new FileInputStream(new File(String.format("ZBotData\\%s", filename))));
				Hashtable<String, Integer> scrapeData = (Hashtable<String, Integer>) scrapeFileOIS.readObject();
				
				Hashtable<Long, Integer> charCounts = new Hashtable<>();
				Hashtable<Long, Integer> messageCounts = new Hashtable<>();
				Set<String> userIDs = scrapeData.keySet();
				
				userIDs.iterator().forEachRemaining(x ->
				{
					char[] idChars = x.toCharArray();
					if (!Character.toString(idChars[idChars.length - 1]).equals("m"))
					{
						charCounts.put(Long.parseLong(x), scrapeData.get(x));
						messageCounts.put(Long.parseLong(x), scrapeData.get(x + "m"));
					}
				});
				
				List<Long> charLB = charCounts.keySet().stream()
				.sorted((a,b) -> Integer.compare(charCounts.get(b), charCounts.get(a)))
				.limit(20)
				.collect(Collectors.toList());
				
				String finalMessage = "Username: **`{character count, message count}`**\n\n";
				short i = 0;
				for (long id : charLB)
				{
					User user = jda.retrieveUserById(id).submit().get();
					finalMessage += String.format("%d. %s: **`{%d, %d}`**%n", ++i, user.getName(), charCounts.get(id), messageCounts.get(id));
				}
				
				channel.sendMessageFormat("Character leaderboard:%n%s", finalMessage).queue();
				scrapeFileOIS.close();
			}
			else {channel.sendMessageFormat("ID `%d` does not exist.", msRecallID).queue();}
			
			idFileOIS.close();
		}
		catch (FileNotFoundException e) {System.out.println("[Reader] File Not Found.");}
		catch (IOException e) {System.out.println("[Reader] IOException Occurred.");}
		catch (IndexOutOfBoundsException e) {System.out.println("[Reader] Out of Bounds.");}
		catch (Exception e) {System.out.println("[Reader] Unknown Exception Occurred."); e.printStackTrace();}
	}
}
