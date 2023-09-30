package asynchronous.typing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import zyenyo.Database;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;

import dataStructures.AchievementDetails;
import dataStructures.streakStatusResult;

public class Achievement implements Runnable {
	private MessageReceivedEvent event;
	private String idStr;
	private MessageChannel channel;
	private String[] args;

	public Achievement(MessageReceivedEvent event, String[] args) {
		this.event = event;
		this.idStr = event.getAuthor().getId();
		this.channel = event.getChannel();
		this.args = args;
	}

	@Override
	public void run() {
		event.getChannel().sendTyping().queue();
		try {
			AchievementDetails achievement = Database.getAchievement(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
			EmbedBuilder embed = new EmbedBuilder().setColor(Color.black);

			embed.setTitle(achievement.title());

			embed.setThumbnail(String.format("attachment://thumbnail.png"));
			embed.setDescription(achievement.description());

			File file = new File(achievement.thumbnail());


			channel.sendMessageEmbeds(embed.build()).addFile(file, "thumbnail.png").queue();

		} catch (Exception e) {
			System.err.println(e);
		}

	}

}
