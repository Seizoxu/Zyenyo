package asynchronous.typing;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import com.google.common.collect.Iterables;
import com.mongodb.client.AggregateIterable;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;

public class TypeTop implements Runnable
{
	private MessageReceivedEvent event;
	private JDA jda;
	private String[] args;
	
	public TypeTop(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.jda = event.getJDA();
		this.args = args;
	}
	
	@Override
	public void run()
	{
		// One page = 5 plays.
		int playsPage = 1;
		String userId = event.getAuthor().getId();
		String messageAuthorName = event.getAuthor().getName();
		
		for (String cmd : args)
		{
			Matcher matcher = Pattern.compile("<@\\d+>").matcher(cmd);
			if (matcher.find())
			{
				userId = event.getMessage().getMentionedMembers().get(0).getId();
				messageAuthorName = event.getMessage().getMentionedMembers().get(0).getUser().getName();
			}

			switch(cmd.toLowerCase())
			{
			case "-page":
			case "-p":
				playsPage = getPageArg(args, cmd);
				break;
			}
		}
		
		AggregateIterable<Document> plays = Database.getTopPlays(userId);

		EmbedBuilder embed = new EmbedBuilder()
				.setTitle("TypeTop for " + messageAuthorName);
		
		int index = 0;
		for (Document play : Iterables.skip(plays, (playsPage-1) * 5))
		{
			embed.addField(
					String.format("#%d. %s",
							(playsPage-1) * 5 + index + 1, play.getString("prompt")),
					String.format(
							  "**`TP :`** %.2f%n"
							+ "**`WPM:`** %.2f%n"
							+ "**`ACC:`** %.2f%%%n"
							+ "*Set <t:%d:R>*",
							play.get("tp"),
							play.get("wpm"),
							play.get("accuracy"),
							ZonedDateTime.parse(
									play.get("date").toString(),
									DateTimeFormatter.ofPattern("E MMM d HH:mm:ss z uuuu", Locale.ENGLISH))
							.toInstant()
							.toEpochMilli()/1000
							),
					false
					);
			
			if (++index == 5) {break;}
		}
		
		embed.setFooter(
				String.format("Showing test %d to %d on page %d.",
						(playsPage-1) * 5 + index - 4,
						(playsPage-1) * 5 + index + 1,
						playsPage
						));
		
		event.getChannel().sendMessageEmbeds(embed.build()).queue();
	}
	
	private static int getPageArg(String[] args, String cmd)
	{
		String pageNumberArg;
		int pageNumber = 1;
		try
		{
			pageNumberArg = args[ Arrays.asList(args).indexOf(cmd) + 1];
			pageNumber = Integer.parseInt(pageNumberArg);
			
			if (pageNumber > 20) {return 1;}
		}
		catch(Exception e)
		{
			return 1;
		}
		
		return pageNumber;
	}
}
