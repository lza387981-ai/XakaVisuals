package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public final class MeArmorHud {
    private MeArmorHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        Feature feature = FeatureRegistry.find("Me Armor");
        if (feature == null || !feature.enabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen != null) return;

        int slotSize = 22;
        int gap = 4;
        int totalWidth = slotSize * 4 + gap * 3;
        int x = (context.getScaledWindowWidth() - totalWidth) / 2;
        int y = context.getScaledWindowHeight() - 54;

        context.fill(x - 5, y - 5, x + totalWidth + 5, y + slotSize + 12, 0xB8101010);
        DefaultedList<ItemStack> armor = client.player.getInventory().armor;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = armor.get(3 - i);
            int slotX = x + i * (slotSize + gap);
            context.fill(slotX, y, slotX + slotSize, y + slotSize, 0xFF292929);
            if (!stack.isEmpty()) {
                context.drawItem(stack, slotX + 3, y + 3);
                if (stack.isDamageable()) {
                    int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamage());
                    int percent = Math.round(remaining * 100.0f / stack.getMaxDamage());
                    int color = percent > 60 ? 0xFF62D47B : percent > 25 ? 0xFFE8C65C : 0xFFE45B67;
                    context.drawCenteredTextWithShadow(client.textRenderer, percent + "%",
                            slotX + slotSize / 2, y + slotSize + 1, color);
                }
            } else {
                context.drawCenteredTextWithShadow(client.textRenderer, "-",
                        slotX + slotSize / 2, y + 7, 0xFF777777);
            }
        }
    }
}
