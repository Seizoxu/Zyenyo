package zyenyo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import commands.Help;
import commands.Info;
import commands.MesticsRead;
import commands.MesticsScrape;
import commands.Ping;
import commands.Typing;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class Zyenyo
{
	protected static JDA api;
	public static ExecutorService masterThreadPool = Executors.newCachedThreadPool();
	
	public static void main(String[] arguments) throws Exception
	{
		final String BOT_TOKEN = arguments[0];
        final String MONGO_URI = arguments[1];

        //Connect to Database.
        Database.connect(MONGO_URI);
		
		// LOAD: Commands.
		api = JDABuilder.createDefault(BOT_TOKEN)
				.addEventListeners(new Info(),
						new MesticsScrape(),
						new MesticsRead(),
						new Help(),
						new Ping(),
//						new Profile(),
//						new DpiConverter(),
						new Typing())
				.build();
		
		BotConfig.setConfigVars(api.getSelfUser().getIdLong());
		
		api.getPresence().setStatus(OnlineStatus.ONLINE);
		api.getPresence().setActivity(Activity.playing("with everyone. :D"));
	}
}
