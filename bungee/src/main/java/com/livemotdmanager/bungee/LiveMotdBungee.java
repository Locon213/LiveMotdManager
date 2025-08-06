package com.livemotdmanager.bungee;

import com.livemotdmanager.core.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * BungeeCord/Waterfall plugin implementation.
 */
public class LiveMotdBungee extends Plugin implements Listener, ServerInfoProvider {
    private MotdConfig configData;
    private MotdManager manager;
    private WeatherService weather;
    private DiscordService discord;
    private Logger fileLogger;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new MotdCommand());
        setupLogger();
    }

    @Override
    public void onDisable() {
        if (weather != null) weather.stop();
        if (fileLogger != null) for (var h : fileLogger.getHandlers()) h.close();
    }

    private void setupLogger() {
        try {
            File logFile = new File(getDataFolder(), "livemotdmanager.log");
            FileHandler handler = new FileHandler(logFile.getPath(), true);
            fileLogger = Logger.getLogger("LiveMotdManager");
            fileLogger.addHandler(handler);
        } catch (Exception e) {
            getLogger().warning("Could not setup log file: " + e.getMessage());
        }
    }

    private void saveDefaultConfig() {
        try {
            File cfg = new File(getDataFolder(), "config.yml");
            if (!cfg.exists()) {
                getDataFolder().mkdirs();
                try (InputStream in = getResourceAsStream("config.yml")) {
                    if (in != null) Files.copy(in, cfg.toPath());
                }
            }
        } catch (Exception e) {
            getLogger().severe("Cannot save default config: " + e.getMessage());
        }
    }

    private void loadConfig() {
        try (InputStream in = new FileInputStream(new File(getDataFolder(), "config.yml"))) {
            configData = ConfigLoader.load(in);
        } catch (Exception e) {
            getLogger().severe("Failed to load config: " + e.getMessage());
            configData = new MotdConfig();
        }
        weather = new WeatherService(configData.weather);
        discord = new DiscordService(configData.discord);
        weather.start();
        manager = new MotdManager(configData, weather, discord, this);
    }

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        Component comp = manager.provide();
        String legacy = LegacyComponentSerializer.legacySection().serialize(comp);
        event.getResponse().setDescriptionComponent(TextComponent.fromLegacyText(legacy));
    }

    // ServerInfoProvider
    @Override
    public int onlinePlayers() { return ProxyServer.getInstance().getOnlineCount(); }

    @Override
    public int maxPlayers() { return ProxyServer.getInstance().getConfig().getPlayerLimit(); }

    @Override
    public double tps() { return 20.0; }

    // Command implementation
    public class MotdCommand extends Command {
        public MotdCommand() { super("motd", "livemotdmanager.command"); }
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length == 0) {
                sender.sendMessage(new TextComponent("/motd help"));
                return;
            }
            switch (args[0].toLowerCase()) {
                case "help":
                    sender.sendMessage(new TextComponent("/motd reload - reload configuration"));
                    sender.sendMessage(new TextComponent("/motd set <text> - set temporary MOTD"));
                    sender.sendMessage(new TextComponent("/motd info - show debug info"));
                    sender.sendMessage(new TextComponent("/motd force <template|off> - force a template"));
                    break;
                case "reload":
                    loadConfig();
                    sender.sendMessage(new TextComponent("Config reloaded."));
                    break;
                case "set":
                    if (args.length < 2) {
                        sender.sendMessage(new TextComponent("Usage: /motd set <text>"));
                        break;
                    }
                    String text = String.join(" ", Arrays.copyOfRange(args,1,args.length));
                    manager.setTemporaryMotd(text);
                    sender.sendMessage(new TextComponent("Temporary MOTD set."));
                    break;
                case "info":
                    sender.sendMessage(new TextComponent("Current template: " + manager.getCurrentTemplate()));
                    sender.sendMessage(new TextComponent("Weather: " + weather.getCachedWeather()));
                    sender.sendMessage(new TextComponent("Discord online: " + discord.getOnlineUsers()));
                    break;
                case "force":
                    if (args.length < 2) {
                        sender.sendMessage(new TextComponent("Usage: /motd force <template|off>"));
                        break;
                    }
                    if (args[1].equalsIgnoreCase("off")) {
                        manager.clearForcedTemplate();
                        sender.sendMessage(new TextComponent("Forced template cleared."));
                    } else {
                        manager.setForcedTemplate(args[1]);
                        sender.sendMessage(new TextComponent("Forced template set to " + args[1] + "."));
                    }
                    break;
                default:
                    sender.sendMessage(new TextComponent("Unknown subcommand. Use /motd help"));
            }
        }
    }
}
