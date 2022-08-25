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
					{channel.sendMessage("Oh dear, you seem to be having a \"help\" overload.").queue(); return;}
				
				switch(args[1].toLowerCase())
				{
				case "help":
					embed = InfoCard.HELP_EASTER_EGG;
					break;
				case "information": case "info":
					embed = InfoCard.INFO;
					break;
				case "mesticsscrape": case "msscrape": case "scrape":
					embed = InfoCard.MESTICS_SCRAPE;
					break;
				case "ping":
					embed = InfoCard.PING;
					break;
				case "mesticsread": case "msread": case "read":
					embed = InfoCard.MESTICS_READ;
				case "typestart": case "typetest": case "ttest": case "tt":
					embed = InfoCard.TYPING_TEST;
				case "typestop": case "typequit": case "tquit": case "tq":
					embed = InfoCard.TYPING_QUIT;
				case "typestats": case "tstats": case "ts":
					embed = InfoCard.TYPING_STATS;
				case "leaderboard": case "lboard": case "lb":
					embed = InfoCard.LEADERBOARD;
				case "chart":
					embed = InfoCard.CHART;
				}
			}
			else {embed = InfoCard.FULL_HELP;}
			
			try {channel.sendMessageEmbeds(embed.build()).queue();}
			catch (IllegalStateException e) {channel.sendMessageEmbeds(InfoCard.commandNotFound(args[1]).build()).queue();}
		}
	}
}