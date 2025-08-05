# LiveMotdManager

LiveMotdManager is a multi-platform Minecraft plugin that keeps your server list message dynamic.
It supports Spigot/Paper/Purpur/Folia servers and BungeeCord/Waterfall (1.16+) or Velocity proxies.
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

Uses [open-meteo.com](https://open-meteo.com/) APIs with no key required. The plugin performs an
initial API test on startup and logs the result to the console.

## Discord

Optional integration with DiscordSRV. Placeholder `%discord_online%` shows number of connected
Discord users.

Additional documentation is available in the [wiki]([wiki/Home.md](https://github.com/Locon213/LiveMotdManager/wiki)).

## License

MIT
