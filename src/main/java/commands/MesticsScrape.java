package commands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import asynchronous.Scraper;
import dataStructures.Aliases;
import dataStructures.InfoCard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MesticsScrape extends ListenerAdapter
{
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if (event.getAuthor().isBot()) {return;}
		
		// Gets server, channel, and message data.
		MessageChannel channel = event.getChannel();
		Message message = event.getMessage();
		String content = message.getContentRaw();
		String[] args = content.split("\\s+"); // \s is one whitespace character, \s+ is more than one whitespace character.
		String guildName = event.getGuild().getName();
		
		
		ExecutorService pool = Executors.newCachedThreadPool();
		
		if (Aliases.MESTICSSCRAPE.contains(args[0].toLowerCase()))
		{
			// Executes the command if the message is formatted correctly.
			if (!(args.length == 1) || !(args[1].equalsIgnoreCase("help")))
			{	
				try
				{
					long channelID = Long.parseLong(args[1].substring(2, args[1].length() - 1));
					GuildChannel scrapeChannel = event.getGuild().getGuildChannelById(channelID);
					int limit = Integer.parseInt(args[2]);
					pool.submit(new Scraper(guildName, scrapeChannel, limit, event));
				}
				catch (NumberFormatException e)
				{
					EmbedBuilder syntax = InfoCard.MesticsScrapeSyntax(new EmbedBuilder());
					channel.sendMessageEmbeds(syntax.build()).queue();
				}
			}
			// Otherwise sends help.
			else
			{
				EmbedBuilder info = InfoCard.MesticsScrapeHelp(new EmbedBuilder());
				channel.sendTyping().queue();
				channel.sendMessageEmbeds(info.build()).queue();
			}
		}
	}
}