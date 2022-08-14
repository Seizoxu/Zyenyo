package commands;

import asynchronous.Reader;
import dataStructures.Aliases;
import dataStructures.InfoCard;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import zyenyo.Zyenyo;

public class MesticsRead extends ListenerAdapter
{
	private MessageReceivedEvent event;
	private Runnable sendHelp = new Runnable()
	{@Override public void run() {event.getMessage().replyEmbeds(InfoCard.INCORRECT_SYNTAX.build()).queue();}};
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		this.event = event;
		if (event.getAuthor().isBot()) {return;}
		String[] args = event.getMessage().getContentRaw().split("\\s+");
		
		// IF: User requests for help...
		if (args.length == 2 && args[1].equalsIgnoreCase("help")) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
		
		// IF: Command is MesticsRead...
		if (Aliases.MESTICSREAD.contains(args[0].toLowerCase()))
		{
			if (args.length == 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			
			Zyenyo.masterThreadPool.submit(new Reader(event, args));
		}
	}
}