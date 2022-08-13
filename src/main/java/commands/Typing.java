package commands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import asynchronous.ClearTests;
import asynchronous.TypeStats;
import asynchronous.TypingTest;
import dataStructures.Aliases;
import dataStructures.InfoCard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import zyenyo.Zyenyo;

public class Typing extends ListenerAdapter
{
	private TypingTest typingTest;
	@Override public void onMessageReceived(MessageReceivedEvent event)
	{
		if (event.getAuthor().isBot()) {return;}
		
		// Gets data.
		MessageChannel channel = event.getChannel();
		String[] args = event.getMessage().getContentRaw().split("\\s+");
		
		ExecutorService pool = Executors.newFixedThreadPool(1);
		
		if (Aliases.TYPESTART.contains(args[0].toLowerCase()))
		{
			// Executes command if the syntax is correct.
			if ((args.length == 1) || !(args[1].equalsIgnoreCase("help")))
			{
				try
				{
					if (Zyenyo.isTypeTestRunning) {return;}
					pool.submit(typingTest = new TypingTest(event, args));
					Zyenyo.isTypeTestRunning = true;
				}
				catch (NumberFormatException e)
				{
					EmbedBuilder syntax = InfoCard.TypingTestSyntax(new EmbedBuilder());
					channel.sendMessageEmbeds(syntax.build()).queue();
				}
			}
			// Otherwise sends help.
			else
			{
				EmbedBuilder info = InfoCard.TypingTestHelp(new EmbedBuilder());
				channel.sendTyping().queue();
				channel.sendMessageEmbeds(info.build()).queue();
			}
		}
		
		else if (Aliases.TYPEQUIT.contains(args[0].toLowerCase()))
		{
			// Executes the command if the message is formatted correctly.
			if (args.length == 1 || !(args[1].equalsIgnoreCase("help")))
			{
				try
				{
					if (!Zyenyo.isTypeTestRunning) {return;}
					typingTest.quitTest();
					Zyenyo.isTypeTestRunning = false;
				}
				catch (NumberFormatException e)
				{
					EmbedBuilder syntax = InfoCard.TypingQuitSyntax(new EmbedBuilder());
					channel.sendMessageEmbeds(syntax.build()).queue();
				}
			}
			// Otherwise sends help.
			else
			{
				EmbedBuilder info = InfoCard.TypingQuitHelp(new EmbedBuilder());
				channel.sendTyping().queue();
				channel.sendMessageEmbeds(info.build()).queue();
			}
		}
		
		else if (Aliases.TYPESTATS.contains(args[0].toLowerCase()))
		{
			// Executes the command if the message is formatted correctly.
			if (args.length == 1 || !(args[1].equalsIgnoreCase("help")))
			{
				try {pool.submit(new TypeStats(event));}
				catch (NumberFormatException e)
				{
					EmbedBuilder syntax = InfoCard.TypingStatsSyntax(new EmbedBuilder());
					channel.sendMessageEmbeds(syntax.build()).queue();
				}
			}
			// Otherwise sends help.
			else
			{
				EmbedBuilder info = InfoCard.TypingStatsHelp(new EmbedBuilder());
				channel.sendTyping().queue();
				channel.sendMessageEmbeds(info.build()).queue();
			}
		}
		else if (Aliases.CLEARTESTS.contains(args[0].toLowerCase()))
                {
                  
			if (args.length == 1 || !(args[1].equalsIgnoreCase("help")))
                        {
                              try {pool.submit(new ClearTests(event));}
                              catch (NumberFormatException e)
                              {
                                      EmbedBuilder syntax = InfoCard.TypingStatsSyntax(new EmbedBuilder());
                                      channel.sendMessageEmbeds(syntax.build()).queue();
                              }

                        }
                }
	}
}
