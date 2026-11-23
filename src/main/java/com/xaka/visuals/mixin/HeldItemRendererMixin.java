package com.xaka.visuals.mixin;

import com.xaka.visuals.Feature;
import com.xaka.visuals.FeatureRegistry;
import com.xaka.visuals.FeatureSettings;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @ModifyVariable(method="renderFirstPersonItem", at=@At("HEAD"), argsOnly=true, ordinal=0)
    private MatrixStack xaka$applyViewModel(MatrixStack matrices) {
        Feature view=FeatureRegistry.find("View Model");
        Feature hand=FeatureRegistry.find("Hand Position");
        if ((view!=null&&view.enabled())||(hand!=null&&hand.enabled())) {
            matrices.translate(FeatureSettings.handX,FeatureSettings.handY,FeatureSettings.handZ);
        }
        return matrices;
    }

    @ModifyVariable(method="renderFirstPersonItem", at=@At("HEAD"), argsOnly=true, ordinal=2)
    private float xaka$adjustSwordSwing(float swingProgress) {
        Feature sword=FeatureRegistry.find("Sword Animations");
        if (sword==null||!sword.enabled()) return swingProgress;
        return MathHelper.clamp(swingProgress*(float)FeatureSettings.swordSpeed,0.0f,1.0f);
    }
}
