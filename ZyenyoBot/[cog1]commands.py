import discord
from discord.ext import commands
class commands(commands.Cog):
    def __init__(self, client):
        self.client = client
    
    @commands.command(aliases=['help', '?'])
    async def commands(self, ctx):
        await ctx.send(f'**This is the help section. It is currently under development.**')
def setup(client):
    client.add_cog(commands(client))