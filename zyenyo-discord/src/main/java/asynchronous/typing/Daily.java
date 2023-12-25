package asynchronous.typing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import zyenyo.Database;

import java.awt.Color;
import dataStructures.streakStatusResult;

public class Daily implements Runnable {
	private MessageReceivedEvent event;
	private String idStr;
	private MessageChannel channel;

	public Daily(MessageReceivedEvent event, String[] args) {
		this.event = event;
		this.idStr = event.getAuthor().getId();
		this.channel = event.getChannel();
	}

	@Override
	public void run() {
		event.getChannel().sendTyping().queue();
		try {
			streakStatusResult result = Database.getStreakStatus(idStr);
			EmbedBuilder embed = new EmbedBuilder().setColor(Color.black);

			switch (result.status()) {
				case CLAIMED:
					embed.setTitle(String.format("Next daily available <t:%d:R>", result.referenceDate().getTimeInMillis() / 1000));
					break;
				case MISSED:
					embed.setTitle(String.format("Your streak expired <t:%d:R>", result.referenceDate().getTimeInMillis() / 1000))
							.setDescription("Type `\\tt` to start a new streak");
					break;
				case AVAILABLE:
					embed.setTitle("Daily Available!").setDescription(
							"Complete a test with `\\tt` to continue your streak")
							.addField("", String.format("Streak expires <t:%d:R>",
									result.referenceDate().getTimeInMillis() / 1000), false);
					break;
				case INITIAL:
					embed.setTitle("Daily Available!").setDescription(
							"Complete a test with `\\tt` to begin your streak");
					break;
				default:
					break;

			}

			channel.sendMessageEmbeds(embed.build()).queue();

		} catch (Exception e) {
			System.err.println(e);
		}

	}

}
