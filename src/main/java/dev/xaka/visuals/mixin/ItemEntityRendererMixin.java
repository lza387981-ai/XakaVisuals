package dev.xaka.visuals.mixin;

import dev.xaka.visuals.XakaVisualsClient;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A lightweight, client-only presentation tweak for dropped item models. */
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void xaka$tiltDroppedItems(ItemEntityRenderState state, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!XakaVisualsClient.droppedItemTilt) return;
        float phase = state.age * 0.055f + state.uniqueOffset;
        float sway = (float) Math.sin(phase) * 3.0f;
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(16.0f + sway));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(
                (float) Math.cos(phase * 0.7f) * 2.0f));
    }
}
