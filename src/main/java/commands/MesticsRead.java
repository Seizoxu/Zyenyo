package commands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import asynchronous.Reader;
import dataStructures.Aliases;
import dataStructures.InfoCard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MesticsRead extends ListenerAdapter
{
	private JDA jda;
	
	public MesticsRead(JDA api) {this.jda = api;}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if (event.getAuthor().isBot()) {return;}
		
		// Gets server, channel, and message data.
		MessageChannel channel = event.getChannel();
		Message message = event.getMessage();
		String content = message.getContentRaw();
		String[] args = content.split("\\s+");
		
		ExecutorService pool = Executors.newCachedThreadPool();
		
		if (Aliases.MESTICSREAD.contains(args[0].toLowerCase()))
		{
			// Executes the command if the message is formatted correctly.
			if (!(args.length == 1) || !(args[1].equalsIgnoreCase("help")))
			{
				try
				{pool.submit(new Reader(Integer.parseInt(args[1]), event, jda));}
				catch (NumberFormatException e)
				{channel.sendMessageEmbeds(InfoCard.MesticsReadSyntax(new EmbedBuilder()).build()).queue();}
			}
			// Otherwise sends help.
			else
			{
				EmbedBuilder info = InfoCard.MesticsReadHelp(new EmbedBuilder());
				channel.sendTyping().queue();
				channel.sendMessageEmbeds(info.build()).queue();
			}
		}
	}
}