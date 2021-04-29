[![Java CI with Gradle](https://github.com/M0diis/M0-CoreCord/actions/workflows/gradle.yml/badge.svg)](https://github.com/M0diis/M0-CoreCord/actions/workflows/gradle.yml)

# M0-CoreCord
Discord integration for CoreProtect plugin.

The integration works both SQLite and MySQL databases.

Spigot page:
https://www.spigotmc.org/resources/m0-corecord.91805/

## Development
Building is quite simple.

Clone the repository or download the source code from releases.

Make sure you have gradle installed and run `gradlew shadowjar` to build the jar.

APIs used:

JDA Pagination Utils
- https://github.com/ygimenez/Pagination-Utils

SQLite driver
- https://github.com/xerial/sqlite-jdbc

MySQL driver
- https://dev.mysql.com/downloads/connector/j/

Java Discord API
- https://github.com/DV8FromTheWorld/JDA

Paper API
- https://github.com/PaperMC/Paper

## Dev-builds

All the development builds can be found on actions page.
Open the workflow and get the artifact from there.

https://github.com/M0diis/M0-OnlinePlayersGUI/actions

## Usage & Discord

To use this integration you have to have CoreProtect installed and have a Discord server.

You can find CoreProtect plugin here:
https://www.spigotmc.org/resources/coreprotect.8631/

To create a discord bot head to Discord Developer Portal.
You will have log in with your account and create a new application.

Developer portal is located here:
https://discord.com/developers/applications

Go the application settings menu and add a BOT user.
The token can be found in the BOT section. Make sure to not share it with anyone you don't trust. 

From there you can go to OAUTH section and invite your bot to your discord server.

You can find more information how to create a BOT on discord.py docs:
https://discordpy.readthedocs.io/en/stable/discord.html

