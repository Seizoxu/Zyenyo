package commands;

import asynchronous.messageStatistics.Reader;
import asynchronous.messageStatistics.Scraper;
import dataStructures.Aliases;
import dataStructures.InfoCard;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import zyenyo.Zyenyo;

public class MessageStatistics extends ListenerAdapter
{
	private MessageReceivedEvent event;
	private Runnable sendHelp = new Runnable()
		{@Override public void run() {event.getMessage().replyEmbeds(InfoCard.INCORRECT_SYNTAX.build()).queue();}};
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if (event.getAuthor().isBot()) {return;}
		this.event = event;
		String[] args = event.getMessage().getContentRaw().split("\\s+");
		
		// IF: Command is MesticsRead...
		if (Aliases.MESTICSREAD.contains(args[0].toLowerCase()))
		{
			if (args.length == 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			
			Zyenyo.masterThreadPool.submit(new Reader(event, args));
		}
		
		// IF: Command is MesticsScrape...
		else if (Aliases.MESTICSSCRAPE.contains(args[0].toLowerCase()))
		{
			if (args.length != 3) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			
			Zyenyo.masterThreadPool.submit(new Scraper(event, args));
		}
	}
}