package asynchronous.typing;

import org.bson.Document;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;

public class CompareTest implements Runnable
{

	private MessageReceivedEvent event;
	private String[] args;
	
	/**
	 * Sends a message embed of a specified user's top 100 tests.
	 * @param event
	 * @param args
	 * @since 0.3.01-beta
	 */
	public CompareTest(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.args = args;
	}
	
	@Override
	public void run()
	{
		String messageAuthorName = event.getAuthor().getName();
		String channelID = event.getChannel().getId();

		Document stats = Database.channelCompare(channelID, event.getAuthor().getId());

		EmbedBuilder embed = new EmbedBuilder().setTitle(String.format("Top stats for %s on %s", messageAuthorName, stats.getString("_id")));
		embed.appendDescription(String.format(
			"Best TP: **`%.2f`**%n"
				+ "Best WPM: **`%.2f`**%n"
				+ "Best ACC: **`%.2f`**%%%n"
				+ "Average WPM: **`%.2f`**%n",
				stats.getDouble("maxTp"), stats.getDouble("maxWpm"), stats.getDouble("maxAcc"), stats.getDouble("avgTp")));

		event.getChannel().sendMessageEmbeds(embed.build()).queue();
	}
		
}
