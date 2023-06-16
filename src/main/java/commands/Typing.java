package commands;

import java.util.HashMap;

import asynchronous.typing.Chart;
import asynchronous.typing.Leaderboard;
import asynchronous.typing.Daily;
import asynchronous.typing.TypingTestNumberRow;
import asynchronous.typing.TypingTestTeam;
import asynchronous.typing.TypeStats;
import asynchronous.typing.TypingTest;
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

		// IF: User requests for help...
		if (args.length == 2 && args[1].equalsIgnoreCase("help")) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
		
		
		
		// IF: Command is TYPESTART...
		if (Aliases.TYPESTART.contains(args[0].toLowerCase()))
		{
			if (args.length > 2) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			if (guildTestList.containsKey(serverID)) {Zyenyo.masterThreadPool.submit(testAlreadyRunning); return;}
				
			Zyenyo.masterThreadPool.submit(typingTest = new TypingTest(event, args));
			guildTestList.put(serverID, typingTest);
		}

                // IF: Command is TEAMVS...
		else if (Aliases.TEAMVS.contains(args[0].toLowerCase()))
		{
			if (args.length == 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			if (guildTestList.containsKey(serverID)) {Zyenyo.masterThreadPool.submit(testAlreadyRunning); return;}

			Zyenyo.masterThreadPool.submit(typingTest = new TypingTestTeam(event, args));
			guildTestList.put(serverID, typingTest);

		}

                // IF: Command is NUMROWTEST...
		else if (Aliases.NUMROWTEST.contains(args[0].toLowerCase()))
		{
			if (args.length > 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			if (guildTestList.containsKey(serverID)) {Zyenyo.masterThreadPool.submit(testAlreadyRunning); return;}

			Zyenyo.masterThreadPool.submit(typingTest = new TypingTestNumberRow(event, args));
			guildTestList.put(serverID, typingTest);

		}

		
		// IF: Command is TYPEQUIT...
		else if (Aliases.TYPEQUIT.contains(args[0].toLowerCase()))
		{
			if (args.length != 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			if (!guildTestList.containsKey(serverID)) {return;}

			guildTestList.get(serverID).quitTest();
			guildTestList.remove(serverID);
		}

		// IF: Command is TYPESTATS...
		else if (Aliases.TYPESTATS.contains(args[0].toLowerCase()))
			{Zyenyo.masterThreadPool.submit(new TypeStats(event, args));}
		
		// IF: Command is CHART...
		else if (Aliases.CHART.contains(args[0].toLowerCase()))
		{
			if (args.length != 1) {Zyenyo.masterThreadPool.submit(sendHelp); return;}
			
			Zyenyo.masterThreadPool.submit(new Chart(event));
		}
		
		// IF: Command is LEADERBOARD...
		else if (Aliases.LEADERBOARD.contains(args[0].toLowerCase()))
		{
			Zyenyo.masterThreadPool.submit(new Leaderboard(event, args));
		}

		// IF: Command is DAILY...
		else if (Aliases.DAILY.contains(args[0].toLowerCase()))
		{
			Zyenyo.masterThreadPool.submit(new Daily(event, args));
		}
	}
}
