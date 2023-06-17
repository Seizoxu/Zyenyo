package asynchronous.typing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import zyenyo.Database;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import dataStructures.streakStatus;
import dataStructures.streakStatusResult;

public class Daily implements Runnable
{
	private MessageReceivedEvent event;
	private JDA jda;
	private String[] args;
	private String idStr;
	private MessageChannel channel;

	public Daily(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.jda = event.getJDA();
		this.args = args;
		this.idStr = event.getAuthor().getId();
		this.channel = event.getChannel();
	}
	@Override
	public void run() {
		event.getChannel().sendTyping().queue();
		try {
			streakStatusResult result = Database.getStreakStatus(idStr);

			if (result.status() == streakStatus.CLAIMED) {
				long today = Calendar.getInstance().getTimeInMillis();
				long availableOn = result.availableOnDate().getTimeInMillis();	
				long minutes = TimeUnit.MILLISECONDS.toMinutes(Math.abs(availableOn - today));
				int hours = (int) minutes / 60 ;
				channel.sendMessage(String.format("Next daily available in **%dh %dm**", hours, minutes % 60)).queue();
			} else {
				channel.sendMessage("Daily available! Complete a test with `\\tt` to continue your streak.").queue();
			}
		} catch (Exception e) {
			System.err.println(e);
		}
		
	}

}
