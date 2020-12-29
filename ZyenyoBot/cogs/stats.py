import discord, os, json
from discord.ext import commands
from shutil import copyfile
from re import sub

botPrefixes = [">", ".", "!", "-", "_", "~", "`", "?"]


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

######################

with open("ZBotData/char_count_DB.json") as temp:
    CHARACTER_INDEX = json.loads(temp.readline())

class stats(commands.Cog):
    def __init__(self, client):
        self.client = client

    @commands.Cog.listener()
    async def on_message(self, message):
        if message.author.bot or message.content[0] in botPrefixes:
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
    
    
    #mstats are whatever data the bot has collected passively (lines 39-51).
    @commands.command(aliases=["mstats"])
    async def messagestatistics(self, ctx):
        username = ctx.author.id
        totalChars = CHARACTER_INDEX[f"{username}"]
        totalMsgs = CHARACTER_INDEX[f"{username}_tmc"]
        tAvgChar = round(totalChars / totalMsgs, 2)
        await ctx.send(
            f"Average character count: . . . . . . . . . . **`{tAvgChar}`**\n"
            f"Total character count: . . . . . . . . . . . . . **`{totalChars}`**\n"
            f"Total messages sent: . . . . . . . . . . . . . . **`{totalMsgs}`**"
        )
        
        
    # starch is whatever data has been actively scraped from a server (lines 82-109).
    @commands.command(aliases=["starch","astats"])
    async def archivestatistics(self, ctx):
        username = ctx.author.id
        totalChars = SCRAPER_CHAR_INDEX[f"{username}"]
        totalMsgs = SCRAPER_CHAR_INDEX[f"{username}_tmc"]
        tAvgChar = round(totalChars / totalMsgs, 2)
        await ctx.send(
            f"Average character count: . . . . . . . . . . **`{tAvgChar}`**\n"
            f"Total character count: . . . . . . . . . . . . . **`{totalChars}`**\n"
            f"Total messages sent: . . . . . . . . . . . . . . **`{totalMsgs}`**"
        )
        
        
    @commands.command(aliases=["march"])
    async def messagearchive(self, ctx, channelName, scrapeLimit):
        nakedID = sub("<#|>", "", str(channelName))
        channel = self.client.get_channel(int(nakedID))
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
