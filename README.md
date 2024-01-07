# CounterStrikeBot

The purpose of this bot is to inform users of a Discord server in a specific channel about played matches.  
Continuing from its original purpose, it also allows a specific user group to execute "rcon changelevel <map_name>" to change the level on a to be specified retake server. 

## Configuration

All of these properties are defined in "config.properties".
These properties are relevant to connect to Discord and to be aware of the permitted user group:
- `discord.apiToken` - https://discord.com/developers/applications to receive your individual bot token
- `discord.allowedRoleId` - the ID of a user group to use the command 

These properties are currently relevant for the CS2 stats feature. 
- `` -
- `` -
- `` -
- `` -

The following properties are relevant to connect to the retake server and what maps are allowed to be played:
- `server.ip` - the IP address of the csgo server
- `server.port` - the port of the csgo server
- `server.password` - the RCON password of the csgo server (to define in `server.cfg`)
- `csgo.maps` - a comma seperated list of allowed maps to switch to (like `de_dust,de_tuscan,...`)
- `server.delay` - a delay (in seconds) to stop users from spamming a map change.

The next properties were relevant for a scratched commendation system. They can be empty as of now: 
- `server.ftp.ip` - the IP address to access the server using FTP
- `server.ftp.port` - the FTP port
- `server.ftp.user` - the FTP user (access has to be granted outside of this application)
- `server.ftp.password` - the FTP password
