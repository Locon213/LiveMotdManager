package com.livemotdmanager.core;

/**
 * Provides Discord placeholder integration.
 * This implementation uses reflection so DiscordSRV is optional.
 */
public class DiscordService {
    private final boolean enabled;

    public DiscordService(MotdConfig.DiscordSettings settings) {
        this.enabled = settings.enable;
    }

    public int getOnlineUsers() {
        if (!enabled) return 0;
        try {
            Class<?> cls = Class.forName("github.scarsz.discordsrv.DiscordSRV");
            Object plugin = cls.getMethod("getPlugin").invoke(null);
            Object jda = plugin.getClass().getMethod("getJda").invoke(plugin);
            Object users = jda.getClass().getMethod("getUsers").invoke(jda);
            return (int) users.getClass().getMethod("size").invoke(users);
        } catch (Exception e) {
            return 0;
        }
    }
}
