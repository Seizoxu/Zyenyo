package commands;

import java.util.HashMap;

import asynchronous.typing.Chart;
import asynchronous.typing.CompareTest;
import asynchronous.typing.Daily;
import asynchronous.typing.Difference;
import asynchronous.typing.Leaderboard;
import asynchronous.typing.TypeList;
import asynchronous.typing.TypeStats;
import asynchronous.typing.TypeTop;
import asynchronous.typing.TypingTest;
import asynchronous.typing.TypingTestNumberRow;
import asynchronous.typing.TypingTestTeam;
import asynchronous.typing.TypingTestTemplate;
import dataStructures.Aliases;
import dataStructures.InfoCard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import zyenyo.Zyenyo;

public class Typing extends ListenerAdapter
{
	public static HashMap<Long, TypingTestTemplate> guildTestList = new HashMap<>(); // guildID : TestInstance
	private TypingTestTemplate typingTest;
	private MessageChannel channel;
	private long serverID;
	private String[] args;
	private Runnable sendHelp = new Runnable()
		{@Override public void run() {channel.sendMessageEmbeds(InfoCard.INCORRECT_SYNTAX.build()).queue();}};
	private Runnable testAlreadyRunning = new Runnable()
	{
		@Override
		public void run()
		{
			channel.sendMessageEmbeds(new EmbedBuilder()
					.setDescription("Test is already running in this server. Type `\\tq` to stop the test.")
					.build())
			.queue();
		}
	};
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if (event.getAuthor().isBot()) {return;}

		// Gets data.
		channel = event.getChannel();
		serverID = event.getGuild().getIdLong();
		args = event.getMessage().getContentRaw().split("\\s+");

		
		
		if (Aliases.TYPESTART.contains(args[0].toLowerCase()))
		{
			if (args.length > 2) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			if (guildTestList.containsKey(serverID)) {Zyenyo.masterThreadPool.submit(testAlreadyRunning); return;}
				
			Zyenyo.masterThreadPool.submit(typingTest = new TypingTest(event, args));
			guildTestList.put(serverID, typingTest);
		}

		else if (Aliases.TEAMVS.contains(args[0].toLowerCase()))
		{
			if (args.length == 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			if (guildTestList.containsKey(serverID)) {Zyenyo.masterThreadPool.submit(testAlreadyRunning); return;}

			Zyenyo.masterThreadPool.submit(typingTest = new TypingTestTeam(event, args));
			guildTestList.put(serverID, typingTest);

		}

		else if (Aliases.NUMROWTEST.contains(args[0].toLowerCase()))
		{
			if (args.length > 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			if (guildTestList.containsKey(serverID)) {Zyenyo.masterThreadPool.submit(testAlreadyRunning); return;}

			Zyenyo.masterThreadPool.submit(typingTest = new TypingTestNumberRow(event, args));
			guildTestList.put(serverID, typingTest);

		}

		
		else if (Aliases.TYPEQUIT.contains(args[0].toLowerCase()))
		{
			if (args.length != 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			if (!guildTestList.containsKey(serverID)) {return;}

			guildTestList.get(serverID).quitTest();
			guildTestList.remove(serverID);
		}

		else if (Aliases.TYPESTATS.contains(args[0].toLowerCase()))
		{
			Zyenyo.masterThreadPool.submit(new TypeStats(event, args));
		}
		
		else if (Aliases.TYPELIST.contains(args[0].toLowerCase()))
		{
			Zyenyo.masterThreadPool.submit(new TypeList(event, args));
		}
		
		else if (Aliases.CHART.contains(args[0].toLowerCase()))
		{
			if (args.length != 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			
			Zyenyo.masterThreadPool.submit(new Chart(event));
		}
		
		else if (Aliases.LEADERBOARD.contains(args[0].toLowerCase()))
		{
			Zyenyo.masterThreadPool.submit(new Leaderboard(event, args));
		}

		else if (Aliases.DAILY.contains(args[0].toLowerCase()))
		{
			Zyenyo.masterThreadPool.submit(new Daily(event, args));
		}
		
		else if (Aliases.TYPETOP.contains(args[0].toLowerCase()))
		{
			Zyenyo.masterThreadPool.submit(new TypeTop(event, args));
		}

		else if (Aliases.COMPARE.contains(args[0].toLowerCase()))
		{
			Zyenyo.masterThreadPool.submit(new CompareTest(event, args));
		}
		
		else if (Aliases.DIFFERENCE.contains(args[0].toLowerCase()))
		{
			Zyenyo.masterThreadPool.submit(new Difference(event, args));
		}
	}
}
