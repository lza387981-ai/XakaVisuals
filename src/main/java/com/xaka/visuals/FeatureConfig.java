package com.xaka.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class FeatureConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("xakavisuals-v3.properties");

    private FeatureConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(FILE)) {
            properties.load(input);
            for (Feature feature : FeatureRegistry.all()) {
                String key = key(feature);
                feature.setEnabled(Boolean.parseBoolean(properties.getProperty(key + ".enabled",
                        Boolean.toString(feature.enabled()))));
                feature.setBindKey(parseInt(properties.getProperty(key + ".bind"), -1));
            }
        } catch (IOException exception) {
            System.err.println("[XakaVisuals] Could not load config: " + exception.getMessage());
        }
    }

    public static void save() {
        Properties properties = new Properties();
        for (Feature feature : FeatureRegistry.all()) {
            String key = key(feature);
            properties.setProperty(key + ".enabled", Boolean.toString(feature.enabled()));
            properties.setProperty(key + ".bind", Integer.toString(feature.bindKey()));
        }
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream output = Files.newOutputStream(FILE)) {
                properties.store(output, "XakaVisuals V3 settings");
            }
        } catch (IOException exception) {
            System.err.println("[XakaVisuals] Could not save config: " + exception.getMessage());
        }
    }

    private static String key(Feature feature) {
        return "feature." + feature.category().name().toLowerCase() + "."
                + feature.name().toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    private static int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
