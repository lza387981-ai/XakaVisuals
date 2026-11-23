package com.xaka.visuals;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class FeatureConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xakavisuals-v3.properties");
    private FeatureConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (InputStream input = Files.newInputStream(FILE)) {
            p.load(input);
            HudDragController.load(parseInt(p.getProperty("hud.target.x"), -1),
                    parseInt(p.getProperty("hud.target.y"), -1),
                    parseInt(p.getProperty("hud.armor.x"), -1),
                    parseInt(p.getProperty("hud.armor.y"), -1));
            for (Feature feature : FeatureRegistry.all()) {
                String key = key(feature);
                feature.setEnabled(Boolean.parseBoolean(p.getProperty(key + ".enabled", Boolean.toString(feature.enabled()))));
                feature.setBindKey(parseInt(p.getProperty(key + ".bind"), -1));
            }
            FeatureSettings.load(p);
        } catch (IOException exception) {
            System.err.println("[XakaVisuals] Could not load config: " + exception.getMessage());
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Feature feature : FeatureRegistry.all()) {
            String key = key(feature);
            p.setProperty(key + ".enabled", Boolean.toString(feature.enabled()));
            p.setProperty(key + ".bind", Integer.toString(feature.bindKey()));
        }
        p.setProperty("hud.target.x", Integer.toString(HudDragController.targetX()));
        p.setProperty("hud.target.y", Integer.toString(HudDragController.targetY()));
        p.setProperty("hud.armor.x", Integer.toString(HudDragController.armorX()));
        p.setProperty("hud.armor.y", Integer.toString(HudDragController.armorY()));
        FeatureSettings.save(p);
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream output = Files.newOutputStream(FILE)) { p.store(output, "XakaVisuals V3 settings"); }
        } catch (IOException exception) {
            System.err.println("[XakaVisuals] Could not save config: " + exception.getMessage());
        }
    }

    private static String key(Feature f) {
        return "feature." + f.category().name().toLowerCase() + "." + f.name().toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }
    private static int parseInt(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }
}
