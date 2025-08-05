# LiveMotdManager

LiveMotdManager is a multi-platform Minecraft plugin providing a dynamic server list MOTD. It supports Spigot/Paper/Purpur/Folia servers and BungeeCord/Waterfall (1.16+) / Velocity proxies. The plugin changes MOTD based on time, player count, TPS and more. Weather and Discord integrations are included.

## Building

Requirements: Java 17+ and Maven.

```bash
mvn package
```

Resulting jars will be in `spigot/target`, `bungee/target` and `velocity/target`.

## Installation

1. Place the jar for your platform into the plugins folder.
2. Start the server/proxy to generate the config file.
3. Edit `config.yml` as needed and run `/motd reload` to apply changes.

## Commands

- `/motd reload` – reload configuration.
- `/motd set <text>` – set temporary MOTD until restart.
- `/motd info` – show active template and debug info.

## Configuration

See `config.yml` for an example configuration with multiple templates and integrations.

## Weather

Uses [open-meteo.com](https://open-meteo.com/) APIs with no key required.

## Discord

Optional integration with DiscordSRV. Placeholder `%discord_online%` shows number of connected Discord users.

## License

MIT
