package dev.xaka.visuals.mixin;

import dev.xaka.visuals.XakaVisualsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
    @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
    private static void xaka$hideUnderwaterOverlay(MinecraftClient client, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (XakaVisualsClient.noFluidOverlay) ci.cancel();
    }
    @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
    private static void xaka$hideInWallOverlay(Sprite sprite, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (XakaVisualsClient.noInWallOverlay) ci.cancel();
    }

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void xaka$hideFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
            CallbackInfo ci) {
        if (XakaVisualsClient.noFireOverlay) ci.cancel();
    }
}
