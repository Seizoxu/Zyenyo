package dataStructures;

import java.util.HashSet;
import java.util.Set;

import zyenyo.Zyenyo;

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
	
	public final static Set<String> DPICONVERTER = new HashSet<>();
	
	public static void setAliases()
	{
		PING.add(Zyenyo.PREFIX + "ping");
		
		HELP.add(Zyenyo.PREFIX + "help");
		
		INFO.add(Zyenyo.PREFIX + "information");
		INFO.add(Zyenyo.PREFIX + "info");
		
		MESTICSSCRAPE.add(Zyenyo.PREFIX + "mesticsscrape");
		MESTICSSCRAPE.add(Zyenyo.PREFIX + "msscrape");
		MESTICSSCRAPE.add(Zyenyo.PREFIX + "scrape");
		
		MESTICSREAD.add(Zyenyo.PREFIX + "mesticsread");
		MESTICSREAD.add(Zyenyo.PREFIX + "msread");
		MESTICSREAD.add(Zyenyo.PREFIX + "read");
		
		TYPESTART.add(Zyenyo.PREFIX + "typestart");
		TYPESTART.add(Zyenyo.PREFIX + "typetest");
		TYPESTART.add(Zyenyo.PREFIX + "ttest");
		TYPESTART.add(Zyenyo.PREFIX + "tt");
		
		TYPEQUIT.add(Zyenyo.PREFIX + "typestop");
		TYPEQUIT.add(Zyenyo.PREFIX + "typequit");
		TYPEQUIT.add(Zyenyo.PREFIX + "tquit");
		TYPEQUIT.add(Zyenyo.PREFIX + "tq");
		
		TYPESTATS.add(Zyenyo.PREFIX + "typestats");
		TYPESTATS.add(Zyenyo.PREFIX + "tstats");
		TYPESTATS.add(Zyenyo.PREFIX + "ts");
		
		DPICONVERTER.add(Zyenyo.PREFIX + "dpiconverter");
		DPICONVERTER.add(Zyenyo.PREFIX + "dpiconv");
		DPICONVERTER.add(Zyenyo.PREFIX + "dpicalculator");
		DPICONVERTER.add(Zyenyo.PREFIX + "dpicalc");
	}
}
