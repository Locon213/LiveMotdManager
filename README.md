# LiveMotdManager

LiveMotdManager is a multi-platform Minecraft plugin that keeps your server list message dynamic.
It supports Spigot/Paper/Purpur/Folia servers from 1.16 through 1.21 and BungeeCord/Waterfall (1.16+) or Velocity proxies(3.3.0+).
The MOTD can react to time of day, player counts, TPS, real world weather and Discord activity.

## Building

Requirements: Java 17+ and Maven.

```bash
mvn package
```

Resulting jars are placed in `spigot/target`, `bungee/target` and `velocity/target` named like
`livemotdmanager-spigot-1.0.0.jar`.

## Installation

1. Place the jar for your platform into the plugins folder.
2. Start the server/proxy to generate `config.yml`.
3. Edit the configuration and run `/motd reload` to apply changes.

## Commands

Run `/motd help` in game for usage.

- `/motd reload` – reload configuration.
- `/motd set <text>` – set temporary MOTD until restart.
- `/motd info` – show active template and debug info.
- `/motd force <template|off>` – force a configured template regardless of conditions.

## Configuration

See `config.yml` for an example configuration with multiple templates and integrations.

## Weather

Uses the [OpenWeather](https://openweathermap.org/) API. An API key is required and must be
specified in `weather.api-key` in the configuration. The plugin performs an initial API test on
startup and logs the result to the console.

## Discord

Optional integration with DiscordSRV. Placeholder `%discord_online%` shows number of connected
Discord users.

## Updates

On startup the plugin contacts GitHub to see if a newer release is available. If one is found a
message with the version number and download link is printed to the console. Releases are
published at [github.com/Locon213/LiveMotdManager/releases](https://github.com/Locon213/LiveMotdManager/releases).

Additional documentation is available in the [wiki](wiki/Home.md).

## License

MIT
