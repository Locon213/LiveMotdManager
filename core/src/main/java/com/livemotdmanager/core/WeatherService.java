package com.livemotdmanager.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Fetches real world weather information using the OpenWeather API.
 */
public class WeatherService {
    private final boolean enabled;
    private final String city;
    private final String apiKey;
    private final int updateIntervalMinutes;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile String cached = "";
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public WeatherService(MotdConfig.WeatherSettings settings) {
        this.enabled = settings.enable;
        this.city = settings.city;
        this.apiKey = settings.apiKey;
        this.updateIntervalMinutes = settings.updateIntervalMinutes;
    }

    public void start() {
        if (!enabled) return;
        // Initial fetch for startup verification
        update();
        System.out.println("[LiveMotdManager] Weather API test: " + (cached.isEmpty() ? "unavailable" : cached));
        scheduler.scheduleAtFixedRate(this::update, updateIntervalMinutes, updateIntervalMinutes, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    public String getCachedWeather() {
        return cached;
    }

    private void update() {
        if (!enabled || city == null || city.isEmpty() || apiKey == null || apiKey.isEmpty()) return;
        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric", encodedCity, apiKey);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(10)).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
            if (!json.has("weather") || !json.has("main")) return;
            JsonObject main = json.getAsJsonObject("main");
            double temp = main.get("temp").getAsDouble();
            JsonObject first = json.getAsJsonArray("weather").get(0).getAsJsonObject();
            String desc = first.get("description").getAsString();
            if (!desc.isEmpty()) {
                desc = desc.substring(0,1).toUpperCase(Locale.ROOT) + desc.substring(1);
            }
            cached = String.format(Locale.US, "%s %.1f°C", desc, temp);
        } catch (Exception e) {
            // ignore but keep cached
        }
    }
}
