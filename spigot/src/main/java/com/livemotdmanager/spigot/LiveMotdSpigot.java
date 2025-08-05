package com.livemotdmanager.spigot;

import com.livemotdmanager.core.*;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Spigot/Paper/Folia plugin entry.
 */
public class LiveMotdSpigot extends JavaPlugin implements Listener, TabExecutor, ServerInfoProvider {
    private MotdConfig configData;
    private MotdManager manager;
    private WeatherService weather;
    private DiscordService discord;
    private BukkitAudiences adventure;
    private Logger fileLogger;

    @Override
    public void onEnable() {
        saveDefaultConfigFile();
        loadConfig();
        adventure = BukkitAudiences.create(this);
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("motd").setExecutor(this);
        setupLogger();
    }

    @Override
    public void onDisable() {
        if (weather != null) weather.stop();
        if (adventure != null) adventure.close();
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

    private void saveDefaultConfigFile() {
        try {
            File cfg = new File(getDataFolder(), "config.yml");
            if (!cfg.exists()) {
                getDataFolder().mkdirs();
                try (InputStream in = getResource("config.yml")) {
                    if (in != null) Files.copy(in, cfg.toPath());
                }
            }
        } catch (Exception e) {
            getLogger().severe("Cannot save default config: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        Component comp = manager.provide();
        String legacy = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(comp);
        event.setMotd(legacy);
    }

    // ServerInfoProvider implementation
    @Override
    public int onlinePlayers() { return Bukkit.getOnlinePlayers().size(); }

    @Override
    public int maxPlayers() { return Bukkit.getMaxPlayers(); }

    @Override
    public double tps() {
        try {
            var method = Bukkit.getServer().getClass().getMethod("getTPS");
            double[] tps = (double[]) method.invoke(Bukkit.getServer());
            return tps.length > 0 ? tps[0] : 20.0;
        } catch (Exception e) {
            return 20.0;
        }
    }

    // Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/motd help");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help":
                sender.sendMessage("/motd reload - reload configuration");
                sender.sendMessage("/motd set <text> - set temporary MOTD");
                sender.sendMessage("/motd info - show debug info");
                sender.sendMessage("/motd force <template|off> - force a template");
                break;
            case "reload":
                loadConfig();
                sender.sendMessage("MOTD config reloaded.");
                break;
            case "set":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /motd set <text>");
                    break;
                }
                String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                manager.setTemporaryMotd(text);
                sender.sendMessage("Temporary MOTD set.");
                break;
            case "info":
                sender.sendMessage("Current template: " + manager.getCurrentTemplate());
                sender.sendMessage("Weather: " + weather.getCachedWeather());
                sender.sendMessage("Discord online: " + discord.getOnlineUsers());
                break;
            case "force":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /motd force <template|off>");
                    break;
                }
                if (args[1].equalsIgnoreCase("off")) {
                    manager.clearForcedTemplate();
                    sender.sendMessage("Forced template cleared.");
                } else {
                    manager.setForcedTemplate(args[1]);
                    sender.sendMessage("Forced template set to " + args[1] + ".");
                }
                break;
            default:
                sender.sendMessage("Unknown subcommand. Use /motd help");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("help", "reload", "set", "info", "force");
        return List.of();
    }
}
