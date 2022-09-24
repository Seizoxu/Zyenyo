package asynchronous.typing;

import dataStructures.InfoCard;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;

public class AddPrompt implements Runnable
{
	private MessageReceivedEvent event;
	private MessageChannel channel;
	private Runnable sendHelp = new Runnable()
	{@Override public void run() {channel.sendMessageEmbeds(InfoCard.INCORRECT_SYNTAX.build()).queue();}};
	
	public AddPrompt(MessageReceivedEvent event) {this.event = event;}
	
	@Override
	public void run()
	{
		String args[] = event.getMessage().getContentRaw().split("#");
		channel = event.getChannel();
                
                Database.addPrompt(args[1], args[2]);
		}
}
