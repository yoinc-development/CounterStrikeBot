# CounterStrikeBot

[![GitHub release](https://img.shields.io/github/v/release/janesth/CounterStrikeBot)](https://github.com/janesth/CounterStrikeBot)

This bot lets Discord users check and compare each other's Counter Strike 2 stats, randomly split a voice channel into balanced teams, and get notified in a Discord channel whenever a tracked player finishes a new Leetify-tracked match. Each running instance of this bot is independent of the others and can be configured to suit your guild's needs.

## Features

### Slash Commands

- `/stats <player>` - mention a Discord user to display their Counter Strike 2 stats, resolved from their linked Steam account.
- `/compare <playerone> <playertwo>` - mention two Discord users to compare their Counter Strike 2 stats head-to-head.
- `/teams <amountofteams>` - shuffles the members of your current voice channel into `amountofteams` balanced teams (defaults to 2). You have to be in a voice channel yourself for this to work.

Commands are answered in English.

### Home channel redirect

If `discord.guildID` and `discord.channelID` are configured, command results triggered outside of that channel (but within that guild) are posted into the configured channel instead, and the requester is told a message was sent there.

### Leetify match notifications

Every hour, on the hour, the bot fetches the [Leetify](https://leetify.com) match history for every user Carthage knows a Steam account for, and posts an embed into the `discord.channelID` channel for every match that hasn't been seen before (tracked per-user via Carthage). If multiple tracked users played the same match together, a single embed lists all of them instead of sending one message per player.

## Configuration

All of these properties are defined in a `config.properties` file (see "Run the bot" below for how it's assembled). If you choose to run your own instance of the bot, please consider all of these properties to be mandatory. 

These properties are relevant to connect to Discord and to control where results get posted:
- `discord.apiToken` - visit the official [Discord Developers Portal](https://discord.com/developers/applications) to receive your individual bot token
- `discord.guildID` - the ID of the guild whose commands should be redirected to a home channel
- `discord.channelID` - the ID of that guild's home channel; this is also the channel Leetify match notifications are posted to

These properties are relevant for the Counter Strike stats feature:
- `steam.api` - Steam Web API key, used to fetch player stats from the Steam Web API
- `carthage.url` - See "What is Carthage" below.

This property is relevant for the Leetify match notifications feature:
- `leetify.apiToken` - Leetify API key, used to fetch player profiles and match history from the Leetify API

## Run the bot

Build with Maven, selecting the `local` or `prod` profile to decide which properties file gets used as `config.properties`:

```
mvn package -Plocal
```

This expects a `src/main/resources/config-local.properties` (or `config-prod.properties` for the `prod` profile) file containing the properties listed above; it gets copied to `config.properties` during the build. Then run the resulting shaded jar with `java -jar`.

## F.A.Q.

### Can I run the bot myself? Do I have to invite the public instance of the bot to my server?
Theoretically both are possible. You have to make sure that you set the variables defined in `config.properties` so your local instance can run.

### What is Carthage?
Carthage is a simple storage project for our private Discord, containing the link between Discord and Steam acount, given that this information is not available via JDA.

### Can I fork this and make it better?
Yes.
