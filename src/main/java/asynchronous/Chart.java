package asynchronous;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
			String jsonString = TypingApiHandler.requestData("chart/wpm/" + event.getAuthor().getId());
			JSONObject json = (JSONObject) JSONValue.parse(jsonString);

			event.getChannel().sendMessage(json.get("URL").toString())
			.queue();
		}
		catch (IOException | InterruptedException e) {e.printStackTrace();}
	}
}
