# Changelog
This is a log of all changes that have been made to the project since the 0.1.0-beta version.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0-beta] - 2022-10-03
### Added
- Team Vs mode in the typing test, where players can go against each other in teams.
- A command that gets prompt data from the database.

### Changed
- The recalculate prompts method is now split into two methods, so that it is now possible to
  recalculate just a single prompt, if necessary.
- The command to recalculate prompts now sends that information to the database as well.
- Typing points (hereafter TP) has been rebalanced, so that the WPM scaling is much fairer than
  before; the multiplier has gone from a linear curve to a power curve.

### Fixed
- ADMIN Add prompt command now

## [0.1.0-beta] - 2022-09-26
### Added
- Ping Command: Returns the ping of the bot and Discord gateway.
- Help Command: Shows a list of (most) available commands and their syntaxes.
- Info Command: Shows basic bot info.
- Message Statistics (Scrape/Read) Commands: Scrapes messages in a channel, and retreives statistics
  from them (just character count for now) / and the ability to read this data.
- Type Start command: Starts a typing test.
- Type Quit command: Aborts a typing test.
- Type Stats command: Show a user's statistics gathered during his/her typing tests.
- Type Chart command: Shows a graph/chart of the user's previous 10 tests.
- Type Leaderboard command: Shows a leaderboard type of the user's choice...
  The best or average of (WPM/Accuracy/TP).
- ADMIN add test command: Adds a test to the database, mainly used for testing purposes.
- ADMIN clear profile command: Clears a user's profile.
- ADMIN find cheaters command: Attempts to find users who have scores that appear to be cheated.
- ADMIN remove cheaters command: Removes (clears the profile) suspicious users from the database.
- ADMIN add prompt command: Adds a typing prompt to the database.
- ADMIN recalculate prompts command: Recalculates the prompt rating and difficulty categorisations
  of all prompts in the database, back into the currently stored variables in the bot.
- ADMIN shutdown command: Shuts down the bot, in the case of an emergency.
- pull.sh file: Automatically pulls from the stable branch of this repository, once a week.

### Unreleased
- DPI converter command: Converts your DPI from one setup to another.
- Profile osu command: Shows your profile overview/stats.
- Settings osu command: Meant for another project, with Google sheets integration, where one's
  osu! playstyle settings can be stored on a spreadsheet, editable by a bot.