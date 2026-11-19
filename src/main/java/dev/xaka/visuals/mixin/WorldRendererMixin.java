package dev.xaka.visuals.mixin;

import dev.xaka.visuals.XakaVisualsClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(
            method = "renderWeather(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/util/math/Vec3d;FLnet/minecraft/client/render/Fog;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void xakavisuals$hideWeather(FrameGraphBuilder frameGraphBuilder, Vec3d pos,
                                         float tickDelta, Fog fog, CallbackInfo ci) {
        if (XakaVisualsClient.noWeather) {
            ci.cancel();
        }
    }

    @Inject(
            method = "addWeatherParticlesAndSound(Lnet/minecraft/client/render/Camera;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void xakavisuals$hideWeatherParticlesAndSound(Camera camera, CallbackInfo ci) {
        if (XakaVisualsClient.noWeather) {
            ci.cancel();
        }
    }
}
