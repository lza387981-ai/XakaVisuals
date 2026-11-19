package dev.xaka.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Persistent settings shared by the menu and HUD. */
public final class VisualSettings {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xakavisuals.properties");

    public static boolean watermark = true;
    public static boolean fpsCounter = true;
    public static boolean playerHalo;
    public static boolean targetMarker;
    public static boolean cosmeticPets = true;
    public static boolean handPosition;
    public static boolean safeFpsBoost;

    private VisualSettings() {}

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            watermark = read(p, "watermark", true);
            fpsCounter = read(p, "fpsCounter", true);
            playerHalo = read(p, "playerHalo", false);
            targetMarker = read(p, "targetMarker", false);
            cosmeticPets = read(p, "cosmeticPets", true);
            handPosition = read(p, "handPosition", false);
            safeFpsBoost = read(p, "safeFpsBoost", false);
        } catch (IOException ignored) {
            // Keep safe defaults if the config cannot be read.
        }
    }

    private static boolean read(Properties p, String key, boolean fallback) {
        return Boolean.parseBoolean(p.getProperty(key, Boolean.toString(fallback)));
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("watermark", Boolean.toString(watermark));
        p.setProperty("fpsCounter", Boolean.toString(fpsCounter));
        p.setProperty("playerHalo", Boolean.toString(playerHalo));
        p.setProperty("targetMarker", Boolean.toString(targetMarker));
        p.setProperty("cosmeticPets", Boolean.toString(cosmeticPets));
        p.setProperty("handPosition", Boolean.toString(handPosition));
        p.setProperty("safeFpsBoost", Boolean.toString(safeFpsBoost));
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
