package asynchronous.typing;

import java.io.IOException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Deprecated
public class RemoveCheaters implements Runnable
{
	private MessageReceivedEvent event;
	
	public RemoveCheaters(MessageReceivedEvent event) {this.event = event;}
	
	@Override
	public void run()
	{
		try
		{
			TypingApiHandler.requestData("admin/remove/cheatedtests");
			
			event.getChannel().sendMessageEmbeds(new EmbedBuilder()
					.setDescription("Successfully removed all cheated scores.").build()).queue();
		}
		catch (IOException | InterruptedException e) {e.printStackTrace();}
	}
}
