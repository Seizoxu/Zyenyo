import discord
from discord.ext import commands

from . import botconfig
from .cogs.ping import ping
from .cogs.stats import stats

client = commands.Bot(command_prefix=botconfig.PREFIX)


client.add_cog(ping(client))
client.add_cog(stats(client))

# A list for the following commands.
coglist = {
    "ping"  : ping,
    "stats" : stats
}

####################
#####[COMMANDS]#####
####################


# loadcog and unloadcog aren't meant to be used regularly; they exist for testing purposes.
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
