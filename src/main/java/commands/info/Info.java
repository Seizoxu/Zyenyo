package commands.info;

import dataStructures.Aliases;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Info extends ListenerAdapter
{
	private static final String INFO_MESSAGE = "Greetings. I am a statistics bot created by " + /*~~Goshujin-sama~~ *cough**/ "Seizoxu, "
			+ "currently under development. Some features may work one day, and break the next.";
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if (event.getAuthor().isBot()) {return;}
		
		MessageChannel channel = event.getChannel();
		Message message = event.getMessage();
		String content = message.getContentRaw();
		String[] args = content.split("\\s+");
		
		if (Aliases.INFO.contains(args[0].toLowerCase()))
		{
			channel.sendMessageEmbeds(new EmbedBuilder()
					.setTitle("ZyenyoBot Information.")
					.setDescription(INFO_MESSAGE)
					.addField("GitHub", "https://github.com/Seizoxu/Zyenyo", false)
					.setColor(0xde213c) // Zyen's favourite colour. :-)
					.setFooter("Developed by Seizoxu#0781 & ashwin#6541.")
					.build())
			
			.queue();
		}
	}
}
