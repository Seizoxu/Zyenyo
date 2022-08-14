package dataStructures;

import java.util.HashSet;
import java.util.Set;

import zyenyo.BotConfig;

public final class Aliases
{
	public final static Set<String> PING = new HashSet<>();
	public final static Set<String> HELP = new HashSet<>();
	public final static Set<String> INFO = new HashSet<String>();
	
	public final static Set<String> MESTICSSCRAPE = new HashSet<>();
	public final static Set<String> MESTICSREAD = new HashSet<>();
	
	public final static Set<String> TYPESTART = new HashSet<>();
	public final static Set<String> TYPEQUIT = new HashSet<>();
	public final static Set<String> TYPESTATS = new HashSet<>();
	public final static Set<String> CLEARTESTS = new HashSet<>();
	public final static Set<String> CHART = new HashSet<>();
	
	public final static Set<String> DPICONVERTER = new HashSet<>();
	
	public static void setAliases()
	{
		PING.add(BotConfig.PREFIX + "ping");
		
		HELP.add(BotConfig.PREFIX + "help");
		
		INFO.add(BotConfig.PREFIX + "information");
		INFO.add(BotConfig.PREFIX + "info");
		
		MESTICSSCRAPE.add(BotConfig.PREFIX + "mesticsscrape");
		MESTICSSCRAPE.add(BotConfig.PREFIX + "msscrape");
		MESTICSSCRAPE.add(BotConfig.PREFIX + "scrape");
		
		MESTICSREAD.add(BotConfig.PREFIX + "mesticsread");
		MESTICSREAD.add(BotConfig.PREFIX + "msread");
		MESTICSREAD.add(BotConfig.PREFIX + "read");
		
		TYPESTART.add(BotConfig.PREFIX + "typestart");
		TYPESTART.add(BotConfig.PREFIX + "typetest");
		TYPESTART.add(BotConfig.PREFIX + "ttest");
		TYPESTART.add(BotConfig.PREFIX + "tt");
		
		TYPEQUIT.add(BotConfig.PREFIX + "typestop");
		TYPEQUIT.add(BotConfig.PREFIX + "typequit");
		TYPEQUIT.add(BotConfig.PREFIX + "tquit");
		TYPEQUIT.add(BotConfig.PREFIX + "tq");
		
		TYPESTATS.add(BotConfig.PREFIX + "typestats");
		TYPESTATS.add(BotConfig.PREFIX + "tstats");
		TYPESTATS.add(BotConfig.PREFIX + "ts");

		CLEARTESTS.add(BotConfig.PREFIX + "cleartests");

		CHART.add(BotConfig.PREFIX + "chart");
		
		DPICONVERTER.add(BotConfig.PREFIX + "dpiconverter");
		DPICONVERTER.add(BotConfig.PREFIX + "dpiconv");
		DPICONVERTER.add(BotConfig.PREFIX + "dpicalculator");
		DPICONVERTER.add(BotConfig.PREFIX + "dpicalc");
	}
}
