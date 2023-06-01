# RetakeBot

Allows a specific user group on a Discord server to execute "rcon changelevel <map_name>".

## Instructions

Add the following properties to "config.properties":
- `discord.apiToken` - https://discord.com/developers/applications to receive your individual bot token
- `discord.allowedRoleId` - the ID of a user group to use the command 
- `server.ip` - the IP address of the csgo server
- `server.port` - the port of the csgo server
- `server.password` - the RCON password of the csgo server (to define in `server.cfg`)
- `csgo.maps` - a comma seperated list of allowed maps to switch to (like `de_dust,de_tuscan,...`)
