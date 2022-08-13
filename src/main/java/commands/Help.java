package commands;

import dataStructures.Aliases;
import dataStructures.InfoCard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Help extends ListenerAdapter
{
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		// Gets server, channel, and message data.
		MessageChannel channel = event.getChannel();
		Message message = event.getMessage();
		String content = message.getContentRaw();
		String[] args = content.split("\\s+");
		EmbedBuilder embed = new EmbedBuilder();
		
		if (Aliases.HELP.contains(args[0].toLowerCase()))
		{
			if (args.length >= 2)
			{
				if (args.length == 3 && args[2].equalsIgnoreCase("help"))
					{channel.sendMessage("Oh dear, you seem to be having quite the \"help\" overload.").queue(); return;}
				
				switch(args[1].toLowerCase())
				{
				case "help":
					embed = InfoCard.HelpEasterEgg(embed);
					break;
				case "information": case "info":
					embed = InfoCard.InfoHelp(embed);
					break;
				case "mesticsscrape": case "msscrape": case "scrape":
					embed = InfoCard.MesticsScrapeHelp(embed);
					break;
				case "ping":
					embed = InfoCard.PingHelp(embed);
					break;
				case "mesticsread": case "msread": case "read":
					embed = InfoCard.MesticsReadHelp(embed);
				}
			}
			else {embed = InfoCard.FullHelp(embed);}
			
			channel.sendTyping().queue();
			try {channel.sendMessageEmbeds(embed.build()).queue();}
			catch (IllegalStateException e) {channel.sendMessageEmbeds(InfoCard.CommandNotFound(embed, args[1]).build()).queue();}
		}
	}
}