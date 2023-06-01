package zyenyo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.bson.Document;

import dataStructures.PromptHeadings;

public class CalculatePromptDifficulty
{
	private final static Set<Character> SPECIAL_CHARS =	Set.of(
			'`','~','$','%','^','&','*','(',')','-','_','=','+',
			'[','{',']','}','\\','|',
			':',';');
	private final static Set<Character> LEFT_HAND_CHARS = Set.of(
			'`','1','2','3','4','5',			'~','!','@','#','$','%',
			'q','w','e','r','t',				'Q','W','E','R','T',
			'a','s','d','f','g',				'A','S','D','F','G',
			'z','x','c','v',					'Z','X','C','V');
	private final static Set<Character> RIGHT_HAND_CHARS = Set.of(
			'7','8','9','0','-','=',			'&','*','(',')','_','+',
			'y','u','i','o','p','[',']','\\',	'Y','U','I','O','P','{','}','|',
			'h','j','k','l',';','\'',			'H','J','K','L',':','"',
			'n','m',',','.','/',				'N','M','<','>','?');
	private final static Set<Character> BOTH_HANDS_CHARS =	Set.of('6','b','^','B',' ');
	
	private final static double COMBO_EXPONENT_CONSTANT = 2;
	
	private static HashMap<Integer, Double> promptRatingMap = new HashMap<>();
	private static ArrayList<List<Integer>> promptsSortedByDifficulty = new ArrayList<List<Integer>>(4);
	
	// Typing Points v0.2.3_2
	public static void recalculatePromptRatings()
	{
		BufferedReader reader = null;
		ObjectOutputStream mapObjectWriter = null,
				listObjectWriter = null;
		File promptRatingFile = new File(BotConfig.BOT_DATA_FILEPATH + "TypingPrompts/PromptRatingMap.zbo");
		File categorisedPrompts = new File(BotConfig.BOT_DATA_FILEPATH + "TypingPrompts/SortedPromptsList.zbo");
		try
		{
			char[] prompt;
			for (int i = 0; i < 4; i++) {promptsSortedByDifficulty.add(new ArrayList<Integer>());} // Initialise 2DArrayList.
			for (int i = 0; i < BotConfig.NUM_PROMPTS; i++)
			{
				reader = new BufferedReader(new FileReader(String.format("%sTypingPrompts/prompt%d.txt", BotConfig.BOT_DATA_FILEPATH, i+1)));
				prompt = reader.readLine().toCharArray();
				promptData pd = calculateSinglePrompt(prompt);
				
				promptRatingMap.put(i+1, pd.typeRating());
				
				if (pd.typeRating() < 0.8) {promptsSortedByDifficulty.get(0).add(i+1);} // Easy.
				else if (pd.typeRating() < 1.0) {promptsSortedByDifficulty.get(1).add(i+1);} // Medium.
				else if (pd.typeRating() < 1.5) {promptsSortedByDifficulty.get(2).add(i+1);} // Hard.
				else {promptsSortedByDifficulty.get(3).add(i+1);} // Diabolical.
			}
			
			new File(BotConfig.BOT_DATA_FILEPATH+"TypingPrompts/").mkdirs();
			mapObjectWriter = new ObjectOutputStream(new FileOutputStream(promptRatingFile));
			mapObjectWriter.writeObject(promptRatingMap);
			System.out.println("[CREATED] Prompt Rating Map File");
			
			listObjectWriter = new ObjectOutputStream(new FileOutputStream(categorisedPrompts));
			listObjectWriter.writeObject(promptsSortedByDifficulty);
			System.out.println("[CREATED] Prompt Difficulty Categorisation File");
		}
		catch (IOException e) {e.printStackTrace();}
		finally
		{
			try
			{
				if (reader != null) {reader.close();}
				if (mapObjectWriter != null) {mapObjectWriter.close();}
				if (listObjectWriter != null) {listObjectWriter.close();}
			}
			catch (IOException e) {e.printStackTrace();}
		}
	}
	
	
	public static promptData calculateSinglePrompt(char[] prompt)
	{
		char currentChar;
		double currentPoints;
		Hand previousHand = Hand.LEFT;
		double totalDifficulty = 0;
		double specialCharacterBonus = 0;
		int handCombo = 0;
		
		for (int j = 0; j < prompt.length; j++)
		{
			currentChar = prompt[j];
			if (LEFT_HAND_CHARS.contains(currentChar))
			{
				if (previousHand.equals(Hand.LEFT)) {handCombo++;} // Same hand.
				else if (previousHand.equals(Hand.RIGHT)) // Other hand.
				{
					handCombo = 0;
					previousHand = Hand.LEFT;
				}
			}
			else if (RIGHT_HAND_CHARS.contains(currentChar))
			{
				if (previousHand.equals(Hand.LEFT)) // Other hand.
				{
					handCombo = 0;
					previousHand = Hand.RIGHT;
				}
				else if (previousHand.equals(Hand.RIGHT)) {handCombo++;} // Same hand.
			}
			else if (BOTH_HANDS_CHARS.contains(currentChar))
			{
				handCombo = 0;
				if (previousHand.equals(Hand.LEFT)) {previousHand = Hand.RIGHT;}
				else if (previousHand.equals(Hand.RIGHT)) {previousHand = Hand.LEFT;}
			}
			
			currentPoints = calculateComboPoints(handCombo);
			specialCharacterBonus += (SPECIAL_CHARS.contains(currentChar)) ? 2.5*currentPoints : 0;
			
			totalDifficulty += currentPoints;
		}
		
		totalDifficulty += specialCharacterBonus;
		double lengthBonus = (prompt.length < 200)
				? Math.sqrt(prompt.length/200)
				: (Math.log(prompt.length/200) / Math.log(50)) + 1;
		// Effective log base 50 of (x/200) + 1   (Change of log base formula).
		
		double typeRating = lengthBonus * ((totalDifficulty / (double)prompt.length));
		
		return new promptData(typeRating, lengthBonus, specialCharacterBonus);
	}
	
	
	private static double calculateComboPoints(int handCombo)
	{
		if (handCombo > 9) {return 0.75*Math.pow(9, COMBO_EXPONENT_CONSTANT);} // 10 max combo bonus (40.50 points per char).
		return 0.75*Math.pow(handCombo, COMBO_EXPONENT_CONSTANT);
	}
	
	
	public static Runnable downloadAndUpdatePrompts = new Runnable()
	{
		private String newPromptsPath = BotConfig.BOT_DATA_FILEPATH + "newPrompts/";
		private String oldPromptsPath = BotConfig.BOT_DATA_FILEPATH + "TypingPrompts/";
		
		@Override
		public void run()
		{
			ArrayList<Document> prompts = Database.getPrompts();
			
			// Creates temporary newPrompts directory.
			File newPromptsFolder = new File(newPromptsPath);
			if (!newPromptsFolder.exists()) {newPromptsFolder.mkdirs();}
			
			// Creates all the prompt files with their proper ID and text.
			for (int i = 0; i < prompts.size(); i++)
			{
				File newPrompt = new File( String.format("%sprompt%d.txt", newPromptsPath, i+1) );
				PromptHeadings.addHeading(prompts.get(i).getString("title"));
				
				try (BufferedWriter promptWriter = new BufferedWriter( new FileWriter(newPrompt) );)
				{
					if (!newPrompt.exists()) {newPrompt.createNewFile();}					
					promptWriter.write( prompts.get(i).get("text").toString() );
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
			// Deletes old folder, and renames the new one to the old.
			try
			{
				File oldPromptsFolder = new File(oldPromptsPath);
				FileUtils.deleteDirectory(oldPromptsFolder);
				
				newPromptsFolder.renameTo(oldPromptsFolder);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			BotConfig.NUM_PROMPTS = prompts.size();
			recalculatePromptRatings();
		}
	};
}

record promptData(double typeRating, double lengthBonus, double specialCharacterBonus) {}

enum Hand {LEFT,RIGHT}
