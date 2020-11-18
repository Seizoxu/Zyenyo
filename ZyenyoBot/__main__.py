from .index import client
import os

client.run(os.environ["BOT_TOKEN"])