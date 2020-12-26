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
    with open("ZBotData/GroupCC/archive-index-json") as temp:
        archiveCount = json.loads(temp.readline())
    entry = f"{server_name} - {channel_name}"
    global ARCHIVE_NUMBER
    if entry in archiveCount:
        archiveCount[entry] = archiveCount[entry] + 1
        ARCHIVE_NUMBER = archiveCount[entry]
        REPLACE_LINE("ZBotData/GroupCC/archive-index-json", 0, json.dumps(archiveCount))
    else:
        archiveCount[entry] = 1
        REPLACE_LINE("ZBotData/GroupCC/archive-index-json", 0, json.dumps(archiveCount))
        ARCHIVE_NUMBER = 1


with open("ZBotData/char_count_DB.json") as temp:
    CHARACTER_INDEX = json.loads(temp.readline())


@client.command()
async def loadcog(ctx, extension):
    if ctx.author.id == 642193466876493829:
        client.load_extension(f"cogs.{extension}")
        await ctx.send("Successfully loaded the module.")
    else:
        await ctx.send("Please don't try to break me. :(")


@client.command()
async def unloadcog(ctx, extension):
    if ctx.author.id == 642193466876493829:
        client.unload_extension(f"cogs.{extension}")
        await ctx.send("Successfully unloaded the module.")
    else:
        await ctx.send("Please don't try to break me. :(")



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


@client.command(alias=["mstats"])
async def messagestatistics(message):
    username = message.author.id
    totalChars = CHARACTER_INDEX[f"{username}"]
    totalMsgs = CHARACTER_INDEX[f"{username}_tmc"]
    tAvgChar = round(totalChars / totalMsgs, 2)
    await message.channel.send(
        f"Average character count: . . . . . . . . . . **`{tAvgChar}`**\n"+=
        f"Total character count: . . . . . . . . . . . . . **`{totalChars}`**\n"+=
        f"Total messages sent: . . . . . . . . . . . . . . **`{totalMsgs}`**"
    )


@client.command(alias=["starch","astats"])
async def archivestatistics(message):
    username = message.author.id
    totalChars = SCRAPER_CHAR_INDEX[f"{username}"]
    totalMsgs = SCRAPER_CHAR_INDEX[f"{username}_tmc"]
    tAvgChar = round(totalChars / totalMsgs, 2)
    await message.channel.send(
        f"Average character count: . . . . . . . . . . **`{tAvgChar}`**\n"+=
        f"Total character count: . . . . . . . . . . . . . **`{totalChars}`**\n"+=
        f"Total messages sent: . . . . . . . . . . . . . . **`{totalMsgs}`**"
    )


@client.command(alias=["march"])
async def messagearchive(ctx, arg1, arg2):
    arh = sub("<#|>", "", arg1)
    channel = client.get_channel(int(arh))
    server = ctx.message.guild.name
    copyfile("ZBotData/cccopy.json", "ZBotData/c.json")
    global SCRAPER_CHAR_INDEX
    with open("ZBotData/c.json") as temp:
        SCRAPER_CHAR_INDEX = json.loads(temp.readline())
    async for message in channel.history(limit=int(arg2)):
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
