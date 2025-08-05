package com.livemotdmanager.core;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Loads configuration using SnakeYAML.
 */
public final class ConfigLoader {
    private ConfigLoader() {}

    public static MotdConfig load(InputStream in) {
        Yaml yaml = new Yaml(new Constructor(MotdConfig.class, new LoaderOptions()));
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            MotdConfig cfg = yaml.load(reader);
            if (cfg == null) cfg = new MotdConfig();
            return cfg;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load configuration", e);
        }
    }
}
