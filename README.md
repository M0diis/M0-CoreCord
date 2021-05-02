[![Java CI with Gradle](https://github.com/M0diis/M0-CoreCord/actions/workflows/gradle.yml/badge.svg)](https://github.com/M0diis/M0-CoreCord/actions/workflows/gradle.yml)

# M0-CoreCord
Discord integration for CoreProtect plugin.

The integration works both SQLite and MySQL databases.

Spigot page:
https://www.spigotmc.org/resources/m0-corecord.91863/

## Development
Building is quite simple.

To build CoreCord, you need JDK 8 or higher installed on your system.

Clone the repository or download the source code from releases.  
Make sure you have gradle installed and run `gradlew shadowjar` to build the jar.  
The jar can be found in `/build/libs/` folder. 

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

You also need to create and invite a Bot User to your Discord server.

You can find all the required information on how to do so the Wiki:  
https://github.com/M0diis/M0-CoreCord/wiki/Discord-BOT

## Configuration

```yaml
## MySQL Configuration
# Make sure the values are the exact same
# as in your CoreProtect config.
# By default table prefix is co_

# You can also use SQLite, but I strongly recommend
# using MySQL instead.

use-mysql: true
mysql-host: "host-name"
mysql-port: 3306
mysql-database: "database-name"
mysql-username: "database-user"
# Leave '' if password not required
mysql-password: "user-password"

## Discord BOT Token
# https://discord.com/developers/applications
discord-bot-token: "your-token"
command-prefix: "co!"

## Embed configuration
# Embed page buttons use unicode symbols
# Leave them as it is if you don't know what you're doing
# Do not use high values for rows in one page

# Date format uses Java SimpleDataFormat
# https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
date-format: "yyyy-MM-dd HH:mm:ss"
# Whether to delete the embed when close button pressed
# If false, it will only hide the buttons.
delete-on-close: false
embed-page-left: '⬅️'
embed-page-right: '➡️'
embed-close: '❌'
rows-in-page: 5
# Whether to always show number of results in embed description
# without need to #count
always-show-count: true

# Role IDs that are allowed to use the commands
# You have to have developer mode enabled on discord
# To get the ID right click on the role and copy
allowed-roles:
  - 'role-one-id'
  - 'role-two-id'

## Debugging
# Only enable if you need to find out what's wrong
# Sends a lot of extra messages to the console
debug: false
```
