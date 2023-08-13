package asynchronous.typing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import zyenyo.Database;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;

import org.bson.Document;

import com.mongodb.client.AggregateIterable;

import dataStructures.streakStatusResult;

public class AchievementList implements Runnable {
	private MessageReceivedEvent event;
	private String idStr;
	private String[] args;
	private MessageChannel channel;

	public AchievementList(MessageReceivedEvent event, String[] args) {
		this.event = event;
		this.idStr = event.getAuthor().getId();
		this.channel = event.getChannel();
		this.args = args;
	}

	@Override
	public void run() {
		event.getChannel().sendTyping().queue();
		int page = 1;
		
		for (String cmd : args) {
			switch (cmd.toLowerCase()) {
				case "-p": page = getValueArg(args, "-p"); break;
				case "-page": page = getValueArg(args, "-page"); break;
			}
		}

		final int initialPosition = (page -1) * 10;

		try {
			AggregateIterable<Document> achievementList = Database.getAchievementList(page);

			EmbedBuilder embed = new EmbedBuilder().setColor(Color.black);
			
			embed.setTitle("Zyenyo Achievements");

			int index = initialPosition;

			for (Document doc : achievementList) {
				embed.addField(String.format("[%d] %s", ++index, doc.getString("title")), doc.getString("description"), false);
			}

			embed.setFooter(
					String.format("Showing achievements %d to %d on page %d.",
							initialPosition+1, initialPosition+10, page)
			);

			channel.sendMessageEmbeds(embed.build()).queue();

		} catch (Exception e) {
			System.err.println(e);
		}

	}
	
	//TODO: this method is duplicated from Leaderboard.java. extract this to reduce code duplication in the future
	public int getValueArg(String[] args, String checkArg)
	{
		String lbPageArg;
		int lbPage;
		
		try
		{
			lbPageArg = args[ Arrays.asList(args).indexOf(checkArg) + 1 ];
			lbPage = Integer.parseInt(lbPageArg);
			
			// Avoid ridiculously high page numbers.
			if ( lbPage >= 500 ) {return 1;}
		}
		catch (ArrayIndexOutOfBoundsException | NullPointerException | NumberFormatException e)
		{
			return 1;
		}
		
		return lbPage;
	}

}
