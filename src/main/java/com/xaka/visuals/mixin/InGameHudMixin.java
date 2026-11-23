package com.xaka.visuals.mixin;

import com.xaka.visuals.Feature;
import com.xaka.visuals.FeatureRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method="renderFireOverlay", at=@At("HEAD"), cancellable=true)
    private void xaka$hideFireOverlay(DrawContext context, CallbackInfo ci) {
        Feature noFire=FeatureRegistry.find("No Fire");
        if (noFire!=null&&noFire.enabled()) ci.cancel();
    }
}
