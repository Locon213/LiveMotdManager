package com.livemotdmanager.core;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Loads configuration using SnakeYAML.
 */
public final class ConfigLoader {
    private ConfigLoader() {}

    public static MotdConfig load(InputStream in) {
        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(options);
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            MotdConfig cfg = yaml.loadAs(reader, MotdConfig.class);
            if (cfg == null) cfg = new MotdConfig();
            return cfg;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load configuration", e);
        }
    }
}
