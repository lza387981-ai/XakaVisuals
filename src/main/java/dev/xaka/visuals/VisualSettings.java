package dev.xaka.visuals;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Persistent configuration for the implemented XakaVisuals.V2 features. */
public final class VisualSettings {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xakavisuals.properties");
    public static boolean fullbright = false;

    private VisualSettings() {}

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            XakaVisualsClient.animations = bool(p, "animations", XakaVisualsClient.animations);
            XakaVisualsClient.compactHud = bool(p, "compactHud", XakaVisualsClient.compactHud);
            XakaVisualsClient.watermark = bool(p, "watermark", XakaVisualsClient.watermark);
            XakaVisualsClient.fps = bool(p, "fps", XakaVisualsClient.fps);
            XakaVisualsClient.coordinates = bool(p, "coordinates", XakaVisualsClient.coordinates);
            XakaVisualsClient.enemyHpEnabled = bool(p, "enemyHpEnabled", XakaVisualsClient.enemyHpEnabled);
            XakaVisualsClient.selfHealthHud = bool(p, "selfHealthHud", XakaVisualsClient.selfHealthHud);
            XakaVisualsClient.enemyHpX = integer(p, "enemyHpX", XakaVisualsClient.enemyHpX);
            XakaVisualsClient.enemyHpY = integer(p, "enemyHpY", XakaVisualsClient.enemyHpY);
            XakaVisualsClient.markerEnabled = bool(p, "markerEnabled", XakaVisualsClient.markerEnabled);
            XakaVisualsClient.markerSquare = bool(p, "markerSquare", XakaVisualsClient.markerSquare);
            XakaVisualsClient.markerSize = integer(p, "markerSize", XakaVisualsClient.markerSize);
            XakaVisualsClient.targetFrameColorIndex = integer(p, "targetFrameColorIndex", XakaVisualsClient.targetFrameColorIndex);
            XakaVisualsClient.haloEnabled = bool(p, "haloEnabled", XakaVisualsClient.haloEnabled);
            XakaVisualsClient.haloSize = integer(p, "haloSize", XakaVisualsClient.haloSize);
            XakaVisualsClient.bodyMarkerEnabled = bool(p, "bodyMarkerEnabled", XakaVisualsClient.bodyMarkerEnabled);
            XakaVisualsClient.bodyMarkerSquare = bool(p, "bodyMarkerSquare", XakaVisualsClient.bodyMarkerSquare);
            XakaVisualsClient.bodyMarkerSize = integer(p, "bodyMarkerSize", XakaVisualsClient.bodyMarkerSize);
            XakaVisualsClient.bodyMarkerOffsetX = decimal(p, "bodyMarkerOffsetX", XakaVisualsClient.bodyMarkerOffsetX);
            XakaVisualsClient.bodyMarkerOffsetY = decimal(p, "bodyMarkerOffsetY", XakaVisualsClient.bodyMarkerOffsetY);
            XakaVisualsClient.bodyMarkerOffsetZ = decimal(p, "bodyMarkerOffsetZ", XakaVisualsClient.bodyMarkerOffsetZ);
            XakaVisualsClient.accentIndex = integer(p, "accentIndex", XakaVisualsClient.accentIndex);
            XakaVisualsClient.petEnabled = bool(p, "petEnabled", XakaVisualsClient.petEnabled);
            XakaVisualsClient.petType = integer(p, "petType", XakaVisualsClient.petType);
            XakaVisualsClient.petSize = integer(p, "petSize", XakaVisualsClient.petSize);
            XakaVisualsClient.wingsEnabled = bool(p, "wingsEnabled", XakaVisualsClient.wingsEnabled);
            XakaVisualsClient.wingStyle = integer(p, "wingStyle", XakaVisualsClient.wingStyle);
            XakaVisualsClient.wingSpeed = decimal(p, "wingSpeed", XakaVisualsClient.wingSpeed);
            XakaVisualsClient.hatEnabled = bool(p, "hatEnabled", XakaVisualsClient.hatEnabled);
            XakaVisualsClient.hatStyle = integer(p, "hatStyle", XakaVisualsClient.hatStyle);
            XakaVisualsClient.handOffsetX = decimal(p, "handOffsetX", XakaVisualsClient.handOffsetX);
            XakaVisualsClient.handOffsetY = decimal(p, "handOffsetY", XakaVisualsClient.handOffsetY);
            XakaVisualsClient.handOffsetZ = decimal(p, "handOffsetZ", XakaVisualsClient.handOffsetZ);
            XakaVisualsClient.handRotationX = decimal(p, "handRotationX", XakaVisualsClient.handRotationX);
            XakaVisualsClient.handRotationY = decimal(p, "handRotationY", XakaVisualsClient.handRotationY);
            XakaVisualsClient.handRotationZ = decimal(p, "handRotationZ", XakaVisualsClient.handRotationZ);
            XakaVisualsClient.fpsBoost = bool(p, "fpsBoost", XakaVisualsClient.fpsBoost);
            XakaVisualsClient.droppedItemTilt = bool(p, "droppedItemTilt", XakaVisualsClient.droppedItemTilt);
            XakaVisualsClient.customCrosshair = bool(p, "customCrosshair", XakaVisualsClient.customCrosshair);
            XakaVisualsClient.crosshairSize = integer(p, "crosshairSize", XakaVisualsClient.crosshairSize);
            XakaVisualsClient.crosshairGap = integer(p, "crosshairGap", XakaVisualsClient.crosshairGap);
            XakaVisualsClient.crosshairThickness = integer(p, "crosshairThickness", XakaVisualsClient.crosshairThickness);
            XakaVisualsClient.crosshairDot = bool(p, "crosshairDot", XakaVisualsClient.crosshairDot);
            XakaVisualsClient.crosshairDotSize = integer(p, "crosshairDotSize", XakaVisualsClient.crosshairDotSize);
            XakaVisualsClient.crosshairOutline = bool(p, "crosshairOutline", XakaVisualsClient.crosshairOutline);
            XakaVisualsClient.crosshairOutlineWidth = integer(p, "crosshairOutlineWidth", XakaVisualsClient.crosshairOutlineWidth);
            XakaVisualsClient.autoSprint = bool(p, "autoSprint", XakaVisualsClient.autoSprint);
            XakaVisualsClient.lowArmorNotify = bool(p, "lowArmorNotify", XakaVisualsClient.lowArmorNotify);
            XakaVisualsClient.deathCoords = bool(p, "deathCoords", XakaVisualsClient.deathCoords);
            XakaVisualsClient.hitParticles = bool(p, "hitParticles", XakaVisualsClient.hitParticles);
            XakaVisualsClient.jumpCircle = bool(p, "jumpCircle", XakaVisualsClient.jumpCircle);
            XakaVisualsClient.jumpEffect = bool(p, "jumpEffect", XakaVisualsClient.jumpEffect);
            XakaVisualsClient.jumpCubes = bool(p, "jumpCubes", XakaVisualsClient.jumpCubes);
            XakaVisualsClient.mouseParticles = bool(p, "mouseParticles", XakaVisualsClient.mouseParticles);
            XakaVisualsClient.mouseParticleSize = integer(p, "mouseParticleSize", XakaVisualsClient.mouseParticleSize);
            XakaVisualsClient.particleDensity = Math.max(1, Math.min(5, integer(p, "particleDensity", XakaVisualsClient.particleDensity)));
            XakaVisualsClient.jumpCubeCount = integer(p, "jumpCubeCount", XakaVisualsClient.jumpCubeCount);
            XakaVisualsClient.jumpCubeSize = integer(p, "jumpCubeSize", XakaVisualsClient.jumpCubeSize);
            XakaVisualsClient.jumpEffectRadius = integer(p, "jumpEffectRadius", XakaVisualsClient.jumpEffectRadius);
            XakaVisualsClient.jumpEffectDuration = integer(p, "jumpEffectDuration", XakaVisualsClient.jumpEffectDuration);
            XakaVisualsClient.playerTrails = bool(p, "playerTrails", XakaVisualsClient.playerTrails);
            XakaVisualsClient.noFluidOverlay = bool(p, "noFluidOverlay", XakaVisualsClient.noFluidOverlay);
            XakaVisualsClient.noFireOverlay = bool(p, "noFireOverlay", XakaVisualsClient.noFireOverlay);
            XakaVisualsClient.noHurtCam = bool(p, "noHurtCam", XakaVisualsClient.noHurtCam);
            XakaVisualsClient.noWeather = bool(p, "noWeather", XakaVisualsClient.noWeather);
            XakaVisualsClient.ambienceTimeEnabled = bool(p, "ambienceTimeEnabled", XakaVisualsClient.ambienceTimeEnabled);
            XakaVisualsClient.ambienceTime = Math.floorMod(integer(p, "ambienceTime", XakaVisualsClient.ambienceTime), 24000);
            XakaVisualsClient.hideBossBar = bool(p, "hideBossBar", XakaVisualsClient.hideBossBar);
            XakaVisualsClient.noInWallOverlay = bool(p, "noInWallOverlay", XakaVisualsClient.noInWallOverlay);
            XakaVisualsClient.cooldownHud = bool(p, "cooldownHud", XakaVisualsClient.cooldownHud);
            XakaVisualsClient.waypointEnabled = bool(p, "waypointEnabled", XakaVisualsClient.waypointEnabled);
            XakaVisualsClient.nameProtect = bool(p, "nameProtect", XakaVisualsClient.nameProtect);
            XakaVisualsClient.shulkerPreview = bool(p, "shulkerPreview", XakaVisualsClient.shulkerPreview);
            XakaVisualsClient.killEffect = bool(p, "killEffect", XakaVisualsClient.killEffect);
            XakaVisualsClient.waypointX = integer(p, "waypointX", XakaVisualsClient.waypointX);
            XakaVisualsClient.waypointY = integer(p, "waypointY", XakaVisualsClient.waypointY);
            XakaVisualsClient.waypointZ = integer(p, "waypointZ", XakaVisualsClient.waypointZ);
            XakaVisualsClient.activeDance = integer(p, "activeDance", XakaVisualsClient.activeDance);
            XakaVisualsClient.danceSpeed = decimal(p, "danceSpeed", XakaVisualsClient.danceSpeed);
            fullbright = bool(p, "fullbright", fullbright);
            XakaVisualsClient.dancePlaying = false;
            XakaVisualsMusic.setVolume(decimal(p, "musicVolume", XakaVisualsMusic.getVolume()));
        } catch (IOException ignored) {
            // Keep safe defaults if the config is unavailable.
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("animations", Boolean.toString(XakaVisualsClient.animations));
        p.setProperty("compactHud", Boolean.toString(XakaVisualsClient.compactHud));
        p.setProperty("watermark", Boolean.toString(XakaVisualsClient.watermark));
        p.setProperty("fps", Boolean.toString(XakaVisualsClient.fps));
        p.setProperty("coordinates", Boolean.toString(XakaVisualsClient.coordinates));
        p.setProperty("enemyHpEnabled", Boolean.toString(XakaVisualsClient.enemyHpEnabled));
        p.setProperty("selfHealthHud", Boolean.toString(XakaVisualsClient.selfHealthHud));
        p.setProperty("enemyHpX", Integer.toString(XakaVisualsClient.enemyHpX));
        p.setProperty("enemyHpY", Integer.toString(XakaVisualsClient.enemyHpY));
        p.setProperty("markerEnabled", Boolean.toString(XakaVisualsClient.markerEnabled));
        p.setProperty("markerSquare", Boolean.toString(XakaVisualsClient.markerSquare));
        p.setProperty("markerSize", Integer.toString(XakaVisualsClient.markerSize));
        p.setProperty("targetFrameColorIndex", Integer.toString(XakaVisualsClient.targetFrameColorIndex));
        p.setProperty("haloEnabled", Boolean.toString(XakaVisualsClient.haloEnabled));
        p.setProperty("haloSize", Integer.toString(XakaVisualsClient.haloSize));
        p.setProperty("bodyMarkerEnabled", Boolean.toString(XakaVisualsClient.bodyMarkerEnabled));
        p.setProperty("bodyMarkerSquare", Boolean.toString(XakaVisualsClient.bodyMarkerSquare));
        p.setProperty("bodyMarkerSize", Integer.toString(XakaVisualsClient.bodyMarkerSize));
        p.setProperty("bodyMarkerOffsetX", Float.toString(XakaVisualsClient.bodyMarkerOffsetX));
        p.setProperty("bodyMarkerOffsetY", Float.toString(XakaVisualsClient.bodyMarkerOffsetY));
        p.setProperty("bodyMarkerOffsetZ", Float.toString(XakaVisualsClient.bodyMarkerOffsetZ));
        p.setProperty("accentIndex", Integer.toString(XakaVisualsClient.accentIndex));
        p.setProperty("petEnabled", Boolean.toString(XakaVisualsClient.petEnabled));
        p.setProperty("petType", Integer.toString(XakaVisualsClient.petType));
        p.setProperty("petSize", Integer.toString(XakaVisualsClient.petSize));
        p.setProperty("wingsEnabled", Boolean.toString(XakaVisualsClient.wingsEnabled));
        p.setProperty("wingStyle", Integer.toString(XakaVisualsClient.wingStyle));
        p.setProperty("wingSpeed", Float.toString(XakaVisualsClient.wingSpeed));
        p.setProperty("hatEnabled", Boolean.toString(XakaVisualsClient.hatEnabled));
        p.setProperty("hatStyle", Integer.toString(XakaVisualsClient.hatStyle));
        p.setProperty("handOffsetX", Float.toString(XakaVisualsClient.handOffsetX));
        p.setProperty("handOffsetY", Float.toString(XakaVisualsClient.handOffsetY));
        p.setProperty("handOffsetZ", Float.toString(XakaVisualsClient.handOffsetZ));
        p.setProperty("handRotationX", Float.toString(XakaVisualsClient.handRotationX));
        p.setProperty("handRotationY", Float.toString(XakaVisualsClient.handRotationY));
        p.setProperty("handRotationZ", Float.toString(XakaVisualsClient.handRotationZ));
        p.setProperty("fpsBoost", Boolean.toString(XakaVisualsClient.fpsBoost));
        p.setProperty("droppedItemTilt", Boolean.toString(XakaVisualsClient.droppedItemTilt));
        p.setProperty("customCrosshair", Boolean.toString(XakaVisualsClient.customCrosshair));
        p.setProperty("crosshairSize", Integer.toString(XakaVisualsClient.crosshairSize));
        p.setProperty("crosshairGap", Integer.toString(XakaVisualsClient.crosshairGap));
        p.setProperty("crosshairThickness", Integer.toString(XakaVisualsClient.crosshairThickness));
        p.setProperty("crosshairDot", Boolean.toString(XakaVisualsClient.crosshairDot));
        p.setProperty("crosshairDotSize", Integer.toString(XakaVisualsClient.crosshairDotSize));
        p.setProperty("crosshairOutline", Boolean.toString(XakaVisualsClient.crosshairOutline));
        p.setProperty("crosshairOutlineWidth", Integer.toString(XakaVisualsClient.crosshairOutlineWidth));
        p.setProperty("autoSprint", Boolean.toString(XakaVisualsClient.autoSprint));
        p.setProperty("lowArmorNotify", Boolean.toString(XakaVisualsClient.lowArmorNotify));
        p.setProperty("deathCoords", Boolean.toString(XakaVisualsClient.deathCoords));
        p.setProperty("hitParticles", Boolean.toString(XakaVisualsClient.hitParticles));
        p.setProperty("jumpCircle", Boolean.toString(XakaVisualsClient.jumpCircle));
        p.setProperty("jumpEffect", Boolean.toString(XakaVisualsClient.jumpEffect));
        p.setProperty("jumpCubes", Boolean.toString(XakaVisualsClient.jumpCubes));
        p.setProperty("mouseParticles", Boolean.toString(XakaVisualsClient.mouseParticles));
        p.setProperty("mouseParticleSize", Integer.toString(XakaVisualsClient.mouseParticleSize));
        p.setProperty("particleDensity", Integer.toString(XakaVisualsClient.particleDensity));
        p.setProperty("jumpCubeCount", Integer.toString(XakaVisualsClient.jumpCubeCount));
        p.setProperty("jumpCubeSize", Integer.toString(XakaVisualsClient.jumpCubeSize));
        p.setProperty("jumpEffectRadius", Integer.toString(XakaVisualsClient.jumpEffectRadius));
        p.setProperty("jumpEffectDuration", Integer.toString(XakaVisualsClient.jumpEffectDuration));
        p.setProperty("playerTrails", Boolean.toString(XakaVisualsClient.playerTrails));
        p.setProperty("noFluidOverlay", Boolean.toString(XakaVisualsClient.noFluidOverlay));
        p.setProperty("noFireOverlay", Boolean.toString(XakaVisualsClient.noFireOverlay));
        p.setProperty("noHurtCam", Boolean.toString(XakaVisualsClient.noHurtCam));
        p.setProperty("noWeather", Boolean.toString(XakaVisualsClient.noWeather));
        p.setProperty("ambienceTimeEnabled", Boolean.toString(XakaVisualsClient.ambienceTimeEnabled));
        p.setProperty("ambienceTime", Integer.toString(Math.floorMod(XakaVisualsClient.ambienceTime, 24000)));
        p.setProperty("hideBossBar", Boolean.toString(XakaVisualsClient.hideBossBar));
        p.setProperty("noInWallOverlay", Boolean.toString(XakaVisualsClient.noInWallOverlay));
        p.setProperty("cooldownHud", Boolean.toString(XakaVisualsClient.cooldownHud));
        p.setProperty("waypointEnabled", Boolean.toString(XakaVisualsClient.waypointEnabled));
        p.setProperty("nameProtect", Boolean.toString(XakaVisualsClient.nameProtect));
        p.setProperty("shulkerPreview", Boolean.toString(XakaVisualsClient.shulkerPreview));
        p.setProperty("killEffect", Boolean.toString(XakaVisualsClient.killEffect));
        p.setProperty("waypointX", Integer.toString(XakaVisualsClient.waypointX));
        p.setProperty("waypointY", Integer.toString(XakaVisualsClient.waypointY));
        p.setProperty("waypointZ", Integer.toString(XakaVisualsClient.waypointZ));
        p.setProperty("activeDance", Integer.toString(XakaVisualsClient.activeDance));
        p.setProperty("danceSpeed", Float.toString(XakaVisualsClient.danceSpeed));
        p.setProperty("fullbright", Boolean.toString(fullbright));
        p.setProperty("musicVolume", Float.toString(XakaVisualsMusic.getVolume()));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "XakaVisuals.V2");
            }
        } catch (IOException ignored) {
            // A failed config write must not interrupt gameplay.
        }
    }

    private static boolean bool(Properties p, String key, boolean fallback) {
        return Boolean.parseBoolean(p.getProperty(key, Boolean.toString(fallback)));
    }
    private static int integer(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key, Integer.toString(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }
    private static float decimal(Properties p, String key, float fallback) {
        try {
            float value = Float.parseFloat(p.getProperty(key, Float.toString(fallback)));
            return Float.isFinite(value) ? value : fallback;
        } catch (NumberFormatException ignored) { return fallback; }
    }
}
