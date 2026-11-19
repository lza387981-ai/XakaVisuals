package dev.xaka.visuals.mixin;

import dev.xaka.visuals.XakaVisualsClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void xakavisuals$disableHurtCamera(float tickDelta, CallbackInfo ci) {
        if (XakaVisualsClient.noHurtCam) {
            ci.cancel();
        }
    }
}
