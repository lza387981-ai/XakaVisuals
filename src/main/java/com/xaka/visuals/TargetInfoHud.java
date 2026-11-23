package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Locale;

public final class TargetInfoHud {
    private TargetInfoHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        Feature feature = FeatureRegistry.find("Target Info");
        if (feature == null || !feature.enabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.currentScreen != null) return;
        HitResult hit = client.crosshairTarget;
        if (!(hit instanceof EntityHitResult entityHit)) return;

        Entity entity = entityHit.getEntity();
        if (!(entity instanceof LivingEntity target) || target == client.player || !target.isAlive()) return;

        float maxHealth = Math.max(1.0f, target.getMaxHealth());
        float health = Math.max(0.0f, Math.min(target.getHealth(), maxHealth));
        float ratio = health / maxHealth;
        int panelWidth = 190;
        int panelHeight = 60;
        int x = HudDragController.x("target", context.getScaledWindowWidth() - panelWidth - 12);
        int y = HudDragController.y("target", context.getScaledWindowHeight() - panelHeight - 42);

        context.fill(x, y, x + panelWidth, y + panelHeight, 0xC9101010);
        context.fill(x, y, x + panelWidth, y + 2, 0xFFE18ACB);
        context.drawText(client.textRenderer, target.getDisplayName(), x + 8, y + 7, 0xFFFFFFFF, true);

        String hp = String.format(Locale.ROOT, "%.1f / %.1f HP", health, maxHealth);
        context.drawText(client.textRenderer, hp, x + 8, y + 20, 0xFFE6E6E6, false);
        context.drawText(client.textRenderer, "БРОНЯ ВРАГА: " + target.getArmor(),
                x + 8, y + 32, 0xFFD8B5D0, false);

        int barX = x + 8;
        int barY = y + 46;
        int barWidth = panelWidth - 16;
        context.fill(barX, barY, barX + barWidth, barY + 6, 0xFF454545);
        int fillColor = ratio > 0.6f ? 0xFF63D471 : ratio > 0.3f ? 0xFFE6C45A : 0xFFE45B67;
        context.fill(barX, barY, barX + Math.round(barWidth * ratio), barY + 6, fillColor);
    }
}
