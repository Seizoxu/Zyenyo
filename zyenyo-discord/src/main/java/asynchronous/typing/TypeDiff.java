package asynchronous.typing;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import dataStructures.Prompt;
import fun.mike.dmp.Diff;
import fun.mike.dmp.DiffMatchPatch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.BotConfig;
import zyenyo.Database;

public class TypeDiff implements Runnable
{
	private MessageReceivedEvent event;
	private String[] args;
	
	private String discordId = "";
	private String username = "";
	private Prompt recentPrompt;
	private String originalText;
	private String modifiedText;
	
	public TypeDiff(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.args = args;
	}
	
	@Override
	public void run()
	{
		parseArguments();
		
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
	    		.setDescription(String.format("User: %s%n%n%s", username, str.trim()))
	    		.setFooter(String.format("DIFF calculated in %dms.", timeTaken));
	    
	    event.getChannel().sendMessageEmbeds(embed.build()).queue();
	}
	
	
	private void parseArguments()
	{
		for (String cmd : args)
		{
			Matcher matcher = Pattern.compile("<@\\d+>").matcher(cmd);
			if (matcher.find())
			{
				discordId = event.getMessage().getMentionedMembers().get(0).getId();
				username = event.getMessage().getMentionedMembers().get(0).getUser().getName();
			}
		}
		
		// Could just return from above, but planning to add more options.
		if (discordId.isBlank())
		{
			discordId = event.getAuthor().getId();
			username = event.getAuthor().getName();
		}
	}
}
