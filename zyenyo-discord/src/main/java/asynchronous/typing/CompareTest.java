package asynchronous.typing;

import java.time.Instant;

import org.bson.Document;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;

public class CompareTest implements Runnable
{

	private MessageReceivedEvent event;
	
	/**
	 * Sends a message embed of a specified user's top 100 tests.
	 * @param event
	 * @param args
	 * @since 0.3.01-beta
	 */
	public CompareTest(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
	}
	
	@Override
	public void run()
	{
		try {
			String messageAuthorName = event.getAuthor().getName();
			String channelID = event.getChannel().getId();

			Document stats = Database.channelCompare(channelID, event.getAuthor().getId());

			EmbedBuilder embed = new EmbedBuilder().setTitle(String.format("Best score for %s on %s", messageAuthorName, stats.getString("prompt")));
			embed.appendDescription(String.format(
				"TP: **`%.2f`**%n"
				+ "WPM: **`%.2f`**%n"
				+ "ACC: **`%.2f`**%%%n"
				+ "Set: <t:%d:R>",
				stats.getDouble("tp"), stats.getDouble("wpm"), stats.getDouble("accuracy"), Instant.parse(stats.getString("date")).toEpochMilli()));

			event.getChannel().sendMessageEmbeds(embed.build()).queue();
		
		} catch (Exception e) {
			System.err.println(e);
		}
	}
		
}
