package zyenyo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class BotConfig
{
	public static final char PREFIX = '\\';
	public static Long BOT_USER_ID = -1L;
	public static HashMap<Integer, Double> promptRatingMap = new HashMap<>();
	
	public static final String BOT_DATA_FILEPATH = "ZBotData/";
	public static final String SCRAPE_DATA_FILEPATH = "ZBotData/ScrapeData/";
	public static final String INDEX_COUNTS_FILEPATH = "ZBotData/ScrapeData/COUNTS.zbif"; //ZBIF = ZyenyoBotIndexFile.
	public static final String INDEX_IDS_FILEPATH = "ZBotData/ScrapeData/IDs.zbif";
	public static final int NUM_PROMPTS = 33;
	
	private static final File PROMPT_RATING_FILE = new File("ZBotData/TypingPrompts/PromptRatingMap.zbo");
	
	@SuppressWarnings("unchecked") // Unavoidable unchecked cast.
	protected static void setConfigVars(Long id)
	{
		BOT_USER_ID = id;
		System.out.println("[LOADED] Bot ID");
		
		if (!PROMPT_RATING_FILE.exists()) {CalculatePromptDifficulty.calculate();}
		try (ObjectInputStream objectReader = new ObjectInputStream(new FileInputStream(PROMPT_RATING_FILE)))
		{
			promptRatingMap = (HashMap<Integer, Double>)objectReader.readObject();
			System.out.println("[LOADED] Type Rating File");
		}
		catch(IOException | ClassNotFoundException e) {e.printStackTrace();}
	}
}
