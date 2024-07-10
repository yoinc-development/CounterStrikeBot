# CounterStrikeBot

[![GitHub release](https://img.shields.io/github/v/release/janesth/CounterStrikeBot)](https://github.com/janesth/CounterStrikeBot)


The purpose of this bot is to inform users of a Discord server in a specific channel about played matches.  
Continuing from its original purpose, it also allows a specific user group to execute "rcon changelevel <map_name>" to change the level on a to be specified retake server. 

## Configuration

All of these properties are defined in "config.properties". Mandatory properties are **bold**.

These properties are relevant to connect to Discord and to limit this bot's functionalities by various factors:
- **`discord.apiToken` - https://discord.com/developers/applications to receive your individual bot token**
- `discord.allowedRoleId` - the ID of a user group to use commands
- `discord.dedicatedChannel` - the ID of a dedicated text channel for the bot to use
- `discord.dedicatedVoiceChannel` - the ID of a dedicated voice channel for the bot to use

These properties are currently relevant for the CS2 stats feature. 
- **`steam.api` - Steam Web API key**

The following properties were relevant to connect to the retake server and what maps are allowed to be played:
- `server.ip` - the IP address of the csgo server
- `server.port` - the port of the csgo server
- `server.password` - the RCON password of the csgo server (to define in `server.cfg`)
- `csgo.maps` - a comma seperated list of allowed maps to switch to (like `de_dust,de_tuscan,...`)
- `server.delay` - a delay (in seconds) to stop users from spamming a map change.

The next properties were relevant for a scratched commendation system: 
- `server.ftp.ip` - the IP address to access the server using FTP
- `server.ftp.port` - the FTP port
- `server.ftp.user` - the FTP user (access has to be granted outside of this application)
- `server.ftp.password` - the FTP password

The last property is revelant regarding the bot's wow feature:
- **`db.url` - the URL of a database for the bot to store all submitted wow clips**

## Run the bot

TO BE DEFINED

## F.A.Q.

### Why do I need to run the bot myself? Is there not a public version already running for me to invite to my own server?
Short answer: there is a public version of the bot, but its wow, map and teams features are tailored to a specific server.

Long answer: allowing access to these properties allows for flexibility in the usage of the bot on individual servers. Some Discord server owners struggle
with permissions for individual text channels (bot dedicated channels). It can be quite a hassle to have synced permissions across a category 
containing a different variety of text / voice channels. Setting a dedicated id for both text and voice channel allows the user to set limits within the bot 
without it relying on permissions of the server. The public version of this bot is currently limited by these dedicated channels for the wow, map and teams features 
and therefore can not be invited to other servers - the same can not be said for the stats or compare features. These would work regardless of restrictions.

### Why doesn't ``/map`` work?
SourceMod is not compatible with Counter Strike 2.

### Can I fork this and make it better? 
Yes.