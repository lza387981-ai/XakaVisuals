package dev.xaka.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Persistent settings for features already implemented in V2. */
public final class VisualSettings {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xakavisuals.properties");

    public static boolean watermark = true;
    public static boolean fpsCounter = true;

    private VisualSettings() {}

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            watermark = Boolean.parseBoolean(p.getProperty("watermark", "true"));
            fpsCounter = Boolean.parseBoolean(p.getProperty("fpsCounter", "true"));
        } catch (IOException ignored) {
            // Defaults remain active if the config cannot be read.
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("watermark", Boolean.toString(watermark));
        p.setProperty("fpsCounter", Boolean.toString(fpsCounter));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "XakaVisuals.V2");
            }
        } catch (IOException ignored) {
            // Config persistence must never crash the game.
        }
    }
}
