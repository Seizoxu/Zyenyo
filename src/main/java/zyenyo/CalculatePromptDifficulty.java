package zyenyo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Set;

public class CalculatePromptDifficulty
{
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
	
	private final static double COMBO_BASE_CONSTANT = 2;
	
	private static HashMap<Integer, Double> promptRatingMap = new HashMap<>();
	
	public static void calculate()
	{
		BufferedReader reader = null;
		ObjectOutputStream objectWriter = null;
		File promptRatingFile = new File(BotConfig.BOT_DATA_FILEPATH + "TypingPrompts/PromptRatingMap.zbo");
		try
		{
			char[] prompt;
			char currentChar;
			for (int i = 0; i < BotConfig.NUM_PROMPTS; i++)
			{
				reader = new BufferedReader(new FileReader(String.format("%sTypingPrompts/prompt%d.txt", BotConfig.BOT_DATA_FILEPATH, i+1)));
				prompt = reader.readLine().toCharArray();
				Hand previousHand = Hand.LEFT;
				double totalDifficulty = 0;
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
					
					totalDifficulty += calculatePoints(handCombo);
				}
				
				promptRatingMap.put(i+1, ((double)totalDifficulty / (double)prompt.length) - 1);
			}
			
			new File(BotConfig.BOT_DATA_FILEPATH+"TypingPrompts/").mkdirs();
			objectWriter = new ObjectOutputStream(new FileOutputStream(promptRatingFile));
			objectWriter.writeObject(promptRatingMap);
			
			System.out.println("[CREATED] Prompt Rating File");
		}
		catch (IOException e) {e.printStackTrace();}
		finally
		{
			try
			{
				if (reader != null) {reader.close();}
				if (objectWriter != null) {objectWriter.close();}
			}
			catch (IOException e) {e.printStackTrace();}
		}
	}
	
	private static double calculatePoints(int handCombo)
	{
		if (handCombo > 9) {return Math.pow(COMBO_BASE_CONSTANT, 9);} // 10 max combo bonus (125.00 points per char).
		
		return Math.pow(COMBO_BASE_CONSTANT, handCombo);
	}
}

enum Hand {LEFT,RIGHT}
