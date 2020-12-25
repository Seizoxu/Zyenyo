import discord, os, json, ast
from discord.ext import commands
from shutil import copyfile
from re import sub
from . import botconfig

# I'm importing something here, the fix-me at line 72 should tell you why,
from .cogs.ping import ping


client = commands.Bot(command_prefix=botconfig.PREFIX)

# This is usually how you'd want to register cogs, not using load_extensions (load_extensions is messy.)
# Also Documentation Time: https://discordpy.readthedocs.io/en/latest/ext/commands/cogs.html#cog-registration
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
    temp = open("ZBotData/GroupCC/archive-index-json")
    archiveCount = json.loads(temp.readline())
    temp.close()
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



# FIXME(A BETTER WAY): There's a better way to do this: Python Docs on the following:
# https://docs.python.org/3/reference/compound_stmts.html#the-with-statement
temp = open("ZBotData/char_count_DB.json")
chc = json.loads(temp.readline())
temp.close()


@client.command()
async def load(ctx, extension): #FIXME(Naming): Load What exactly?
    if ctx.author.id == 642193466876493829:
        client.load_extension(f"cogs.{extension}")
        await ctx.send("Successfully loaded the module.")
    else:
        await ctx.send("Please don't try to break me. :(")


@client.command()
async def unload(ctx, extension): #FIXME(Naming): unload What exactly?
    if ctx.author.id == 642193466876493829:
        client.unload_extension(f"cogs.{extension}")
        await ctx.send("Successfully unloaded the module.")
    else:
        await ctx.send("Please don't try to break me. :(")


# FIXME(DO NOT DO THIS): And for good reason, while this is what you'd call a quick hack (and it works),
# It's a very bad thing to do. This one is a freebie, from me :)
# for filename in os.listdir('./cogs'):
#     if filename.endswith('.py'):
#         client.load_extension(f'cogs.{filename[:-3]}')


####################
#####[COMMANDS]#####
####################

#FIXME(Style, Flow): So, there's one line that you're doing regardless of condition.
# So why put them in both blocks? Seperate them.
@client.event
async def on_message(message):
    if message.author.bot:
        return
    char_count = len(message.content)
    un = message.author.id
    if f"{un}_tmc" in chc:
        chc[f"{un}"] = chc[f"{un}"] + char_count
        chc[f"{un}_tmc"] = chc[f"{un}_tmc"] + 1
        REPLACE_LINE("ZBotData/char_count_DB.json", 0, json.dumps(chc))
    else:
        chc[f"{un}"] = char_count
        chc[f"{un}_tmc"] = 1
        REPLACE_LINE("ZBotData/char_count_DB.json", 0, json.dumps(chc))
    await client.process_commands(message)

# FIXME(READABILITY) While I love the usage of template strings, it's best used when it's a smaller sentence.
# The Old += and \n at the end of a string is far more readable.
@client.command()
async def stats(message):
    un = message.author.id
    totalChars = chc[f"{un}"]
    totalMsgs = chc[f"{un}_tmc"]
    tAvgChar = round(totalChars / totalMsgs, 2)
    await message.channel.send(
        f"Average character count: . . . . . . . . . . **`{tAvgChar}`**\nTotal character count: . . . . . . . . . . . . . **`{totalChars}`**\nTotal messages sent: . . . . . . . . . . . . . . **`{totalMsgs}`**"
    )


@client.command()
async def cstats(message):
    un = message.author.id
    totalChars = bchc[f"{un}"]
    totalMsgs = bchc[f"{un}_tmc"]
    tAvgChar = round(totalChars / totalMsgs, 2)
    await message.channel.send(
        f"Average character count: . . . . . . . . . . **`{tAvgChar}`**\nTotal character count: . . . . . . . . . . . . . **`{totalChars}`**\nTotal messages sent: . . . . . . . . . . . . . . **`{totalMsgs}`**"
    )


## bchc is Big-CHaracter-Count.


@client.command()
async def carch(ctx, arg1, arg2): #FIXME(Naming) What *is* carch.
    arh = sub("<#|>", "", arg1)
    channel = client.get_channel(int(arh))
    server = ctx.message.guild.name
    copyfile("ZBotData/cccopy.json", "ZBotData/c.json")
    temper = open("ZBotData/c.json")
    global bchc
    bchc = json.loads(temper.readline())
    temper.close()
    async for message in channel.history(limit=int(arg2)):
        if message.author.bot:
            pass
        else:
            char_count = len(message.content)
            un = message.author.id
            if f"{un}_tmc" in bchc:
                bchc[f"{un}"] = bchc[f"{un}"] + char_count
                bchc[f"{un}_tmc"] = bchc[f"{un}_tmc"] + 1
                REPLACE_LINE("ZBotData/c.json", 0, json.dumps(bchc))
            else:
                bchc[f"{un}"] = char_count
                bchc[f"{un}_tmc"] = 1
                REPLACE_LINE("ZBotData/c.json", 0, json.dumps(bchc))
    ARCHIVE_NAME(server, channel)
    os.rename(
        "ZBotData/c.json", f"ZBotData/GroupCC/{server} - {channel} ({ARCHIVE_NUMBER}).json"
    )
    await ctx.send("Done!")
