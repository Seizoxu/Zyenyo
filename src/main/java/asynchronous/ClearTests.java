package asynchronous;

import java.awt.Color;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
			String jsonString = TypingApiHandler.requestData(event.getAuthor().getIdLong(), "remove");
			JSONObject json = (JSONObject) JSONValue.parse(jsonString);
			
			String text = json.get("text").toString();
			
			event.getChannel().sendMessageEmbeds(new EmbedBuilder()
					.addField("Removed all tests for " + event.getAuthor().getAsTag(), "", false)
					.setColor(new Color(180, 50, 80))
					.build())
			.queue();
		}
		catch (IOException | InterruptedException e) {e.printStackTrace();}
	}


}
