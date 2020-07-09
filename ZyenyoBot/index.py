import discord, os, json, ast
from discord.ext import commands
import botconfig

client = commands.Bot(command_prefix = botconfig.PREFIX)

def replace_line(file_name, line_num, text):
    lines = open(file_name, 'r').readlines()
    lines[line_num] = text
    out = open(file_name, 'w')
    out.writelines(lines)
    out.close()

temp = open('../ZBotData/char_count_DB.json')
chc = json.loads(temp.readline())
temp.close()

@client.command()
async def load(ctx, extension):
    client.load_extension(f'cogs.{extension}')
@client.command()
async def unload(ctx, extension):
    client.unload_extension(f'cogs.{extension}')

for filename in os.listdir('./cogs'):
    if filename.endswith('.py'):
        client.load_extension(f'cogs.{filename[:-3]}')


@client.event
async def on_memer_join(member):
    pass

@client.event
async def on_member_remove(member):
    pass

@client.event
async def on_message(message):
    if message.author.bot:
        return
    char_count = len(message.content)
    un = message.author.id
    if f'{un}_tmc' in chc:
        chc[f'{un}'] = chc[f'{un}'] + char_count
        chc[f'{un}_tmc'] = chc[f'{un}_tmc'] + 1
        replace_line("../ZBotData/char_count_DB.json", 0, json.dumps(chc))
    else:
        chc[f'{un}'] = char_count
        chc[f'{un}_tmc'] = 1
        replace_line("../ZBotData/char_count_DB.json", 0, json.dumps(chc))
    await client.process_commands(message)


@client.command()
async def stats(message):
    un = message.author.id
    totalChars = chc[f'{un}']
    totalMsgs = chc[f'{un}_tmc']
    tAvgChar = round(totalChars / totalMsgs, 2)
    await message.channel.send(f'Average character count: . . . . . . . . . . **`{tAvgChar}`**\nTotal character count: . . . . . . . . . . . . . **`{totalChars}`**\nTotal messages sent: . . . . . . . . . . . . . . **`{totalMsgs}`**')


client.run(botconfig.token)
