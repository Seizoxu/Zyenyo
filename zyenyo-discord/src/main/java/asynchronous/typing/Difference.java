package asynchronous.typing;

import java.util.LinkedList;

import org.bson.Document;

import dataStructures.Prompt;
import fun.mike.dmp.Diff;
import fun.mike.dmp.DiffMatchPatch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.BotConfig;
import zyenyo.Database;

public class Difference implements Runnable
{
	private MessageReceivedEvent event;
	private String[] args;
	
	private Prompt recentPrompt;
	private String originalText;
	private String modifiedText;
	
	public Difference(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.args = args;
	}
	
	@Override
	public void run()
	{
		String discordId = event.getAuthor().getId();
		Document recentPlay = Database.getRecentPlays(discordId, 1).first();
		String promptTitle = recentPlay.getString("prompt");
		double accuracy = recentPlay.getDouble("accuracy");
		modifiedText = recentPlay.getString("submittedText");
		
		if (accuracy == 100d)
		{
			event.getChannel().sendMessage("Your previous accuracy was 100%. There is nothing to correct.").queue();
			return;
		}
		
		for (Prompt prompt : BotConfig.promptList)
		{
			if (promptTitle.equals(prompt.title()))
			{
				recentPrompt = prompt;
				originalText = prompt.body();
				break;
			}
		}
		
		long startTime = System.currentTimeMillis();
		DiffMatchPatch dmp = new DiffMatchPatch();
	    LinkedList<Diff> diffList = dmp.diff_main(modifiedText, originalText);
	    dmp.diffCleanupSemantic(diffList);
	    long timeTaken = System.currentTimeMillis() - startTime;
	    
	    String str = "";
	    for (Diff diff : diffList)
	    {
	    	switch(diff.operation)
	    	{
	    	case EQUAL:
	    		str += String.format(" %s ", diff.text.strip());
	    		break;
	    	case INSERT:
	    		str += String.format(" **`%s`** ", diff.text.strip());
	    		break;
	    	case DELETE:
	    		str += String.format(" ~~`%s`~~ ", diff.text.strip());
	    	}
	    }
	    
	    EmbedBuilder embed = new EmbedBuilder()
	    		.setTitle(String.format("DIFF: [#%d | %.2fTR] %s",
	    				recentPrompt.number(), recentPrompt.typeRating(), recentPrompt.title()))
	    		.setDescription(str.trim())
	    		.setFooter(String.format("DIFF calculated in %dms.", timeTaken));
	    
	    event.getChannel().sendMessageEmbeds(embed.build()).queue();
	}
}
