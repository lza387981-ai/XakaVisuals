package com.xaka.visuals;

/** Persistent client-side visual tuning values. */
public final class FeatureSettings {
    public static double targetEspLength = 0.28;
    public static double targetEspHeight = 0.0;
    public static double hatScale = 1.0;
    public static double hatHeight = 0.82;
    public static int hatAlpha = 145;
    public static double hudScale = 1.0;
    public static int hudAlpha = 235;
    public static double handX = 0.0;
    public static double handY = 0.0;
    public static double handZ = 0.0;

    private FeatureSettings() {}

    public static void load(java.util.Properties p) {
        targetEspLength = decimal(p, "setting.targetEspLength", 0.28, 0.10, 0.50);
        targetEspHeight = decimal(p, "setting.targetEspHeight", 0.0, -0.8, 0.8);
        hatScale = decimal(p, "setting.hatScale", 1.0, 0.4, 2.0);
        hatHeight = decimal(p, "setting.hatHeight", 0.82, 0.2, 1.6);
        hatAlpha = integer(p, "setting.hatAlpha", 145, 30, 255);
        hudScale = decimal(p, "setting.hudScale", 1.0, 0.6, 1.8);
        hudAlpha = integer(p, "setting.hudAlpha", 235, 50, 255);
        handX = decimal(p, "setting.handX", 0.0, -1.0, 1.0);
        handY = decimal(p, "setting.handY", 0.0, -1.0, 1.0);
        handZ = decimal(p, "setting.handZ", 0.0, -1.0, 1.0);
    }

    public static void save(java.util.Properties p) {
        p.setProperty("setting.targetEspLength", Double.toString(targetEspLength));
        p.setProperty("setting.targetEspHeight", Double.toString(targetEspHeight));
        p.setProperty("setting.hatScale", Double.toString(hatScale));
        p.setProperty("setting.hatHeight", Double.toString(hatHeight));
        p.setProperty("setting.hatAlpha", Integer.toString(hatAlpha));
        p.setProperty("setting.hudScale", Double.toString(hudScale));
        p.setProperty("setting.hudAlpha", Integer.toString(hudAlpha));
        p.setProperty("setting.handX", Double.toString(handX));
        p.setProperty("setting.handY", Double.toString(handY));
        p.setProperty("setting.handZ", Double.toString(handZ));
    }

    private static double decimal(java.util.Properties p, String key, double fallback, double min, double max) {
        try { return Math.max(min, Math.min(max, Double.parseDouble(p.getProperty(key, Double.toString(fallback))))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static int integer(java.util.Properties p, String key, int fallback, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(p.getProperty(key, Integer.toString(fallback))))); }
        catch (NumberFormatException ignored) { return fallback; }
    }
}
