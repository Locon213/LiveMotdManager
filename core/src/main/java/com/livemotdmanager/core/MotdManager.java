package com.livemotdmanager.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds MOTD based on configuration and server state.
 */
public class MotdManager {
    private final MotdConfig config;
    private final WeatherService weather;
    private final DiscordService discord;
    private final ServerInfoProvider infoProvider;
    private final List<PlaceholderProvider> placeholders = new ArrayList<>();

    private String cachedString = "";
    private Component cachedComponent = Component.text("");
    private long lastUpdate = 0L;
    private String currentTemplate = "";
    private final long cacheIntervalMs;
    private String temporaryMotd = null;

    public MotdManager(MotdConfig config, WeatherService weather, DiscordService discord, ServerInfoProvider infoProvider) {
        this.config = config;
        this.weather = weather;
        this.discord = discord;
        this.infoProvider = infoProvider;
        this.cacheIntervalMs = config.cache.updateIntervalSeconds * 1000L;
        placeholders.add(this::defaultPlaceholders);
    }

    public void setTemporaryMotd(String temp) {
        this.temporaryMotd = temp;
    }

    public String getCurrentTemplate() {
        return currentTemplate;
    }

    public Component provide() {
        MotdContext ctx = new MotdContext(infoProvider.onlinePlayers(), infoProvider.maxPlayers(), infoProvider.tps(), weather.getCachedWeather(), discord.getOnlineUsers());
        long now = System.currentTimeMillis();
        if (temporaryMotd != null) {
            return MiniMessage.miniMessage().deserialize(applyPlaceholders(temporaryMotd, ctx));
        }
        if (now - lastUpdate > cacheIntervalMs) {
            rebuild(ctx);
            lastUpdate = now;
        }
        return cachedComponent;
    }

    private void rebuild(MotdContext ctx) {
        for (MotdConfig.MotdRule rule : config.motd) {
            if (matches(rule.when, ctx)) {
                currentTemplate = rule.when;
                String txt = applyPlaceholders(rule.text, ctx);
                cachedString = txt;
                cachedComponent = MiniMessage.miniMessage().deserialize(txt);
                return;
            }
        }
        // fallback to default if defined
        cachedString = "";
        cachedComponent = Component.text("");
    }

    public String applyPlaceholders(String text, MotdContext ctx) {
        String out = text;
        for (PlaceholderProvider prov : placeholders) {
            out = prov.apply(out, ctx);
        }
        return out;
    }

    private String defaultPlaceholders(String text, MotdContext ctx) {
        String out = text;
        out = out.replace("%online%", Integer.toString(ctx.online));
        out = out.replace("%maxplayers%", Integer.toString(ctx.max));
        out = out.replace("%tps%", String.format(Locale.US, "%.2f", ctx.tps));
        if (ctx.weather != null) {
            out = out.replace("%weather_" + config.weather.city.toLowerCase(Locale.ROOT) + "%", ctx.weather);
            out = out.replace("%weather_city%", ctx.weather);
        }
        out = out.replace("%discord_online%", Integer.toString(ctx.discordOnline));
        return out;
    }

    public void addPlaceholder(PlaceholderProvider provider) {
        placeholders.add(provider);
    }

    private boolean matches(String when, MotdContext ctx) {
        if (when == null) return false;
        when = when.trim().toLowerCase(Locale.ROOT);
        switch (when) {
            case "default":
                return true;
            case "morning":
                return betweenHours(5, 11);
            case "day":
                return betweenHours(11, 17);
            case "evening":
                return betweenHours(17, 21);
            case "night":
                return ctxTime().isAfter(LocalTime.of(21,0)) || ctxTime().isBefore(LocalTime.of(5,0));
        }
        if (when.startsWith("players")) {
            return compareNumber(ctx.online, when.substring("players".length()));
        }
        if (when.startsWith("tps")) {
            return compareDouble(ctx.tps, when.substring("tps".length()));
        }
        if ("event".equals(when)) {
            return false; // no event flag yet
        }
        return false;
    }

    private boolean betweenHours(int start, int end) {
        LocalTime now = ctxTime();
        return !now.isBefore(LocalTime.of(start,0)) && now.isBefore(LocalTime.of(end,0));
    }

    private LocalTime ctxTime() {
        return LocalTime.now();
    }

    private boolean compareNumber(int left, String expression) {
        expression = expression.trim();
        if (expression.startsWith(">=")) {
            return left >= Integer.parseInt(expression.substring(2).trim());
        } else if (expression.startsWith("<=")) {
            return left <= Integer.parseInt(expression.substring(2).trim());
        } else if (expression.startsWith(">")) {
            return left > Integer.parseInt(expression.substring(1).trim());
        } else if (expression.startsWith("<")) {
            return left < Integer.parseInt(expression.substring(1).trim());
        }
        return false;
    }

    private boolean compareDouble(double left, String expression) {
        expression = expression.trim();
        if (expression.startsWith(">=")) {
            return left >= Double.parseDouble(expression.substring(2).trim());
        } else if (expression.startsWith("<=")) {
            return left <= Double.parseDouble(expression.substring(2).trim());
        } else if (expression.startsWith(">")) {
            return left > Double.parseDouble(expression.substring(1).trim());
        } else if (expression.startsWith("<")) {
            return left < Double.parseDouble(expression.substring(1).trim());
        }
        return false;
    }
}
