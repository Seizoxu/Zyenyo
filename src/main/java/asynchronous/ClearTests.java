package asynchronous;

import java.awt.Color;
import java.io.IOException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class ClearTests implements Runnable
{
	private MessageReceivedEvent event;
	public ClearTests(MessageReceivedEvent event) {this.event = event;}

    @Override
    public void run()
    {
		try
		{
			TypingApiHandler.requestData("test/remove", event.getAuthor().getIdLong());
			
			event.getChannel().sendMessageEmbeds(new EmbedBuilder()
					.setTitle("Removed all tests for " + event.getAuthor().getAsTag())
					.setColor(new Color(180, 50, 80))
					.build())
			.queue();
		}
		catch (IOException | InterruptedException e) {e.printStackTrace();}
	}
}
