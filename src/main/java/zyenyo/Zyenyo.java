package zyenyo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import commands.Help;
import commands.Info;
import commands.MesticsRead;
import commands.MesticsScrape;
import commands.Ping;
import commands.Typing;
import dataStructures.Aliases;
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
		// LOAD: Prerequisites.
		Aliases.setAliases();
		final String BOT_TOKEN = arguments[0];
		api = JDABuilder.createDefault(BOT_TOKEN).build();
		BotConfig.setConfigVars(api.getSelfUser().getIdLong());
		
		// LOAD: Commands.
		api.getPresence().setStatus(OnlineStatus.ONLINE);
		api.getPresence().setActivity(Activity.playing("with everyone. :D"));
		api.addEventListener(new Info());
		api.addEventListener(new MesticsScrape());
		api.addEventListener(new MesticsRead());
		api.addEventListener(new Help());
//		api.addEventListener(new DpiConverter());
		api.addEventListener(new Ping());
		api.addEventListener(new Typing());
		
//		api.addEventListener(new Profile());
	}
	
	public JDA getJDA() {return Zyenyo.api;}
}
