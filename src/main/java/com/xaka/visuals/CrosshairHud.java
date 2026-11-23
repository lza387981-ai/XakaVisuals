package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class CrosshairHud {
    private CrosshairHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        Feature feature = FeatureRegistry.find("Crosshair");
        MinecraftClient client = MinecraftClient.getInstance();
        if (feature == null || !feature.enabled() || client.player == null
                || client.currentScreen != null || client.options.hudHidden) return;

        int cx = context.getScaledWindowWidth() / 2;
        int cy = context.getScaledWindowHeight() / 2;
        int size = (int)Math.round(FeatureSettings.crosshairSize);
        int gap = (int)Math.round(FeatureSettings.crosshairGap);
        int color = FeatureSettings.crosshairColor;
        int shadow = 0xAA17131B;

        context.fill(cx - 1, cy - gap - size, cx + 1, cy - gap, shadow);
        context.fill(cx - 1, cy + gap, cx + 1, cy + gap + size, shadow);
        context.fill(cx - gap - size, cy - 1, cx - gap, cy + 1, shadow);
        context.fill(cx + gap, cy - 1, cx + gap + size, cy + 1, shadow);
        context.fill(cx - 1, cy - gap - size, cx + 1, cy - gap, color);
        context.fill(cx - 1, cy + gap, cx + 1, cy + gap + size, color);
        context.fill(cx - gap - size, cy - 1, cx - gap, cy + 1, color);
        context.fill(cx + gap, cy - 1, cx + gap + size, cy + 1, color);
        context.fill(cx, cy, cx + 1, cy + 1, 0xFFFFFFFF);
    }
}
