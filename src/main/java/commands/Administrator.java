package commands;

import asynchronous.typing.AddTest;
import asynchronous.typing.AddPrompt;
import asynchronous.typing.ClearProfile;
import asynchronous.typing.FindCheaters;
import asynchronous.typing.RemoveCheaters;
import dataStructures.Aliases;
import dataStructures.InfoCard;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import zyenyo.BotConfig;
import zyenyo.Zyenyo;

public class Administrator extends ListenerAdapter
{
	private MessageChannel channel;
	private Runnable sendHelp = new Runnable()
	{@Override public void run() {channel.sendMessageEmbeds(InfoCard.INCORRECT_SYNTAX.build()).queue();}};
	
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if (event.getAuthor().isBot() || !BotConfig.ADMINISTRATOR_IDS.contains(event.getAuthor().getIdLong())) {return;}
		
		channel = event.getChannel();
		String args[] = event.getMessage().getContentRaw().split("\\s+");
		
		// IF: Command is ADDTEST...
		if (Aliases.ADDTEST.contains(args[0].toLowerCase()))
		{
			if (args.length < 4 || args.length > 5) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			
			Zyenyo.masterThreadPool.submit(new AddTest(event));
		}
		
		// IF: Command is CLEARTESTS...
		else if (Aliases.CLEARPROFILE.contains(args[0].toLowerCase()))
		{
			if (args.length != 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			
			Zyenyo.masterThreadPool.submit(new ClearProfile(event));
		}
		
		// IF: Command is FINDCHEATERS
		else if (Aliases.FINDCHEATERS.contains(args[0].toLowerCase()))
		{
			if (args.length != 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			
			Zyenyo.masterThreadPool.submit(new FindCheaters(event));
		}
		
		// IF: Command is REMOVECHEATERS...
		else if (Aliases.REMOVECHEATERS.contains(args[0].toLowerCase()))
		{
			if (args.length != 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			
			Zyenyo.masterThreadPool.submit(new RemoveCheaters(event));
		}

		// IF: Command is ADDPROMPT...
		else if (Aliases.ADDPROMPT.contains(args[0].toLowerCase()))
		{
			Zyenyo.masterThreadPool.submit(new AddPrompt(event));
		}
	
		// IF: Command is SHUTDOWN...
		else if (Aliases.SHUTDOWN.contains(args[0].toLowerCase()))
		{
			channel.sendMessageFormat("Shutting down...").queue();
			event.getJDA().shutdown();
		}
	}
}
