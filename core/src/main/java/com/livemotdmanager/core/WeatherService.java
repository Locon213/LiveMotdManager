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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Fetches real world weather information using open-meteo.com.
 */
public class WeatherService {
    private final boolean enabled;
    private final String city;
    private final int updateIntervalMinutes;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile String cached = "";
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public WeatherService(MotdConfig.WeatherSettings settings) {
        this.enabled = settings.enable;
        this.city = settings.city;
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
        if (!enabled || city == null || city.isEmpty()) return;
        try {
            // Geocode city (encode to handle spaces and special characters)
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String geocodeUrl = String.format("https://geocoding-api.open-meteo.com/v1/search?count=1&name=%s", encodedCity);
            HttpRequest geoReq = HttpRequest.newBuilder().uri(URI.create(geocodeUrl)).timeout(Duration.ofSeconds(10)).build();
            HttpResponse<String> geoResp = http.send(geoReq, HttpResponse.BodyHandlers.ofString());
            JsonObject geoJson = JsonParser.parseString(geoResp.body()).getAsJsonObject();
            if (!geoJson.has("results")) return;
            JsonObject first = geoJson.getAsJsonArray("results").get(0).getAsJsonObject();
            double lat = first.get("latitude").getAsDouble();
            double lon = first.get("longitude").getAsDouble();

            String weatherUrl = String.format("https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current_weather=true", lat, lon);
            HttpRequest weatherReq = HttpRequest.newBuilder().uri(URI.create(weatherUrl)).timeout(Duration.ofSeconds(10)).build();
            HttpResponse<String> weatherResp = http.send(weatherReq, HttpResponse.BodyHandlers.ofString());
            JsonObject weatherJson = JsonParser.parseString(weatherResp.body()).getAsJsonObject();
            if (!weatherJson.has("current_weather")) return;
            JsonObject cur = weatherJson.getAsJsonObject("current_weather");
            double temp = cur.get("temperature").getAsDouble();
            int code = cur.get("weathercode").getAsInt();
            String desc = WeatherCode.describe(code);
            cached = String.format("%s %.1f°C", desc, temp);
        } catch (Exception e) {
            // ignore but keep cached
        }
    }

    /**
     * Maps open-meteo weather codes to short descriptions.
     */
    public static class WeatherCode {
        public static String describe(int code) {
            switch (code) {
                case 0: return "Clear";
                case 1: case 2: case 3: return "Cloudy";
                case 45: case 48: return "Fog";
                case 51: case 53: case 55: return "Drizzle";
                case 61: case 63: case 65: return "Rain";
                case 71: case 73: case 75: return "Snow";
                case 95: return "Thunder";
                default: return "Weather";
            }
        }
    }
}
