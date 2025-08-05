package com.livemotdmanager.core;

/**
 * Holds dynamic data used when building MOTD.
 */
public class MotdContext {
    public final int online;
    public final int max;
    public final double tps;
    public final String weather;
    public final int discordOnline;

    public MotdContext(int online, int max, double tps, String weather, int discordOnline) {
        this.online = online;
        this.max = max;
        this.tps = tps;
        this.weather = weather;
        this.discordOnline = discordOnline;
    }
}
