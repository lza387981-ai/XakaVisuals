package dev.xaka.visuals.mixin;

import dev.xaka.visuals.XakaVisualsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void xakavisuals$hideBossBar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (XakaVisualsClient.hideBossBar) {
            ci.cancel();
        }
    }
}
