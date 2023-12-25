package commands.osu;
//package commands;
//
//import dataStructures.Aliases;
//import net.dv8tion.jda.api.EmbedBuilder;
//import net.dv8tion.jda.api.entities.Message;
//import net.dv8tion.jda.api.entities.MessageChannel;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//import net.dv8tion.jda.api.hooks.ListenerAdapter;
//
//public class DpiConverter extends ListenerAdapter
//{
//	@Override
//	public void onMessageReceived(MessageReceivedEvent event)
//	{
//		if (event.getAuthor().isBot()) {return;}
//		// Gets server, channel, and message data.
//		MessageChannel channel = event.getChannel();
//		Message message = event.getMessage();
//		String content = message.getContentRaw();
//		String[] args = content.split("\\s+");
//		EmbedBuilder embed = new EmbedBuilder();
//		
//		
//		if (Aliases.DPICONVERTER.contains(args[0].toLowerCase()))
//		{
//			System.out.println(1);
//		}
//	}
//
//}
