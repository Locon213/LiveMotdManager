package com.livemotdmanager.velocity;

import com.google.inject.Inject;
import com.livemotdmanager.core.*;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

@Plugin(id = "livemotdmanager", name = "LiveMotdManager", version = "1.0")
public class LiveMotdVelocity implements ServerInfoProvider {
    private final ProxyServer server;
    private final Path dataDir;
    private MotdConfig configData;
    private MotdManager manager;
    private WeatherService weather;
    private DiscordService discord;
    private Logger fileLogger;

    @Inject
    public LiveMotdVelocity(ProxyServer server, @DataDirectory Path dataDir) {
        this.server = server;
        this.dataDir = dataDir;
    }

    @Inject
    public void init() {
        saveDefaultConfig();
        loadConfig();
        setupLogger();
        server.getCommandManager().register(server.getCommandManager().metaBuilder("motd").build(), new MotdCommand());
        UpdateChecker.checkAsync(LiveMotdVelocity.class.getAnnotation(Plugin.class).version(), Logger.getLogger("LiveMotdManager"));
    }

    private void setupLogger() {
        try {
            File logFile = dataDir.resolve("livemotdmanager.log").toFile();
            FileHandler handler = new FileHandler(logFile.getPath(), true);
            fileLogger = Logger.getLogger("LiveMotdManager");
            fileLogger.addHandler(handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultConfig() {
        try {
            Path cfg = dataDir.resolve("config.yml");
            if (!cfg.toFile().exists()) {
                Files.createDirectories(dataDir);
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (in != null) Files.copy(in, cfg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        try (InputStream in = new FileInputStream(dataDir.resolve("config.yml").toFile())) {
            configData = ConfigLoader.load(in);
        } catch (Exception e) {
            e.printStackTrace();
            configData = new MotdConfig();
        }
        weather = new WeatherService(configData.weather);
        discord = new DiscordService(configData.discord);
        weather.start();
        manager = new MotdManager(configData, weather, discord, this);
    }

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        Component comp = manager.provide();
        event.setPing(event.getPing().asBuilder().description(comp).build());
    }

    @Override
    public int onlinePlayers() { return server.getPlayerCount(); }

    @Override
    public int maxPlayers() { return server.getConfiguration().getShowMaxPlayers(); }

    @Override
    public double tps() { return 20.0; }

    public class MotdCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            String[] args = invocation.arguments();
            if (args.length == 0) {
                invocation.source().sendMessage(Component.text("/motd help"));
                return;
            }
            switch (args[0].toLowerCase()) {
                case "help":
                    invocation.source().sendMessage(Component.text("/motd reload - reload configuration"));
                    invocation.source().sendMessage(Component.text("/motd set <text> - set temporary MOTD"));
                    invocation.source().sendMessage(Component.text("/motd info - show debug info"));
                    invocation.source().sendMessage(Component.text("/motd force <template|off> - force a template"));
                    break;
                case "reload":
                    loadConfig();
                    invocation.source().sendMessage(Component.text("Config reloaded."));
                    break;
                case "set":
                    if (args.length < 2) {
                        invocation.source().sendMessage(Component.text("Usage: /motd set <text>"));
                        break;
                    }
                    String text = String.join(" ", java.util.Arrays.copyOfRange(args,1,args.length));
                    manager.setTemporaryMotd(text);
                    invocation.source().sendMessage(Component.text("Temporary MOTD set."));
                    break;
                case "info":
                    invocation.source().sendMessage(Component.text("Current template: " + manager.getCurrentTemplate()));
                    invocation.source().sendMessage(Component.text("Weather: " + weather.getCachedWeather()));
                    invocation.source().sendMessage(Component.text("Discord online: " + discord.getOnlineUsers()));
                    break;
                case "force":
                    if (args.length < 2) {
                        invocation.source().sendMessage(Component.text("Usage: /motd force <template|off>"));
                        break;
                    }
                    if (args[1].equalsIgnoreCase("off")) {
                        manager.clearForcedTemplate();
                        invocation.source().sendMessage(Component.text("Forced template cleared."));
                    } else {
                        manager.setForcedTemplate(args[1]);
                        invocation.source().sendMessage(Component.text("Forced template set to " + args[1] + "."));
                    }
                    break;
                default:
                    invocation.source().sendMessage(Component.text("Unknown subcommand. Use /motd help"));
            }
        }
    }
}
