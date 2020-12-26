import discord, os, json, ast
from discord.ext import commands
from shutil import copyfile
from re import sub

from . import botconfig
from .cogs.ping import ping


client = commands.Bot(command_prefix=botconfig.PREFIX)


client.add_cog(ping(client))


###################
###[DEFINITIONS]###
###################


def REPLACE_LINE(file_name, line_num, text):
    lines = open(file_name, "r").readlines()
    lines[line_num] = text
    out = open(file_name, "w")
    out.writelines(lines)
    out.close()


def ARCHIVE_NAME(server_name, channel_name):
    with open("ZBotData/GroupCC/archive-index.json") as temp:
        archiveCount = json.loads(temp.readline())
    entry = f"{server_name} - {channel_name}"
    global ARCHIVE_NUMBER
    if entry in archiveCount:
        archiveCount[entry] = archiveCount[entry] + 1
        ARCHIVE_NUMBER = archiveCount[entry]
    else:
        archiveCount[entry] = 1
        ARCHIVE_NUMBER = 1
    REPLACE_LINE("ZBotData/GroupCC/archive-index.json", 0, json.dumps(archiveCount))


with open("ZBotData/char_count_DB.json") as temp:
    CHARACTER_INDEX = json.loads(temp.readline())

# loadcog and unloadcog aren't meant to be used regularly; they exist for testing purposes.
coglist = {
    "ping" : ping
}
@client.command(aliases=["load"], hidden=True)
@commands.is_owner()
async def loadcog(ctx, cog):
    if cog in coglist:
        client.add_cog(coglist[cog](client))
        await ctx.send("Successfully loaded the cog.")
    else:
        await ctx.send("The specified cog does not exist.")


@client.command(aliases=["unload"], hidden=True)
@commands.is_owner()
async def unloadcog(ctx, cog):
    if cog in coglist:
        client.remove_cog(cog)
        await ctx.send("Successfully unloaded the cog.")
    else:
        await ctx.send("The specified cog does not exist.")


####################
#####[COMMANDS]#####
####################


@client.event
async def on_message(message):
    if message.author.bot:
        return
    char_count = len(message.content)
    username = message.author.id
    if f"{username}_tmc" in CHARACTER_INDEX:
        CHARACTER_INDEX[f"{username}"] = CHARACTER_INDEX[f"{username}"] + char_count
        CHARACTER_INDEX[f"{username}_tmc"] = CHARACTER_INDEX[f"{username}_tmc"] + 1
    else:
        CHARACTER_INDEX[f"{username}"] = char_count
        CHARACTER_INDEX[f"{username}_tmc"] = 1
    REPLACE_LINE("ZBotData/char_count_DB.json", 0, json.dumps(CHARACTER_INDEX))
    await client.process_commands(message)

#mstats are whatever data the bot has collected passively (lines 71-84).
@client.command(aliases=["mstats"])
async def messagestatistics(message):
    username = message.author.id
    totalChars = CHARACTER_INDEX[f"{username}"]
    totalMsgs = CHARACTER_INDEX[f"{username}_tmc"]
    tAvgChar = round(totalChars / totalMsgs, 2)
    await message.channel.send(
        f"Average character count: . . . . . . . . . . **`{tAvgChar}`**\n"
        f"Total character count: . . . . . . . . . . . . . **`{totalChars}`**\n"
        f"Total messages sent: . . . . . . . . . . . . . . **`{totalMsgs}`**"
    )

# starch is whatever data has been actively scraped from a server (lines 113-140).
@client.command(aliases=["starch","astats"])
async def archivestatistics(message):
    username = message.author.id
    totalChars = SCRAPER_CHAR_INDEX[f"{username}"]
    totalMsgs = SCRAPER_CHAR_INDEX[f"{username}_tmc"]
    tAvgChar = round(totalChars / totalMsgs, 2)
    await message.channel.send(
        f"Average character count: . . . . . . . . . . **`{tAvgChar}`**\n"
        f"Total character count: . . . . . . . . . . . . . **`{totalChars}`**\n"
        f"Total messages sent: . . . . . . . . . . . . . . **`{totalMsgs}`**"
    )


@client.command(aliases=["march"])
async def messagearchive(ctx, channelName, scrapeLimit):
    nakedID = sub("<#|>", "", channelName)
    channel = client.get_channel(int(nakedID))
    server = ctx.message.guild.name
    copyfile("ZBotData/cccopy.json", "ZBotData/c.json")
    global SCRAPER_CHAR_INDEX
    with open("ZBotData/c.json") as temp:
        SCRAPER_CHAR_INDEX = json.loads(temp.readline())
    async for message in channel.history(limit=int(scrapeLimit)):
        if message.author.bot:
            pass
        else:
            char_count = len(message.content)
            username = message.author.id
            if f"{username}_tmc" in SCRAPER_CHAR_INDEX:
                SCRAPER_CHAR_INDEX[f"{username}"] = SCRAPER_CHAR_INDEX[f"{username}"] + char_count
                SCRAPER_CHAR_INDEX[f"{username}_tmc"] = SCRAPER_CHAR_INDEX[f"{username}_tmc"] + 1
                REPLACE_LINE("ZBotData/c.json", 0, json.dumps(SCRAPER_CHAR_INDEX))
            else:
                SCRAPER_CHAR_INDEX[f"{username}"] = char_count
                SCRAPER_CHAR_INDEX[f"{username}_tmc"] = 1
                REPLACE_LINE("ZBotData/c.json", 0, json.dumps(SCRAPER_CHAR_INDEX))
    ARCHIVE_NAME(server, channel)
    os.rename(
        "ZBotData/c.json", f"ZBotData/GroupCC/{server} - {channel} ({ARCHIVE_NUMBER}).json"
    )
    await ctx.send("Done!")
