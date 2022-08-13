//package osuCommands;
//
//import net.dv8tion.jda.api.EmbedBuilder;
//import net.dv8tion.jda.api.entities.Message;
//import net.dv8tion.jda.api.entities.MessageChannel;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//import net.dv8tion.jda.api.hooks.ListenerAdapter;
//import tw.kane.osu4j.OsuClient;
//import zyenyo.Zyenyo;
//
//public class Profile extends ListenerAdapter
//{
//	private final String CLIENT_ID = "12238";
//	private final String CLIENT_SECRET = "MRZSE00cFJjspnnkk0U6dJTi6l5kOgqzdDpXYfGk";
//	
//	@Override
//	public void onMessageReceived(MessageReceivedEvent event)
//	{
//		// Gets server, channel, and message data.
//		MessageChannel channel = event.getChannel();
//		Message message = event.getMessage();
//		String content = message.getContentRaw();
//		String[] args = content.split("\\s+");
//		EmbedBuilder embed = new EmbedBuilder();
//		
//		if (args[0].equalsIgnoreCase(Zyenyo.PREFIX + "osu"))
//		{
//			OsuClient osu = new OsuClient(CLIENT_ID, CLIENT_SECRET);
//		}
//	}
//}
