package asynchronous.typing;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import zyenyo.Database;

import java.awt.Color;
import java.util.Arrays;

import org.bson.Document;

import com.mongodb.client.AggregateIterable;

public class AchievementList implements Runnable {
	private MessageReceivedEvent event;
	private MessageChannel channel;
	static Integer page;

	public AchievementList(MessageReceivedEvent event, String[] args) {
		this.event = event;
		this.channel = event.getChannel();
	}

	@Override
	public void run() {
		event.getChannel().sendTyping().queue();
		page = 1;
		
		try {

			EmbedBuilder embed = makeAchievementList();

			channel.sendMessageEmbeds(embed.build())
				.setActionRow(
					Button.secondary("prev", Emoji.fromUnicode("U+25C0")).asDisabled(), // arrow backward
					Button.secondary("achievementListNext", Emoji.fromUnicode("U+25B6")) // arrow forward
				)
				.queue();

		} catch (Exception e) {
			System.err.println(e);
		}

	}

	private static EmbedBuilder makeAchievementList() {

		AggregateIterable<Document> achievementList = Database.getAchievementList(page);
		final int initialPosition = (page -1) * 10;

		EmbedBuilder embed = new EmbedBuilder().setColor(Color.black);
		embed.setTitle("Zyenyo Achievements");

		int index = initialPosition;

		for (Document doc : achievementList) {
		embed.addField(String.format("[%d] %s", ++index, doc.getString("title")), doc.getString("description"), false);
		}

		embed.setFooter(
			String.format("Showing achievements %d to %d on page %d.",
			initialPosition+1, initialPosition+10, page)
		);

		return embed;
	}

	public static void onNextPage(ButtonClickEvent event) {
		page++;

		event.editMessageEmbeds(makeAchievementList().build())
			.setActionRow(
				Button.secondary("achievementListPrev", Emoji.fromUnicode("U+25C0")).withDisabled(page <=1), // arrow backward
				Button.secondary("achievementListNext", Emoji.fromUnicode("U+25B6")).withDisabled(page >= 20) // arrow forward
			)
			.queue();
	}

	public static void onPrevPage(ButtonClickEvent event) {
		page--;

		event.editMessageEmbeds(makeAchievementList().build())
			.setActionRow(
				Button.secondary("achievementListPrev", Emoji.fromUnicode("U+25C0")).withDisabled(page <=1), // arrow backward
				Button.secondary("achievementListNext", Emoji.fromUnicode("U+25B6")).withDisabled(page >= 20) // arrow forward
			)
			.queue();
	}

}
