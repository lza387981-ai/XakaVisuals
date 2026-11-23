package com.xaka.visuals.mixin;

import com.xaka.visuals.Feature;
import com.xaka.visuals.FeatureRegistry;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameHudMixin {
    @Inject(method="renderFireOverlay", at=@At("HEAD"), cancellable=true)
    private static void xaka$hideFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        Feature noFire=FeatureRegistry.find("No Fire");
        if(noFire!=null&&noFire.enabled()) ci.cancel();
    }
}
