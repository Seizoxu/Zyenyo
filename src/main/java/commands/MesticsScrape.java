package commands;

import asynchronous.Scraper;
import dataStructures.Aliases;
import dataStructures.InfoCard;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import zyenyo.Zyenyo;

public class MesticsScrape extends ListenerAdapter
{
	private String[] args;
	private MessageReceivedEvent event;
	private Runnable sendHelp = new Runnable()
		{@Override public void run() {event.getMessage().replyEmbeds(InfoCard.INCORRECT_SYNTAX.build()).queue();}};
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if (event.getAuthor().isBot()) {return;}
		
		this.event = event;
		args = event.getMessage().getContentRaw().split("\\s+");
		
		// IF: User requests for help...
		if (args.length == 2 && args[1].equalsIgnoreCase("help")) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
		
		// IF: Command is MesticsScrape...
		if (Aliases.MESTICSSCRAPE.contains(args[0].toLowerCase()))
		{
			if (args.length != 3) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			
			Zyenyo.masterThreadPool.submit(new Scraper(event, args));
		}
	}
}