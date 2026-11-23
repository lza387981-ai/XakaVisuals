package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public final class MeArmorHud {
    private MeArmorHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        Feature feature = FeatureRegistry.find("Me Armor");
        MinecraftClient client = MinecraftClient.getInstance();
        if (feature == null || !feature.enabled() || client.player == null || client.currentScreen != null) return;

        int x = context.getScaledWindowWidth() / 2 - 76;
        int y = context.getScaledWindowHeight() - 58;
        context.fill(x - 5, y - 5, x + 4 * 36 - 3, y + 38, 0xA9101015);

        // Armor inventory order: boots, leggings, chestplate, helmet.
        for (int i = 0; i < 4; i++) {
            ItemStack stack = client.player.getInventory().armor.get(3 - i);
            int slotX = x + i * 36;
            context.fill(slotX, y, slotX + 32, y + 32, 0xFF252832);
            if (stack.isEmpty()) {
                context.drawCenteredTextWithShadow(client.textRenderer,
                        switch (i) { case 0 -> "H"; case 1 -> "C"; case 2 -> "L"; default -> "B"; },
                        slotX + 16, y + 12, 0xFF686E7B);
                continue;
            }

            context.drawItem(stack, slotX + 8, y + 1);
            if (!stack.isDamageable()) {
                context.drawCenteredTextWithShadow(client.textRenderer, "—", slotX + 16, y + 22, 0xFFB9BEC9);
                continue;
            }

            int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamage());
            int percent = Math.round(remaining * 100.0f / stack.getMaxDamage());
            int color = percent > 60 ? 0xFF70D98B : percent > 25 ? 0xFFE6C66A : 0xFFE66B75;
            context.drawCenteredTextWithShadow(client.textRenderer, percent + "%", slotX + 16, y + 22, color);
            context.fill(slotX + 4, y + 29, slotX + 28, y + 31, 0xFF494D57);
            context.fill(slotX + 4, y + 29, slotX + 4 + Math.round(24 * percent / 100.0f), y + 31, color);
        }
    }
}
