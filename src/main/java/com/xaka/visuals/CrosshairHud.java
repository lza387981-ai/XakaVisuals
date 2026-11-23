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
        int color = 0xFFE9A0D1;
        int shadow = 0xAA17131B;

        // Four compact brackets with a center pixel; custom overlay stays fully client-side.
        context.fill(cx - 1, cy - 7, cx + 1, cy - 2, shadow);
        context.fill(cx - 1, cy + 2, cx + 1, cy + 7, shadow);
        context.fill(cx - 7, cy - 1, cx - 2, cy + 1, shadow);
        context.fill(cx + 2, cy - 1, cx + 7, cy + 1, shadow);
        context.fill(cx - 1, cy - 6, cx + 1, cy - 2, color);
        context.fill(cx - 1, cy + 2, cx + 1, cy + 6, color);
        context.fill(cx - 6, cy - 1, cx - 2, cy + 1, color);
        context.fill(cx + 2, cy - 1, cx + 6, cy + 1, color);
        context.fill(cx, cy, cx + 1, cy + 1, 0xFFFFFFFF);
    }
}
