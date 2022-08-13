package commands;

import java.sql.Timestamp;
import java.util.Date;
import java.util.IllegalFormatException;

import dataStructures.Aliases;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Ping extends ListenerAdapter
{
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		// Gets server, channel, and message data.
		MessageChannel channel = event.getChannel();
		String message = event.getMessage().getContentRaw();
		
		if (Aliases.PING.contains(message.trim().toLowerCase()))
		{
			try
			{
				long gatewayLatency = event.getJDA().getGatewayPing();
				event.getJDA().getRestPing().queue( (latency) ->
				channel.sendMessageFormat("**Pong!**\nBot: `%d ms`.\nGateway: `%d ms`.", latency, gatewayLatency).queue());
			}
			catch (InsufficientPermissionException e)
			{System.out.println("[" + new Timestamp(new Date().getTime()) + ", Ping] Insufficient permissions to send response."); return;}
			catch (IllegalFormatException e)
			{System.out.println("[" + new Timestamp(new Date().getTime()) + ", Ping] Message Formatting Error."); return;}
			catch (IllegalArgumentException e)
			{System.out.println("[" + new Timestamp(new Date().getTime()) + ", Ping] Invalid Arguments."); return;}
			catch (UnsupportedOperationException e)
			{System.out.println("[" + new Timestamp(new Date().getTime()) + ", Ping] Cannot send message to another bot."); return;}
			catch (Exception e)
			{System.out.println("[" + new Timestamp(new Date().getTime()) + ", Ping] Unknown Error occurred."); return;}
		}
	}
}
