package dataStructures;

import net.dv8tion.jda.api.EmbedBuilder;

public class InfoCard
{
	/*——————————————————————————————————————————|
	|————————————————INFORMATION————————————————|
	|——————————————————————————————————————————*/

	public static final EmbedBuilder FULL_HELP =
		new EmbedBuilder()
		.setTitle("Commands Help.")
		.setDescription("To obtain more information about a command, type `\\help [command]`.\n"
				+ "Keep in mind that brackets `[]` are used for optional parameters, and "
				+ "arrow brackets `<>` are used for required parameters.")
		.addField("Help", "Shows this message.", false)
		.addField("Info", "Shows general bot information.", false)
		.addField("Ping", "Returns the bot and gateway latency.", false)
		.addField("MesticsScrape", "Scrapes a specified number of messages and stores the statistics on a local file.", false)
		.addField("MesticsRead", "Reads a specified scrape file, given an MS-Recall ID.", false)
		.addField("TypeTest", "Starts a typing test.", false)
		.addField("TeamVs", "Starts a team typing test.", false)
		.addField("Numrow", "Starts a numrow typing test.", false)
		.addField("TypeQuit", "Stops a typing test, if there is one currently running in the server.", false)
		.addField("TypeStats", "Shows Typing Statistics.", false)
		.addField("TypeList", "Shows a list of prompts.", false)
//		.addField("Chart", "Shows a chart of a user's typing stats.", false)
		.addField("Leaderboard", "Shows the typing stats leaderboard of a specified statistic.", false)
		.addField("Daily", "Shows daily streak information.", false)
//		.addField("DpiConverter", "Converts fullscreen DPI on one screen to another (not implemented).", false)
		.setColor(0x8D538D);
	
	
	public static final EmbedBuilder HELP_EASTER_EGG =
		new EmbedBuilder()
		.setTitle("CMD: Help.")
		.setDescription("You wanted help on how to use the _help_ command?!")
		.setColor(0x8D538D) // Lilac. The colour of helpfulness, apparently.
		.addField("Syntax", "`\\help <command>`", false);
	
	@Deprecated
	public static final EmbedBuilder HELP =
		new EmbedBuilder()
		.setTitle("CMD: Help.")
		.setDescription("Please use the `help` command to obtain more information about a command.")
		.setColor(0x8D538D)
		.addField("Aliases", "`None.`", false)
		.addField("Syntax", "`\\help [command]`", false);
	
	
	public static final EmbedBuilder PING = 
		new EmbedBuilder()
		.setTitle("CMD: Ping.")
		.setDescription("This command will output both the bot and gateway latency, in milliseconds.")
		.addField("Aliases", "`None`", false)
		.addField("Syntax", "\\ping", false)
		.setColor(0x8D538D);
	
	public static final EmbedBuilder INFO =
		new EmbedBuilder()
		.setTitle("CMD: Bot Information.")
		.setDescription("This command provides information pertaining to this bot (that's me!)")
		.setColor(0x8D538D)
		.addField("Aliases", "`info`", false)
		.addField("Syntax", "`\\information`", false);

	public static final EmbedBuilder INCORRECT_SYNTAX =
		new EmbedBuilder()
		.setTitle("Incorrect Syntax!")
		.setColor(0x8D538D);
	
	public static EmbedBuilder commandNotFound(String command)
	{
		return new EmbedBuilder()
		.addField("Command " + command + " not found!", "", false)
		.setColor(0x8D538D);
	}
	

	
	/*——————————————————————————————————————|
	|————————————————MESTICS————————————————|
	|——————————————————————————————————————*/

	public static final EmbedBuilder MESTICS_SCRAPE =
		new EmbedBuilder()
		.setTitle("CMD: Message Statistics Update.")
		.setDescription("This command scrapes message statistics (\"Mestics\") for each account in a server. This is "
				+ "a manual message scrape with a specified limit which will have separate statistics on a separate file."
				+ "As of now, there is no limit on the number of messages to scrape on a server. Do not abuse this.")
		.setColor(0x8D538D)
		.addField("Aliases", "`msscrape, scrape`", false)
		.addField("Syntax", "`\\mesticsscrape <#channel> <limit:integer>`", false);
	
	public static final EmbedBuilder MESTICS_READ =
		new EmbedBuilder()
		.setTitle("CMD: Read Message Statistics.")
		.setDescription("This command reads existing scrape files created using the message scraper command, given an "
				+ "MSRecall-ID, which was provided in a completion message, when a scrape operation was completed.")
		.setColor(0x8D538D)
		.addField("Aliases","`msread`, `read`", false)
		.addField("Syntax", "`\\mesticsread <MSRecall-ID:integer>`", false);
	

	
	/*—————————————————————————————————————|
	|————————————————TYPING————————————————|
	|—————————————————————————————————————*/

	public static final EmbedBuilder TYPING_TEST =
		new EmbedBuilder()
		.setTitle("CMD: Typing Test.")
		.setDescription("This command starts a typing test. One paragraph shall be randomly selected from a "
				+ "pool or paragraphs, and the typist will have a fixed amount of time to complete it.")
		.setColor(0x8D538D)
		.addField("Aliases","`typestart`, `ttest`, `tt`", false)
		.addField("Syntax", "`\\typetest [Prompt Number]`", false);
//		.addField("Syntax", "`\\typetest [difficulty: easy | medium | hard | diabolical]`", false);
	
	public static final EmbedBuilder TYPING_TEAMVS =
		new EmbedBuilder()
		.setTitle("CMD: Team Typing Test.")
		.setDescription("This command starts a team typing test with the specified users. The specified "
				+ "player list will be divided in half, with the first half being one team, and the second "
				+ "half representing the other.")
		.setColor(0x8D538D)
		.addField("Aliases","`None`", false)
		.addField("Syntax", "`\\teamvs [@RedPlayer1] [@RedPlayer2] [...] [@BluePlayer1] [@BluePlayer2] [...]`", false);
	
	public static final EmbedBuilder TYPING_NUMROW =
		new EmbedBuilder()
		.setTitle("CMD: Numrow Typing Test.")
		.setDescription("This command starts a number row typing test. The user will be prompted with a random "
				+ "string of numbers to type out.") 
		.setColor(0x8D538D)
		.addField("Aliases","`None`", false)
		.addField("Syntax", "`\\numrow`", false);
	
	public static final EmbedBuilder TYPING_QUIT =
		new EmbedBuilder()
		.setTitle("CMD: Quit Typing Test.")
		.setDescription("This command quits the current typing test, if there is one active.")
		.setColor(0x8D538D)
		.addField("Aliases","`typestop`, `tquit`, `tq`", false)
		.addField("Syntax", "`\\typequit`", false);
	
	public static final EmbedBuilder TYPING_STATS =
		new EmbedBuilder()
		.setTitle("CMD: Typing Statistics.")
		.setDescription("This command displays metrics of a user's typing statistics, collected "
				+ "from his/her typing tests so far.")
		.setColor(0x8D538D)
		.addField("Aliases","`tstats`, `ts`", false)
		.addField("Syntax", "`\\typestats [@User]`", false);
	
	public static final EmbedBuilder TYPING_LIST =
		new EmbedBuilder()
		.setTitle("CMD: Typing Prompts List.")
		.setDescription("This command displays a list of all available prompts.")
		.setColor(0x8D538D)
		.addField("Aliases","`tlist`, `tl`", false)
		.addField("Syntax", "`\\typelist [Page Number]`", false);
	
	public static final EmbedBuilder CHART =
			new EmbedBuilder()
			.setTitle("CMD: Chart.")
			.setDescription("This command displays a chart for a user's recent typing tests.")
			.setColor(0x8D538D)
			.addField("Aliases","`None`", false)
			.addField("Syntax", "`\\chart [@User]`", false);

	public static final EmbedBuilder LEADERBOARD =
			new EmbedBuilder()
			.setTitle("CMD: Leaderboard.")
			.setDescription("This command displays the global leaderboard for a user-specified statistic.")
			.setColor(0x8D538D)
			.addField("Aliases","`lboard`, `lb`", false)
			.addField("Syntax", "`\\leaderboard [-wpm | -acc] [-best | -avg] [...]`", false)
			.addField("Options",
					  "`-tp   :` Sorts by TP.\n"
					+ "`-wpm  :` Sorts by WPM.\n"
					+ "`-acc  :` Sorts by accuracy.\n"
					+ "`-best :` Sorts by global (overall) stats.\n"
					+ "`-avg  :` Sorts by average (recent 10 tests) stats.\n"
					+ "`-sum  :` Sorts by cumulative TP.\n"
					+ "`-tests:` Sorts by number of tests.\n"
					+ "`-p    :` Specifies a page number (Eg: `-p 2`).\n", false);

	public static final EmbedBuilder DAILY =
			new EmbedBuilder()
			.setTitle("CMD: Daily.")
			.setDescription("Displays a user's daily streak information, and when the next daily is available.")
			.setColor(0x8D538D)
			.addField("Aliases","`None`", false)
			.addField("Syntax", "`\\daily`", false);

	
	
	/*—————————————————————————————————————|
	|————————————————IN-DEV————————————————|
	|—————————————————————————————————————*/

	public static final EmbedBuilder DPI_CONVERTER =
		new EmbedBuilder()
		.setTitle("CMD: DPI Converter.")
		.setDescription("This command will convert the fullscreen mouse DPI (PPI) in one resolution to another, taking"
				+ " screen sizes into account, as well.")
		.addField("Aliases", "`dpiconv, dpicalculator, dpicalc`", false)
		.addField("Syntax", "\\dpiconverter", false)
		.setColor(0x8D538D);
}