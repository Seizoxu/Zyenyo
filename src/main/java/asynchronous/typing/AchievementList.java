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
	// limit on the number button clicks allowed, so that users don't keep bumping old messages and send unnecessary calls to the bot
	private static final Integer BUTTONCLICK_MAX = 50;
	private MessageReceivedEvent event;
	private MessageChannel channel;
	// TODO: using a static variable for pages causes problems when a user interacts with multiple messages of the same command. 
	static Integer page;
	static Integer buttonClickCount;

	public AchievementList(MessageReceivedEvent event, String[] args) {
		this.event = event;
		this.channel = event.getChannel();
	}

	@Override
	public void run() {
		event.getChannel().sendTyping().queue();
		page = 1;
		buttonClickCount = 0;
		
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
		buttonClickCount++;

		event.editMessageEmbeds(makeAchievementList().build())
			.setActionRow(
				Button.secondary("achievementListPrev", Emoji.fromUnicode("U+25C0")).withDisabled(page <=1 || buttonClickCount >= BUTTONCLICK_MAX), // arrow backward
				Button.secondary("achievementListNext", Emoji.fromUnicode("U+25B6")).withDisabled(page >= 20 || buttonClickCount >= BUTTONCLICK_MAX) // arrow forward
			)
			.queue();
	}

	public static void onPrevPage(ButtonClickEvent event) {
		page--;
		buttonClickCount++;

		event.editMessageEmbeds(makeAchievementList().build())
			.setActionRow(
				Button.secondary("achievementListPrev", Emoji.fromUnicode("U+25C0")).withDisabled(page <=1 || buttonClickCount >= BUTTONCLICK_MAX), // arrow backward
				Button.secondary("achievementListNext", Emoji.fromUnicode("U+25B6")).withDisabled(page >= 20 || buttonClickCount >= BUTTONCLICK_MAX) // arrow forward
			)
			.queue();
	}

}
