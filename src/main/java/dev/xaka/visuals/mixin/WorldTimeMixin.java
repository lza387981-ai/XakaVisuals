package dev.xaka.visuals.mixin;

import dev.xaka.visuals.XakaVisualsClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Changes only the time value read by the local client world for visual calculations. */
@Mixin(World.class)
public abstract class WorldTimeMixin {
    @Inject(method = "getTimeOfDay()J", at = @At("RETURN"), cancellable = true)
    private void xakavisuals$overrideClientVisualTime(CallbackInfoReturnable<Long> cir) {
        if (this instanceof ClientWorld && XakaVisualsClient.ambienceTimeEnabled) {
            cir.setReturnValue((long) Math.floorMod(XakaVisualsClient.ambienceTime, 24000));
        }
    }
}
