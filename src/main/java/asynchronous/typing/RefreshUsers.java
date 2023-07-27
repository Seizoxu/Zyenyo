package asynchronous.typing;

import dataStructures.InfoCard;
import dataStructures.RefreshUserNamesResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;

public class RefreshUsers implements Runnable
{
	private MessageReceivedEvent event;
	private MessageChannel channel;
	private Runnable sendHelp = new Runnable()
	{@Override public void run() {channel.sendMessageEmbeds(InfoCard.INCORRECT_SYNTAX.build()).queue();}};
	
	public RefreshUsers(MessageReceivedEvent event) {this.event = event;}
	
	@Override
	public void run()
	{
		String args[] = event.getMessage().getContentRaw().split("\\s+");
		channel = event.getChannel();
		
		try
		{
			RefreshUserNamesResult result = Database.refreshUserNames(event);
			
			channel.sendMessageEmbeds(new EmbedBuilder()
					.addField("Successfully refreshed user tags",
							String.format(""
									+ "Time taken: **`%dms`**%n"
									+ "Modified documents: **`%d`**%n",
									result.timeTakenMillis(), result.outdatedTagsCount()), false)
					.build())
			.queue();
		}
		catch (NumberFormatException e) {System.out.println("[ADDTEST: Unable to add test.] - Arguments formatted incorrectly."); sendHelp.run();}
	}
}
