# LiveMotdManager Wiki

Welcome to the LiveMotdManager wiki. This documentation expands on configuration and usage.

## Features

- Dynamic MOTD templates based on time, player count and TPS
- Real world weather integration via [openweathermap.org](https://openweathermap.org/)
- DiscordSRV integration
- Works on Spigot/Paper/Folia servers and BungeeCord/Waterfall/Velocity proxies

## Configuration

See the included `config.yml` for a starting point. Each `motd` entry contains a `when` rule
and the MiniMessage formatted `text` to display.

### Weather

Set `weather.enable` to `true` and specify `city`. Use `%weather_motd%` or `%weather_city%`
placeholders in your templates. The `weather.update-interval-minutes` option controls how often
new data is fetched (default 10 minutes). An API key from OpenWeather is required and must be
placed in `weather.api-key`.

### Discord

If DiscordSRV is installed you can show online Discord users with `%discord_online%`.

## Commands

Run `/motd help` in game for the complete list.

- `/motd reload` – reload configuration
- `/motd set <text>` – set a temporary MOTD until restart
- `/motd info` – show debug information
- `/motd force <template|off>` – force a configured template

## Updates

The plugin checks for new releases on GitHub when it starts. If a newer version is available you
will see a console message with a download link. Releases are available at
[github.com/Locon213/LiveMotdManager/releases](https://github.com/Locon213/LiveMotdManager/releases).

## Building

Run `mvn package` to build. Jars are created in each module's `target` directory with names like
`livemotdmanager-spigot-1.0.0.jar`.

