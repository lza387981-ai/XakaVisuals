package com.xaka.visuals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FeatureRegistry {
    private static final List<Feature> FEATURES = new ArrayList<>();
    private static boolean initialized;

    private FeatureRegistry() {}

    public static void init() {
        if (initialized) return;
        add("Target ESP", Feature.Category.RENDER, false);
        add("Fullbright", Feature.Category.RENDER, false);
        add("View Model", Feature.Category.RENDER, false);
        add("NoRender / Optimization", Feature.Category.RENDER, false);
        add("Crosshair", Feature.Category.RENDER, false);
        add("Trails", Feature.Category.RENDER, false);
        add("Aspect Ratio", Feature.Category.RENDER, false);

        add("Target Info", Feature.Category.HUD, false);
        add("Me Armor", Feature.Category.HUD, false);
        add("Hand Position", Feature.Category.HUD, false);
        add("No Fire", Feature.Category.HUD, false);

        add("Chinese Hat", Feature.Category.PLAYER, false);
        FeatureConfig.load();
        initialized = true;
    }

    private static void add(String name, Feature.Category category, boolean enabled) {
        FEATURES.add(new Feature(name, category, enabled));
    }

    public static Feature find(String name) {
        return FEATURES.stream().filter(feature -> feature.name().equals(name)).findFirst().orElse(null);
    }

    public static List<Feature> all() {
        return Collections.unmodifiableList(FEATURES);
    }

    public static List<Feature> in(Feature.Category category) {
        return FEATURES.stream().filter(feature -> feature.category() == category).toList();
    }
}
