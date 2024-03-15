package asynchronous.typing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;

public class BlackList implements Runnable
{
	private MessageReceivedEvent event;
	private String[] args;

	public BlackList(MessageReceivedEvent event, String[] args) {
	      this.args = args;
	      this.event = event;
	}

	@Override
	public void run()
	{
		Database.blacklistAdd(args[1]);
		event.getChannel().sendMessage("Added to blacklist").queue();
	}
}
