import discord, os, json, time
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

with open("ZBotData/activeTimeDB.json") as temp:
    ACTIVITY_INDEX = json.loads(temp.readline())

class Statistics(commands.Cog):
    def __init__(self, client):
        self.client = client

    @commands.Cog.listener()
    async def on_message(self, message):
        if message.author.bot: # or message.content[0] in botPrefixes:
            return
        
        # Fetches message stats.
        character_count = len(message.content)
        username = str(message.author.id)
        timestamp = int(time.time())
        
        # Updates userdata / checks for new user.
        if username in CHARACTER_INDEX:
            CHARACTER_INDEX[username] = CHARACTER_INDEX[username] + character_count
            CHARACTER_INDEX[username + "_tmc"] = CHARACTER_INDEX[username + "_tmc"] + 1
            timeSince = timestamp - ACTIVITY_INDEX[username + "_l"]
            if (timeSince) <= 300:
                ACTIVITY_INDEX[username + "_as"] = ACTIVITY_INDEX[username + "_as"] + timeSince
            ACTIVITY_INDEX[username + "_l"] = timestamp
        else:
            CHARACTER_INDEX[username] = character_count
            CHARACTER_INDEX[username + "_tmc"] = 1
            ACTIVITY_INDEX[username + "_l"] = timestamp
            ACTIVITY_INDEX[username + "_as"] = 0
        
        # Adds to total character and message counts.
        CHARACTER_INDEX["tcc"] = CHARACTER_INDEX["tcc"] + character_count
        CHARACTER_INDEX["tmc"] = CHARACTER_INDEX["tmc"] + 1
        REPLACE_LINE("ZBotData/char_count_DB.json", 0, json.dumps(CHARACTER_INDEX))
        REPLACE_LINE("ZBotData/activeTimeDB.json", 0, json.dumps(ACTIVITY_INDEX))


    @commands.command(aliases=["march"])
    async def messagearchive(self, ctx, channelName, scrapeLimit):
        # Fetches identifying information.
        startTime = time.time()
        nakedID = sub("<#|>", "", str(channelName))
        channel = self.client.get_channel(int(nakedID))
        server = ctx.message.guild.name
        copyfile("ZBotData/cccopy.json", "ZBotData/c.json")
        global SCRAPER_CHAR_INDEX
        with open("ZBotData/c.json") as temp:
            SCRAPER_CHAR_INDEX = json.loads(temp.readline())
        
        # Scrapes as many messages as the specified limit.
        async for message in channel.history(limit=int(scrapeLimit)):
            if message.author.bot:
                pass
            else:
                character_count = len(message.content)
                username = str(message.author.id)
                if username in SCRAPER_CHAR_INDEX:
                    SCRAPER_CHAR_INDEX[username] = SCRAPER_CHAR_INDEX[username] + character_count
                    SCRAPER_CHAR_INDEX[username + "_tmc"] = SCRAPER_CHAR_INDEX[username + "_tmc"] + 1
                else:
                    SCRAPER_CHAR_INDEX[username] = character_count
                    SCRAPER_CHAR_INDEX[username + "_tmc"] = 1
                SCRAPER_CHAR_INDEX["tcc"] = SCRAPER_CHAR_INDEX["tcc"] + character_count
                SCRAPER_CHAR_INDEX["tmc"] = SCRAPER_CHAR_INDEX["tmc"] + 1
                REPLACE_LINE("ZBotData/c.json", 0, json.dumps(SCRAPER_CHAR_INDEX))
        
        # Uses the identifying information to make a unique file name.
        ARCHIVE_NAME(server, channel)
        os.rename(
            "ZBotData/c.json", f"ZBotData/GroupCC/{server} - {channel} ({ARCHIVE_NUMBER}).json"
        )
        timeTaken = time.time() - startTime
        await ctx.send(
            "Done!\n"
            f"Time Elapsed: **{round(timeTaken/60, 3)} minutes.**"
        )


    # mstats are whatever data the bot has collected passively (lines 42-70).
    @commands.command(aliases=["mstats"])
    async def messagestatistics(self, ctx):
        username = str(ctx.author.id)
        totalChars = CHARACTER_INDEX[username]
        totalMsgs = CHARACTER_INDEX[username + "_tmc"]
        tAvgChar = round(totalChars / totalMsgs, 2)
        await ctx.send(
            f"Average character count: . . . . . . . . . . **`{tAvgChar}`**\n"
            f"Total character count: . . . . . . . . . . . . . **`{totalChars}`**\n"
            f"Total messages sent: . . . . . . . . . . . . . . **`{totalMsgs}`**"
        )
        
        
    # starch is whatever data has been actively scraped from a server (lines 73-111).
    @commands.command(aliases=["starch","astats"])
    async def archivestatistics(self, ctx):
        username = str(ctx.author.id)
        totalChars = SCRAPER_CHAR_INDEX[username]
        totalMsgs = SCRAPER_CHAR_INDEX[username + "_tmc"]
        tAvgChar = round(totalChars / totalMsgs, 2)
        await ctx.send(
            f"Average character count: . . . . . . . . . . **`{tAvgChar}`**\n"
            f"Total character count: . . . . . . . . . . . . . **`{totalChars}`**\n"
            f"Total messages sent: . . . . . . . . . . . . . . **`{totalMsgs}`**"
        )


    # Displays the user's active time and their latest message time.
    @commands.command(aliases=["activity", "act"])
    async def activetime(self, ctx):
        username = str(ctx.author.id)
        activityHours = round(ACTIVITY_INDEX[username + "_as"] / 3600, 3)
        activityMinutes = round((activityHours - int(activityHours)) * 60, 3)
        activitySeconds = (activityMinutes - int(activityMinutes)) * 60
        lastMessage = time.asctime(time.localtime(ACTIVITY_INDEX[username + "_l"]))
        await ctx.send(
            f"Total active time: . . . . . . . . . . . . . . **`{int(activityHours)}h {int(activityMinutes)}min {int(activitySeconds)}s`.**\n"
            f"Most recent message: . . . . . . . . . . **`{lastMessage}`**\n"
        )
        
    
    @commands.command(aliases=["clb"])
    async def characterleaderboard(self, ctx, page):
        charLB = {}
        charKeys = list(CHARACTER_INDEX.keys())
        page = int(page)
        if page > 10: page = 10
        
        # Edit and sort the dictionary.
        for i in range(len(CHARACTER_INDEX)):
            if (i % 2) == 0:
                charLB.update({charKeys[i]: CHARACTER_INDEX[charKeys[i]]})
        charLB = dict(sorted(charLB.items(), key = lambda kv: kv[1], reverse=True))
        del charLB["tcc"]
        
        # Getting ready to format the message.
        charKeys = list(charLB.keys())
        charValues = list(charLB.values())
        lbMessage = f"Showing {page * 20 - 19} to {page * 20} of the total characters leaderboard.\n"
        usernames = []
        
        # Make sure all deleted users are handled.
        for i in range((page * 20 - 20), (page * 20)):
            try:
                usernames.append(await self.client.fetch_user(int(charKeys[i])))
            except Exception as e:
                usernames.append("deletedUser#0000")
        
        # Format the message, and send it.
        for i in range((page * 20 - 20), (page * 20)):
            if i < (page * 20) and i >= (page * 20 - 20):
                username = usernames[i - ((page - 1) * 20)]
                lbMessage = lbMessage + f"{i + 1}. {username}: **`{charValues[i]}`**\n"
        await ctx.send(lbMessage)
    
    
    @commands.command(aliases=["alb"])
    async def activityleaderboard(self, ctx, page):
        activityLB = {}
        activityKeys = list(ACTIVITY_INDEX.keys())
        page = int(page)
        if page > 10: page = 10
        
        # Edit and sort the dictionary.
        for i in range(len(ACTIVITY_INDEX)):
            if (i % 2) == 0:
                activityLB.update({activityKeys[i]: ACTIVITY_INDEX[activityKeys[i]]})
        activityLB = dict(sorted(activityLB.items(), key = lambda kv: kv[1], reverse=True))
        del activityLB["tas"]
        
        # Getting ready to format the message.
        activityKeys = list(activityLB.keys())
        activityValues = list(activityLB.values())
        lbMessage = f"Showing {page * 20 - 19} to {page * 20} of the total activity leaderboard.\n"
        usernames = []
        
        # Make sure all deleted users are handled.
        for i in range((page * 20 - 20), (page * 20)):
            try:
                usernames.append(await self.client.fetch_user(int(activityKeys[i])))
            except Exception as e:
                usernames.append("deletedUser#0000")
        
        # Format the message, and send it.
        for i in range((page * 20 - 20), (page * 20)):
            if i < (page * 20) and i >= (page * 20 - 20):
                username = usernames[i - ((page - 1) * 20)]
                lbMessage = lbMessage + f"{i + 1}. {username}: **`{activityValues[i]}`**\n"
        await ctx.send(lbMessage)
