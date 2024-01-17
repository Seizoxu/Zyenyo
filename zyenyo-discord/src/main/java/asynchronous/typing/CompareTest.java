package asynchronous.typing;

import java.time.Instant;

import org.bson.Document;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;

public class CompareTest implements Runnable
{

	private MessageReceivedEvent event;
	private String[] args;
	private String idStr;
	
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

		try {
			if (args.length == 2) {
				idStr = args[1].subSequence(2, args[1].length()-1).toString();
				System.out.println(idStr);
			} else {
				idStr = event.getAuthor().getId();
			}

			String messageAuthorName = event.getAuthor().getName();
			String channelID = event.getChannel().getId();

			Document stats = Database.channelCompare(channelID, idStr);

			EmbedBuilder embed = new EmbedBuilder().setTitle(String.format("Best score for %s on %s", event.getJDA().retrieveUserById(idStr).submit().get().getAsTag(), stats.getString("prompt")));
			embed.appendDescription(String.format(
				"TP: **`%.2f`**%n"
				+ "WPM: **`%.2f`**%n"
				+ "ACC: **`%.2f`**%%%n"
				+ "Set: <t:%d:R>",
				stats.getDouble("tp"), stats.getDouble("wpm"), stats.getDouble("accuracy"), stats.getDate("date").toInstant().toEpochMilli() / 1000));

			event.getChannel().sendMessageEmbeds(embed.build()).queue();
		
		} catch (Exception e) {
			e.printStackTrace();
			event.getChannel().sendMessage(String.format("**Error: %s**", e.getMessage())).queue();
		}
	}
		
}
