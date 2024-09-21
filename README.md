# CounterStrikeBot

[![GitHub release](https://img.shields.io/github/v/release/janesth/CounterStrikeBot)](https://github.com/janesth/CounterStrikeBot)

This bot enables Discord users to join a custom retake server, inform members of a specific guild about Counter Strike stats or lets you share your favourite gaming moment. Each running instance of this bot is independent of each other and be configured to suit your guild's needs.

## Features

### Slash Commands

- `/map <map>` - allows users to change the current map on the retake server. For users to be allowed to use this command, they must be assigned to a designated role - defined in the properties below. `map` contains the prefered map - also to be defined in a property.
- `/stas <steamID>` - display this user's Counter Strike stats based on their Steam account. `steamID` is the user's [steam ID](https://www.steamidfinder.com/).
- `/comapre <steamID1> <steamID2>` - compares two users' Counter Strike stats.
- `/teams <amount>` - divides the current members of a voice channel into teams. `amount` defines the amount of teams to be created. The requester has to be in a voice chat for this feature to work.
- `/wow <url>` - allows to set a personal gaming moment (wow moment). `url` has to be either a YouTube or a Discord url.
- `/status` - returns the current status of the retake server.

### User Context Commands

To execute these commands, right-click on a user, choose "Apps" and then one of the followings commands:

- `wow` - returns this user's wow moment for everyone to see.
- `retake stats` - return this user's stats on the set retake server.

### Scheduled Tasks

There are two Counter Strike relavant scheduled tasks running:

- A collection task to receive discord user information to enable all users to use relevant commands.
- A join task to send an invite to the retake server after a user has joined the server.

## Configuration

All of these properties are defined in "config.properties". If you choose to run your own instance of the bot, please consider all these properties to be mandatory. Due to privacy reasons we won't be commiting our own properties into this repository.

These properties are relevant to connect to Discord and to limit this bot's functionalities by various factors:
- `discord.apiToken` - visit the official [Discord Developers Portal](https://discord.com/developers/applications) to receive your individual bot token
- `discord.allowedRoleId` - the ID of a user group to use commands
- `discord.thisIsMyHome` - the ID of the server's home base

These properties are currently relevant for the CS2 stats feature. 
- `steam.api` - Steam Web API key

The following properties were relevant to connect to the retake server and what maps are allowed to be played:
- `server.ip` - the IP address of the csgo server
- `server.port` - the port of the csgo server
- `server.password` - the RCON password of the csgo server (to define in `server.cfg`)
- `csgo.maps` - a comma seperated list of allowed maps to switch to (like `de_dust,de_tuscan,...`)
- `server.delay` - a delay (in seconds) to stop users from spamming a map change.
- `server.connectLink` - a link to an external website to redirect to the game server (see [this reddit thread](https://www.reddit.com/r/discordapp/comments/13kk1bz/discord_has_stopped_to_support_steam_links_why/) as to why a direct link to Steam isn't possible)

The next properties were relevant for a scratched commendation system: 
- `server.ftp.ip` - the IP address to access the server using FTP
- `server.ftp.port` - the FTP port
- `server.ftp.user` - the FTP user (access has to be granted outside of this application)
- `server.ftp.password` - the FTP password

The last property is relevant regarding the bot's wow feature:
- `db.url` - the URL of a database for the bot to store all submitted wow clips

## Run the bot

TO BE DEFINED

## F.A.Q.

### Can I run the bot myself? Do I have to invite the public instance of the bot to my server??
It would be recommended to run the bot yourself, even though you are able to invite any running instance of it to your server. The downside of inviting an already running instance to your server would be the lack of customization you could do (your own retake server ip, roles, etc.).

### Can I fork this and make it better? 
Yes.