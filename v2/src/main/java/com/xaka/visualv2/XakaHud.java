package com.xaka.visualv2;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

public final class XakaHud {
    private static final int WHITE = 0xFFF3F7FF;
    private static final int MUTED = 0xFF9DB0C9;
    private static final int ACCENT = 0xFF39BDF8;
    private static final int BACKGROUND = 0xA9121B2A;

    private XakaHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        int left = 8;
        int top = 8;

        if (XakaClickScreen.isEnabled("XAKA Watermark")) {
            drawPanel(context, left, top, 118, 30);
            context.fill(left, top, left + 3, top + 30, ACCENT);
            context.drawText(client.textRenderer, Text.literal("XAKA"), left + 10, top + 5, WHITE, true);
            context.drawText(client.textRenderer, Text.literal("VISUAL.V2"), left + 10, top + 17, MUTED, false);
            top += 38;
        }

        if (XakaClickScreen.isEnabled("Coordinates")) {
            String coords = String.format("XYZ  %.1f  %.1f  %.1f",
                    client.player.getX(), client.player.getY(), client.player.getZ());
            drawLabel(context, client, coords, left, top, WHITE);
            top += 14;
        }

        if (XakaClickScreen.isEnabled("FPS Counter")) {
            String fps = "FPS  " + client.getCurrentFps();
            drawLabel(context, client, fps, left, top, ACCENT);
        }

        if (XakaClickScreen.isEnabled("Armor Status")) {
            String armor = "ARMOR  " + client.player.getArmor();
            drawLabel(context, client, armor, 8, client.getWindow().getScaledHeight() - 24, WHITE);
        }

        if (XakaClickScreen.isEnabled("Keystrokes")) {
            drawKeystrokes(context, client);
        }

        if (XakaClickScreen.isEnabled("Target Info")) {
            Entity target = client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit ? hit.getEntity() : null;
            if (target instanceof LivingEntity living && target.isAlive()) {
                String name = target.getName().getString();
                String health = String.format("HP  %.1f", living.getHealth());
                int x = client.getWindow().getScaledWidth() / 2 + 12;
                int y = context.getScaledWindowHeight() / 2 + 8;
                drawPanel(context, x, y, 112, 34);
                context.drawText(client.textRenderer, Text.literal(name), x + 7, y + 5, WHITE, true);
                context.drawText(client.textRenderer, Text.literal(health), x + 7, y + 19, ACCENT, false);
            }
        }
    }

    private static void drawKeystrokes(DrawContext context, MinecraftClient client) {
        int x = 8;
        int y = context.getScaledWindowHeight() - 68;
        drawKey(context, client, "W", client.options.forwardKey.isPressed(), x + 22, y);
        drawKey(context, client, "A", client.options.leftKey.isPressed(), x, y + 22);
        drawKey(context, client, "S", client.options.backKey.isPressed(), x + 22, y + 22);
        drawKey(context, client, "D", client.options.rightKey.isPressed(), x + 44, y + 22);
        drawKey(context, client, "JUMP", client.options.jumpKey.isPressed(), x, y + 44, 66);
    }

    private static void drawKey(DrawContext context, MinecraftClient client,
                                String label, boolean pressed, int x, int y) {
        drawKey(context, client, label, pressed, x, y, 20);
    }

    private static void drawKey(DrawContext context, MinecraftClient client,
                                String label, boolean pressed, int x, int y, int width) {
        context.fill(x, y, x + width, y + 18, pressed ? 0xFF176B83 : BACKGROUND);
        context.drawBorder(x, y, width, 18, pressed ? ACCENT : 0xFF53647A);
        int textWidth = client.textRenderer.getWidth(label);
        context.drawText(client.textRenderer, Text.literal(label),
                x + (width - textWidth) / 2, y + 5, WHITE, false);
    }

    private static void drawLabel(DrawContext context, MinecraftClient client,
                                  String label, int x, int y, int color) {
        int width = client.textRenderer.getWidth(label) + 12;
        drawPanel(context, x, y, width, 16);
        context.drawText(client.textRenderer, Text.literal(label), x + 6, y + 4, color, false);
    }

    private static void drawPanel(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, BACKGROUND);
    }
}
