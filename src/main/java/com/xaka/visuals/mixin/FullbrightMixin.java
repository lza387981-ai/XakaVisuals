package com.xaka.visuals.mixin;

import com.xaka.visuals.Feature;
import com.xaka.visuals.FeatureRegistry;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public abstract class FullbrightMixin {
    @Inject(method = "getBrightness(Lnet/minecraft/world/dimension/DimensionType;I)F",
            at = @At("HEAD"), cancellable = true)
    private static void xaka$fullbright(DimensionType type, int lightLevel,
                                        CallbackInfoReturnable<Float> cir) {
        if (isFullbrightEnabled()) cir.setReturnValue(1.0F);
    }

    @Inject(method = "getBrightness(FI)F", at = @At("HEAD"), cancellable = true)
    private static void xaka$fullbrightAmbient(float ambientLight, int lightLevel,
                                               CallbackInfoReturnable<Float> cir) {
        if (isFullbrightEnabled()) cir.setReturnValue(1.0F);
    }

    private static boolean isFullbrightEnabled() {
        Feature feature = FeatureRegistry.find("Fullbright");
        return feature != null && feature.enabled();
    }
}
