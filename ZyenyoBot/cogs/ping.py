import discord
from discord.ext import commands


class ping(commands.Cog):
    def __init__(self, client):
        self.client = client

    @commands.Cog.listener()
    async def on_ready(self):
        await self.client.change_presence(
            status=discord.Status.online, activity=discord.Game("with Rosogolla.")
        )
        print("Zyen is ready.")

    @commands.command()
    async def ping(self, ctx):
        await ctx.send(f"**Pong! ({round(self.client.latency*1000, 2)} ms)**")


def setup(client):
    client.add_cog(ping(client))
