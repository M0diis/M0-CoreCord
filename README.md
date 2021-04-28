[![Java CI with Gradle](https://github.com/M0diis/M0-CoreCord/actions/workflows/gradle.yml/badge.svg)](https://github.com/M0diis/M0-CoreCord/actions/workflows/gradle.yml)

# M0-CoreCord
Discord integration for CoreProtect plugin.

The integration currently works only with MySQL database
Local file lookups will be implemented in the future.

Spigot page:
https://www.spigotmc.org/resources/m0-corecord.91805/

## Building
Building is quite simple.

Clone the repository or download the source code from releases.
Make sure you have gradle installed and run `gradlew shadowjar`.

## Dev-builds

All the development builds can be found on actions page:
https://github.com/M0diis/M0-OnlinePlayersGUI/actions

## Usage & Discord

You use this integration you (of course) have to have CoreProtect installed and have a Discord server.

You can find CoreProtect plugin here:
https://www.spigotmc.org/resources/coreprotect.8631/

To create a discord bot head to Discord Developer Portal, log in with your account and create a new application.
Developer portal is located here:
https://discord.com/developers/applications

After that, go to its settings menu and add a bot user, here you will also find the token that has to be put in the config.
From there you can go to OAUTH section and invite your bot to your discord server.

You can find more information how to create a BOT on discord.py docs:
https://discordpy.readthedocs.io/en/stable/discord.html



