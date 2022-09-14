package asynchronous.typing;

import dataStructures.InfoCard;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;

public class AddTest implements Runnable
{
	private MessageReceivedEvent event;
	private MessageChannel channel;
	private Runnable sendHelp = new Runnable()
	{@Override public void run() {channel.sendMessageEmbeds(InfoCard.INCORRECT_SYNTAX.build()).queue();}};
	
	public AddTest(MessageReceivedEvent event) {this.event = event;}
	
	@Override
	public void run()
	{
		String args[] = event.getMessage().getContentRaw().split("\\s+");
		channel = event.getChannel();
		
		try
		{
			double	wpm = Double.parseDouble(args[1]),
					accuracy = Double.parseDouble(args[2]),
					tp = Double.parseDouble(args[3]);
			long userID;
			if (args.length == 4) {userID = event.getAuthor().getIdLong();}
			else {userID = Long.parseLong(args[4].substring(2, args[4].length() - 1));} // Only other case possible is args.length == 5.
			
			Database.addTest(userID, wpm, accuracy, tp);
		}
		catch (NumberFormatException e) {System.out.println("[ADDTEST: Unable to add test.] - Arguments formatted incorrectly."); sendHelp.run();}
	}
}
