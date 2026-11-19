package dev.xaka.visuals.mixin;

import dev.xaka.visuals.XakaVisualsClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void xaka$adjustFirstPersonHand(AbstractClientPlayerEntity player, float tickDelta, float pitch,
            Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (player != net.minecraft.client.MinecraftClient.getInstance().player) return;
        matrices.translate(
                XakaVisualsClient.handOffsetX,
                XakaVisualsClient.handOffsetY,
                XakaVisualsClient.handOffsetZ
        );
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(XakaVisualsClient.handRotationX));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(XakaVisualsClient.handRotationY));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(XakaVisualsClient.handRotationZ));
    }
}
