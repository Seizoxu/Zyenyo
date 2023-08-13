package dataStructures;

import java.util.Collections;
import java.util.Set;

import zyenyo.BotConfig;

public final class Aliases
{
	/*——————————————————————————————————————————|
	|————————————————INFORMATION————————————————|
	|——————————————————————————————————————————*/
	public final static Set<String> PING = Collections.singleton(BotConfig.PREFIX + "ping");
	public final static Set<String> HELP = Collections.singleton(BotConfig.PREFIX + "help");
	public final static Set<String> INFO = Set.of(
			BotConfig.PREFIX + "information",
			BotConfig.PREFIX + "info");
	
	
	
	/*——————————————————————————————————————|
	|————————————————MESTICS————————————————|
	|——————————————————————————————————————*/
	public final static Set<String> MESTICSSCRAPE = Set.of(
			BotConfig.PREFIX + "mesticsscrape",
			BotConfig.PREFIX + "msscrape",
			BotConfig.PREFIX + "scrape");
	public final static Set<String> MESTICSREAD = Set.of(
			BotConfig.PREFIX + "mesticsread",
			BotConfig.PREFIX + "msread",
			BotConfig.PREFIX + "read");
	
	
	
	/*—————————————————————————————————————|
	|————————————————TYPING————————————————|
	|—————————————————————————————————————*/
	public final static Set<String> TYPESTART = Set.of(
			BotConfig.PREFIX + "typestart",
			BotConfig.PREFIX + "typetest",
			BotConfig.PREFIX + "ttest",
			BotConfig.PREFIX + "tt");
	public final static Set<String> TEAMVS = Collections.singleton(BotConfig.PREFIX + "teamvs");
	public final static Set<String> NUMROWTEST = Collections.singleton(BotConfig.PREFIX + "numrow");
	public final static Set<String> TYPEQUIT = Set.of(
			BotConfig.PREFIX + "typestop",
			BotConfig.PREFIX + "typequit",
			BotConfig.PREFIX + "tquit",
			BotConfig.PREFIX + "tq");
	public final static Set<String> TYPESTATS = Set.of(
			BotConfig.PREFIX + "typestats",
			BotConfig.PREFIX + "tstats",
			BotConfig.PREFIX + "ts");
	public final static Set<String> TYPELIST = Set.of(
			BotConfig.PREFIX + "typelist",
			BotConfig.PREFIX + "tlist",
			BotConfig.PREFIX + "tl");
	public final static Set<String> CHART = Collections.singleton(BotConfig.PREFIX + "chart");
	public final static Set<String> LEADERBOARD = Set.of(
			BotConfig.PREFIX + "leaderboard",
			BotConfig.PREFIX + "lboard",
			BotConfig.PREFIX + "lb");
	public static final Set<String> DAILY = Collections.singleton(BotConfig.PREFIX + "daily");
	public static final Set<String> ACHIEVEMENT = Collections.singleton(BotConfig.PREFIX + "achievement");

	public final static Set<String> ACHIEVEMENTLIST = Set.of(
			BotConfig.PREFIX + "achievementlist",
			BotConfig.PREFIX + "al",
			BotConfig.PREFIX + "achievements");
	
	
	/*————————————————————————————————————|
	|————————————————ADMIN————————————————|
	|————————————————————————————————————*/
	public final static Set<String> SHUTDOWN = Collections.singleton(BotConfig.PREFIX + "shutdown");
	public final static Set<String> ADDTEST = Collections.singleton(BotConfig.PREFIX + "addtest");
	public final static Set<String> CLEARPROFILE = Collections.singleton(BotConfig.PREFIX + "clearprofile");
	public final static Set<String> FINDCHEATERS = Collections.singleton(BotConfig.PREFIX + "findcheaters");
	public final static Set<String> REMOVECHEATERS = Collections.singleton(BotConfig.PREFIX + "removecheaters");
	public final static Set<String> ADDPROMPT = Collections.singleton(BotConfig.PREFIX + "addprompt");
	public final static Set<String> REFRESHUSERS = Collections.singleton(BotConfig.PREFIX + "refreshusers");
	public final static Set<String> RECALCULATEPROMPTS = Collections.singleton(BotConfig.PREFIX + "recalculateprompts");
	public final static Set<String> UPDATE_AND_RECALCULATE_PROMPTS = Collections.singleton(BotConfig.PREFIX + "urcprompts");

	
	
	/*—————————————————————————————————————|
	|————————————————IN-DEV————————————————|
	|—————————————————————————————————————*/
	public final static Set<String> DPICONVERTER = Set.of(
			BotConfig.PREFIX + "dpiconverter",
			BotConfig.PREFIX + "dpiconv",
			BotConfig.PREFIX + "dpicalculator",
			BotConfig.PREFIX + "dpicalc");
}
