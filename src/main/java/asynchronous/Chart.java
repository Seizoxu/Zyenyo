package asynchronous;

import java.awt.Color;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class Chart implements Runnable
{
	private MessageReceivedEvent event;
	public Chart(MessageReceivedEvent event) {this.event = event;}

    @Override
    public void run()
    {
		try
		{
			String jsonString = TypingApiHandler.requestData(event.getAuthor().getIdLong(), "chart");
			JSONObject json = (JSONObject) JSONValue.parse(jsonString);

			event.getChannel().sendMessage(json.get("URL").toString())
			.queue();
		}
		catch (IOException | InterruptedException e) {e.printStackTrace();}
	}
}
