package com.livemotdmanager.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

/**
 * Simple GitHub release update checker.
 */
public final class UpdateChecker {
    private static final String API_URL = "https://api.github.com/repos/Locon213/LiveMotdManager/releases/latest";
    private static final String DOWNLOAD_URL = "https://github.com/Locon213/LiveMotdManager/releases";

    private UpdateChecker() {}

    public static void checkAsync(String currentVersion, Logger logger) {
        new Thread(() -> check(currentVersion, logger)).start();
    }

    private static void check(String currentVersion, Logger logger) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                    .header("Accept", "application/vnd.github+json")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String latest = parseLatest(response.body());
            if (latest != null && isNewer(latest, currentVersion)) {
                logger.info("A new version of LiveMotdManager is available: " + latest + ". Download at " + DOWNLOAD_URL);
            }
        } catch (Exception e) {
            logger.fine("Update check failed: " + e.getMessage());
        }
    }

    private static String parseLatest(String json) {
        int idx = json.indexOf("\"tag_name\"");
        if (idx == -1) return null;
        int start = json.indexOf('"', idx + 10) + 1;
        int end = json.indexOf('"', start);
        if (start == 0 || end == -1) return null;
        String tag = json.substring(start, end);
        return tag.startsWith("v") ? tag.substring(1) : tag;
    }

    private static boolean isNewer(String latest, String current) {
        try {
            String[] l = latest.split("\\.");
            String[] c = current.split("\\.");
            int max = Math.max(l.length, c.length);
            for (int i = 0; i < max; i++) {
                int li = i < l.length ? Integer.parseInt(l[i]) : 0;
                int ci = i < c.length ? Integer.parseInt(c[i]) : 0;
                if (li > ci) return true;
                if (li < ci) return false;
            }
        } catch (NumberFormatException ignored) { }
        return false;
    }
}
