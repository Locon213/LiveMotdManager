package com.livemotdmanager.core;

/**
 * Provides runtime information about server or proxy.
 */
public interface ServerInfoProvider {
    int onlinePlayers();
    int maxPlayers();
    double tps();
}
