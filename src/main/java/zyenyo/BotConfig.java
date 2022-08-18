package zyenyo;

public class BotConfig
{
	public static final char PREFIX = '\\';
	public static Long BOT_USER_ID = -1L;
	
	public static final String BOT_DATA_FILEPATH = "ZBotData/";
	public static final String SCRAPE_DATA_FILEPATH = "ZBotData/ScrapeData/";
	public static final String INDEX_COUNTS_FILEPATH = "ZBotData/ScrapeData/COUNTS.zbif"; //ZBIF = ZyenyoBotIndexFile.
	public static final String INDEX_IDS_FILEPATH = "ZBotData/ScrapeData/IDs.zbif";
	
	protected static void setConfigVars(Long id) {BOT_USER_ID = id;}
}
