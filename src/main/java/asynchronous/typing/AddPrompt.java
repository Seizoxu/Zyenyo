package asynchronous.typing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;

public class AddPrompt implements Runnable
{
	private MessageReceivedEvent event;

	public AddPrompt(MessageReceivedEvent event) {this.event = event;}

	@Override
	public void run()
	{
		String args[] = event.getMessage().getContentRaw().split("#");

		Database.addPrompt(args[1], args[2]);
		event.getChannel().sendMessage("Added").queue();
	}
}
