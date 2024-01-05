package zyenyo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataStructures.Prompt;
import dataStructures.PromptHeadings;

public class BotConfig
{
	public static char PREFIX;
//	public static HashMap<Integer, String> promptMap = new HashMap<>(200);
//	public static HashMap<Integer, Double> promptRatingMap = new HashMap<>();
	public static List<Prompt> newPromptList = new ArrayList<>(200);
	public static ArrayList<List<Integer>> promptDifficultyList = new ArrayList<>(4);
	public static HashMap<String, Double> characterRatingMap = new HashMap<>(200); // Should only require 116.
	
	public static int NUM_PROMPTS; // Set in CalculatePromptDifficulty.downloadAndUpdatePrompts.run();
	public static final String BOT_DATA_FILEPATH = "ZBotData/";
	public static final String TYPING_PROMPTS_FILEPATH = BOT_DATA_FILEPATH + "TypingPrompts/";
	public static final String SCRAPE_DATA_FILEPATH = "ZBotData/ScrapeData/";
	public static final String INDEX_COUNTS_FILEPATH = "ZBotData/ScrapeData/COUNTS.zbif"; //ZBIF = ZyenyoBotIndexFile.
	public static final String INDEX_IDS_FILEPATH = "ZBotData/ScrapeData/IDs.zbif";
	public static final List<Long> ADMINISTRATOR_IDS = List.of(642193466876493829l, 365691073156087819l);
	
	protected static final File PROMPT_RATING_FILE = new File("ZBotData/TypingPrompts/PromptRatingMap.zbo");
	protected static final File PROMPT_DIFFICULTY_FILE = new File("ZBotData/TypingPrompts/SortedPromptsList.zbo");
	
	protected static void setConfigVars(String ENVIRONMENT)
	{
		PREFIX = ENVIRONMENT.equals("development") ? '.' : '\\';
		
		CalculatePromptDifficulty.downloadAndUpdatePrompts();
		loadBotObjects();
	}
	
	@SuppressWarnings("unchecked")
	protected static void loadBotObjects()
	{
		try (	ObjectInputStream ratingMapOIS = new ObjectInputStream(new FileInputStream(PROMPT_RATING_FILE));
				ObjectInputStream difficultyListOIS = new ObjectInputStream(new FileInputStream(PROMPT_DIFFICULTY_FILE));)
		{
			Map<Integer, Double> promptRatingMap = (HashMap<Integer, Double>)ratingMapOIS.readObject();
			System.out.println("[LOADED] Prompt Rating Map File");
			
			for (int i = 0; i < NUM_PROMPTS; i++)
			{
				try (BufferedReader reader = new BufferedReader(new FileReader(
						String.format("%sprompt%d.txt", TYPING_PROMPTS_FILEPATH, i)));)
				{
					String body = reader.readLine();
					newPromptList.add(new Prompt(i, PromptHeadings.get(i), body, body.length(), promptRatingMap.get(i)));
				}
			}
			
			promptDifficultyList = (ArrayList<List<Integer>>)difficultyListOIS.readObject();
			System.out.println("[LOADED] Prompt Difficulty Categorisation File");
			
			System.out.println("[LOAD_INFO] Num Prompts: " + NUM_PROMPTS);
		}
		catch(IOException | ClassNotFoundException e) {e.printStackTrace();}
	}
}
