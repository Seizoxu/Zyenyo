package dataStructures;

import net.dv8tion.jda.api.EmbedBuilder;

public class InfoCard
{
	public static EmbedBuilder FullHelp(EmbedBuilder embed)
	{
		embed.setTitle("Commands help.")
		.setDescription("These commands have no particular order for the moment. They will be managed later.")
		.addField("Help", "Shows this message.", false)
		.addField("Info", "Shows general bot information.", false)
		.addField("Ping", "Returns the bot and gateway latency.", false)
		.addField("MesticsScrape", "Scrapes a specified number of messages and stores the statistics on a local file.", false)
		.addField("DpiConverter", "Converts fullscreen DPI on one screen to another.", false)
		.setColor(0x8D538D);
		return embed;
	}
	
	public static EmbedBuilder HelpEasterEgg(EmbedBuilder embed)
	{
		embed.setTitle("CMD: Help.")
		.setDescription("You wanted help on how to use the _help_ command?!")
		.setColor(0x8D538D) // Lilac. The colour of helpfulness, apparently.
		.addField("Syntax", "`\\help <command name:plain text>`", false);
		return embed;
	}
	
	public static EmbedBuilder InfoHelp(EmbedBuilder embed)
	{
		embed.setTitle("CMD: Bot Information.")
		.setDescription("This command provides information pertaining to this bot (that's me!)")
		.setColor(0x8D538D)
		.addField("Aliases", "`None.`", false)
		.addField("Syntax", "`\\info`", false);
		return embed;
	}
	
	public static EmbedBuilder CommandNotFound(EmbedBuilder embed, String command)
	{
		embed.addField("Command " + command + " not found!", "", false)
		.setColor(0x8D538D);
		return embed;
	}
	
	public static EmbedBuilder MesticsScrapeHelp(EmbedBuilder embed)
	{
		embed.setTitle("CMD: Message Statistics Update.")
		.setDescription("This command scrapes message statistics (\"Mestics\") for each account in a server. This is "
				+ "a manual message scrape with a specified limit which will have separate statistics on a separate file."
				+ "As of now, there is no limit on the number of messages to scrape on a server. Do not abuse this.")
		.setColor(0x8D538D)
		.addField("Aliases", "`msscrape, scrape`", false)
		.addField("Syntax", "`\\mesticsscrape <channel:#channel> <limit:integer>`", false);
		return embed;
	}
	
	public static EmbedBuilder MesticsScrapeSyntax(EmbedBuilder embed)
	{
		embed.setTitle("Incorrect Syntax!")
		.addField("Syntax", "`\\scrape <limit:integer>`", false)
		.setColor(0x8D538D);
		return embed;
	}
	
	public static EmbedBuilder MesticsReadHelp(EmbedBuilder embed)
	{
		embed.setTitle("CMD: Read Message Statistics.")
		.setDescription("This command reads existing scrape files created using the message scraper command, given an "
				+ "MSRecall-ID, which was provided in a completion message, when a scrape operation was completed.")
		.setColor(0x8D538D)
		.addField("Aliases","`msread`, `read`", false)
		.addField("Syntax", "`\\mesticsread <MSRecall-ID:integer>`", false);
		return embed;
	}
	
	public static EmbedBuilder MesticsReadSyntax(EmbedBuilder embed)
	{
		embed.setTitle("Incorrect Syntax!")
		.addField("Syntax", "`\\mesticsread <MSRecall-ID:integer>`", false)
		.setColor(0x8D538D);
		return embed;
	}
	
	public static EmbedBuilder PingHelp(EmbedBuilder embed)
	{
		embed.setTitle("CMD: Ping.")
		.setDescription("This command will output both the bot and gateway latency, in milliseconds.")
		.addField("Aliases", "`None.`", false)
		.addField("Syntax", "\\ping", false)
		.setColor(0x8D538D);
		return embed;
	}
	
	public static EmbedBuilder DpiConverterHelp(EmbedBuilder embed)
	{
		embed.setTitle("CMD: DPI Converter.")
		.setDescription("This command will convert the fullscreen mouse DPI (PPI) in one resolution to another, taking"
				+ " screen sizes into account, as well.")
		.addField("Aliases", "`dpiconv, dpicalculator, dpicalc`", false)
		.addField("Syntax", "\\dpiconverter", false)
		.setColor(0x8D538D);
		return embed;
	}
	
	public static EmbedBuilder TypingTestHelp(EmbedBuilder embed)
	{
		embed.setTitle("CMD: Typing Test.")
		.setDescription("This command starts a typing test. One paragraph shall be randomly selected from a pool or paragraphs, "
				+ "and the typist will have a fixed amount of time to complete it, with an adjustable speed via difficulty adjustment.")
		.setColor(0x8D538D)
		.addField("Aliases","`typestart`, `ttest`, `tt`", false)
		.addField("Syntax", "`\\typetest <difficulty (optional)>`", false);
		return embed;
	}
	
	public static EmbedBuilder TypingTestSyntax(EmbedBuilder embed)
	{
		embed.setTitle("Incorrect Syntax!")
		.addField("Syntax", "`\\typetest <difficulty (optional)>`", false)
		.setColor(0x8D538D);
		return embed;
	}
	
	public static EmbedBuilder TypingStatsHelp(EmbedBuilder embed)
	{
		embed.setTitle("CMD: Typing Statistics.")
		.setDescription("This command displays metrics of a user's typing statistics, collected from his/her typing tests so far.")
		.setColor(0x8D538D)
		.addField("Aliases","`tstats`, `ts`", false)
		.addField("Syntax", "`\\typestats`", false);
		return embed;
	}
	
	public static EmbedBuilder TypingStatsSyntax(EmbedBuilder embed)
	{
		embed.setTitle("Incorrect Syntax!")
		.addField("Syntax", "`\\typetest`", false)
		.setColor(0x8D538D);
		return embed;
	}
	
	public static EmbedBuilder TypingQuitHelp(EmbedBuilder embed)
	{
		embed.setTitle("CMD: Quit Typing Test.")
		.setDescription("This command quits the current typing test, if there is one active.")
		.setColor(0x8D538D)
		.addField("Aliases","`typestop`, `tquit`, `tq`", false)
		.addField("Syntax", "`\\typequit`", false);
		return embed;
	}
	
	public static EmbedBuilder TypingQuitSyntax(EmbedBuilder embed)
	{
		embed.setTitle("Incorrect Syntax!")
		.addField("Syntax", "`\\typetest`", false)
		.setColor(0x8D538D);
		return embed;
	}
}