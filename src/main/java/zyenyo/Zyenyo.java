package zyenyo;

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
	public static boolean isTypeTestRunning = false;
	public static final String PREFIX = "\\";
	public static final Long BOT_USER_ID = 696614233944752130L;
	
	
	public static void main(String[] arguments) throws Exception
	{
		// Load:
		Aliases.setAliases();
		
		final String BOT_TOKEN = arguments[0];
		JDA api = JDABuilder.createDefault(BOT_TOKEN).build();
		api.getPresence().setStatus(OnlineStatus.ONLINE);
		api.getPresence().setActivity(Activity.playing("with everyone. :D"));
		api.addEventListener(new Info());
		api.addEventListener(new MesticsScrape());
		api.addEventListener(new MesticsRead(api));
		api.addEventListener(new Help());
//		api.addEventListener(new DpiConverter());
		api.addEventListener(new Ping());
		api.addEventListener(new Typing());
		
		
//		api.addEventListener(new Profile());
	}
}
