package com.livemotdmanager.core;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

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
        Constructor constructor = new Constructor(MotdConfig.class, options);
        PropertyUtils utils = new PropertyUtils() {
            @Override
            public Property getProperty(Class<?> type, String name, BeanAccess bAccess) {
                // convert kebab-case (update-interval-minutes) to camelCase
                StringBuilder sb = new StringBuilder();
                boolean upper = false;
                for (char c : name.toCharArray()) {
                    if (c == '-') {
                        upper = true;
                    } else {
                        sb.append(upper ? Character.toUpperCase(c) : c);
                        upper = false;
                    }
                }
                return super.getProperty(type, sb.toString(), bAccess);
            }
        };
        utils.setSkipMissingProperties(true);
        constructor.setPropertyUtils(utils);
        Yaml yaml = new Yaml(constructor);
        yaml.setBeanAccess(BeanAccess.FIELD);
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            MotdConfig cfg = yaml.load(reader);
            if (cfg == null) cfg = new MotdConfig();
            return cfg;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load configuration", e);
        }
    }
}
