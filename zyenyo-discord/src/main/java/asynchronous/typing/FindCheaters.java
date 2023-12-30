package asynchronous.typing;

import java.awt.Color;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Deprecated
public class FindCheaters implements Runnable
{
	private MessageReceivedEvent event;
	
	public FindCheaters(MessageReceivedEvent event) {this.event = event;}
	
	
	@Override
	public void run()
	{
		try
		{
			JSONArray json = (JSONArray) ((JSONObject)JSONValue.parse(TypingApiHandler.requestData("admin/find/cheaters"))).get("users");
			
			if (json.size() == 0)
			{
				event.getChannel().sendMessageEmbeds(new EmbedBuilder()
						.setDescription("No cheaters found.")
						.setColor(Color.black)
						.build())
				.queue();
				return;
			}
			
			JDA jda = event.getJDA();
			JSONObject cheaterMember;
			String	cheaterID, date;
			double	wpm, accuracy;
			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("Suspicious Users List: ")
					.setColor(Color.black);
			for (int i = 0; i < json.size(); i++)
			{
				cheaterMember = (JSONObject) json.get(i);
				cheaterID = cheaterMember.get("discordID").toString();
				date = cheaterMember.get("date").toString();
				wpm = Double.parseDouble(cheaterMember.get("wpm").toString());
				accuracy = Double.parseDouble(cheaterMember.get("accuracy").toString());
				
				embed.addField(jda.retrieveUserById(cheaterID).complete().getAsTag(),
						String.format("Date: %s | WPM: %.2f | Acc: %.2f", date, wpm, accuracy), false);
			}
			
			event.getChannel().sendMessageEmbeds(embed.build()).queue();
		}
		catch (IOException | InterruptedException e)
		{
			event.getChannel().sendMessageEmbeds(new EmbedBuilder()
					.addField("Error: Cannot find cheaters.", "Reason: Unknown.", false)
					.build())
			.queue();
		}
	}
}
