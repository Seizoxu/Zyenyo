package asynchronous.typing;

import java.awt.Color;
import java.io.IOException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Deprecated
public class ClearProfile implements Runnable
{
	private MessageReceivedEvent event;
	public ClearProfile(MessageReceivedEvent event) {this.event = event;}

    @Override
    public void run()
    {
		try
		{
			TypingApiHandler.requestData("admin/remove/" + event.getAuthor().getId());
			
			event.getChannel().sendMessageEmbeds(new EmbedBuilder()
					.setTitle("Removed all tests for " + event.getAuthor().getAsTag())
					.setColor(new Color(180, 50, 80))
					.build())
			.queue();
		}
		catch (IOException | InterruptedException e) {e.printStackTrace();}
	}
}
