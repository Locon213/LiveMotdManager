package com.livemotdmanager.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents configuration for LiveMotdManager.
 */
public class MotdConfig {
    public List<MotdRule> motd = new ArrayList<>();
    public WeatherSettings weather = new WeatherSettings();
    public DiscordSettings discord = new DiscordSettings();
    public CacheSettings cache = new CacheSettings();

    public static class MotdRule {
        public String when;
        public String text;
    }

    public static class WeatherSettings {
        public boolean enable = true;
        public String city = "";
        public String apiKey = "";
        public int updateIntervalMinutes = 10;
    }

    public static class DiscordSettings {
        public boolean enable = true;
        public String placeholder = "%discord_online%";
    }

    public static class CacheSettings {
        public int updateIntervalSeconds = 5;
    }
}
