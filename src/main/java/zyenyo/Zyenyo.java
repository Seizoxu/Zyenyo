package zyenyo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import commands.Administrator;
import commands.MessageStatistics;
import commands.Typing;
import commands.info.Help;
import commands.info.Info;
import commands.info.Ping;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class Zyenyo
{
	private static JDA api;
	public static ExecutorService masterThreadPool = Executors.newCachedThreadPool();
	
	public static void main(String[] arguments) throws Exception
	{
		final String BOT_TOKEN = arguments[0];
        final String MONGO_URI = arguments[1];

        //Connect to Database.
        Database.connect(MONGO_URI);
		
		// LOAD: Commands.
		api = JDABuilder.createDefault(BOT_TOKEN)
				.addEventListeners(
						new Info(),
						new Help(),
						new Ping(),
						new MessageStatistics(),
						new Typing(),
						new Administrator()
//						new Profile(),
//						new DpiConverter(),
				).build();
		
		BotConfig.setConfigVars();
		
		api.getPresence().setStatus(OnlineStatus.ONLINE);
		api.getPresence().setActivity(Activity.playing("with everyone. :D"));
	}
}
