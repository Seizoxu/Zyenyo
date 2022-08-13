package commands;

import dataStructures.Aliases;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Info extends ListenerAdapter
{
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		final String infoMessage = "Greetings. I am a bot created by " + /*~~Goshujin-sama~~ *cough**/ "Seizoxu. If you are reading this "
				+ "message, I am currently undergoing a migration from Python to Java, so some (or many) features will be unavailable.";
		if (event.getAuthor().isBot()) {return;}
		
		MessageChannel channel = event.getChannel();
		Message message = event.getMessage();
		String content = message.getContentRaw();
		String[] args = content.split("\\s+");
		
		if (Aliases.INFO.contains(args[0].toLowerCase()))
		{
			EmbedBuilder info = new EmbedBuilder();
			info.setTitle("ZyenyoBot Information.");
			info.setDescription(infoMessage);
			info.setColor(0xde213c); // Zyen's favourite colour. :-)
			info.setFooter("Created by Seizoxu#0781.", event.getMember().getUser().getAvatarUrl());
			
			channel.sendTyping().queue();
			channel.sendMessageEmbeds(info.build()).queue();
		}
	}
}
