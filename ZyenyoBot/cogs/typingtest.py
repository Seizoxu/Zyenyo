import discord, time, sys, random, asyncio
from discord.ext import commands
sys.path.append('../')
import typeconfig
sys.path.append('ZyenyoBot/')
class typingtest(commands.Cog):
    def __init__(self, client):
        self.client = client
    @commands.command(aliases=['ttest', 'tts'])
    async def typingtest(self, message):
        finishOrder = 1
        channel = message.channel
        script = random.choice(list(typeconfig.paragraphs))
        length = typeconfig.lengths[script]
        duration = round(length*(60/4.5)/30, 1)
        script = typeconfig.paragraphs[script]
        await message.send(f'You have {duration} seconds to finish this test:\n{script}')
        time.sleep(1)
        timeStart = time.time()
        def check(m):
            if m.channel == channel and m.content == 'typestop':
                raise ValueError('Cancelled typetest.')
            return m.content == script and m.channel == channel
        while length > 0:
            try:
                msg = await self.client.wait_for('message', check=check,timeout=duration)
            except:
                pass
            timeEnd = time.time() - timeStart
            author = str(message.author)
            wpm = round((length*60/4.5)/timeEnd, 2)
            try:
                await channel.send(f'{author[:-5]} has finished in place {finishOrder}, with a time of {round(timeEnd, 2)} seconds, with a speed of {wpm} WPM!'.format(msg))
            except:
                pass
            finishOrder = finishOrder + 1
            length = 0

def setup(client):
    client.add_cog(typingtest(client))