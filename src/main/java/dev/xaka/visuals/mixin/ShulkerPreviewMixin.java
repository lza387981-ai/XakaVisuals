package dev.xaka.visuals.mixin;

import dev.xaka.visuals.XakaVisualsClient;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class ShulkerPreviewMixin {
    @Shadow
    protected Slot focusedSlot;

    @Inject(method = "render", at = @At("TAIL"))
    private void xaka$renderShulkerPreview(DrawContext context, int mouseX, int mouseY,
                                           float delta, CallbackInfo ci) {
        if (!XakaVisualsClient.shulkerPreview || focusedSlot == null) return;

        ItemStack hovered = focusedSlot.getStack();
        if (hovered.isEmpty()
                || !(hovered.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof ShulkerBoxBlock)) return;

        ContainerComponent container = hovered.get(DataComponentTypes.CONTAINER);
        if (container == null) return;

        List<ItemStack> stacks = container.stream().toList();
        MinecraftClient client = MinecraftClient.getInstance();
        int panelWidth = 180;
        int panelHeight = 88;
        int panelX = mouseX + 14;
        int panelY = mouseY + 14;
        if (panelX + panelWidth > context.getScaledWindowWidth() - 4) panelX = mouseX - panelWidth - 12;
        if (panelY + panelHeight > context.getScaledWindowHeight() - 4) panelY = mouseY - panelHeight - 12;
        panelX = Math.max(4, Math.min(panelX, context.getScaledWindowWidth() - panelWidth - 4));
        panelY = Math.max(4, Math.min(panelY, context.getScaledWindowHeight() - panelHeight - 4));

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0101420);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, XakaVisualsClient.accent());
        context.drawTextWithShadow(client.textRenderer, hovered.getName(), panelX + 8, panelY + 7, 0xFFF2F3F8);

        for (int slot = 0; slot < 27; slot++) {
            int column = slot % 9;
            int row = slot / 9;
            int x = panelX + 8 + column * 18;
            int y = panelY + 25 + row * 18;
            context.fill(x, y, x + 17, y + 17, 0xFF252D3B);
            if (slot >= stacks.size()) continue;
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) continue;
            context.drawItem(stack, x, y);
            context.drawStackOverlay(client.textRenderer, stack, x, y);
        }
    }
}
