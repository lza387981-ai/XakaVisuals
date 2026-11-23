package com.xaka.visuals.mixin;

import com.xaka.visuals.Feature;
import com.xaka.visuals.FeatureRegistry;
import com.xaka.visuals.FeatureSettings;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true)
    private void xaka$adjustAspectRatio(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        Feature feature = FeatureRegistry.find("Aspect Ratio");
        if (feature == null || !feature.enabled()) return;
        Matrix4f projection = cir.getReturnValue();
        projection.m00(projection.m00() / (float) FeatureSettings.aspectRatio);
        cir.setReturnValue(projection);
    }
}
