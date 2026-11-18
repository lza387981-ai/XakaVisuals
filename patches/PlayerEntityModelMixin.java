package dev.xaka.visuals.mixin;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.model.ModelPart;
import dev.xaka.visuals.XakaVisualsClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies a cosmetic dance to the local player's third-person model only. */
@Mixin(BipedEntityModel.class)
public abstract class PlayerEntityModelMixin {
    @Shadow @Final protected ModelPart head;
    @Shadow @Final protected ModelPart body;
    @Shadow @Final protected ModelPart rightArm;
    @Shadow @Final protected ModelPart leftArm;
    @Shadow @Final protected ModelPart rightLeg;
    @Shadow @Final protected ModelPart leftLeg;

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void xakavisuals$applyDance(BipedEntityRenderState state, CallbackInfo ci) {
        if (!XakaVisualsClient.dancePlaying || state.id != XakaVisualsClient.danceEntityId) return;

        float t = state.age * 0.22f * XakaVisualsClient.danceSpeed;
        float wave = (float) Math.sin(t);
        float wave2 = (float) Math.sin(t * 2.0f);
        switch (XakaVisualsClient.activeDance) {
            case 0 -> {
                body.yaw += wave * 0.10f;
                rightArm.pitch += wave2 * 0.75f - 0.45f;
                leftArm.pitch -= wave2 * 0.75f + 0.45f;
                rightArm.roll += 0.16f;
                leftArm.roll -= 0.16f;
                rightLeg.pitch += wave * 0.22f;
                leftLeg.pitch -= wave * 0.22f;
            }
            case 1 -> {
                body.roll += wave * 0.16f;
                rightArm.pitch -= 0.75f + wave2 * 0.30f;
                leftArm.pitch -= 0.75f - wave2 * 0.30f;
                rightLeg.pitch += wave * 0.62f;
                leftLeg.pitch -= wave * 0.62f;
                rightLeg.yaw += 0.12f;
                leftLeg.yaw -= 0.12f;
            }
            case 2 -> {
                body.yaw += wave * 0.24f;
                rightArm.pitch -= 1.10f + wave * 0.25f;
                leftArm.pitch -= 1.10f - wave * 0.25f;
                rightArm.roll += wave * 0.45f;
                leftArm.roll -= wave * 0.45f;
                rightLeg.yaw += wave * 0.12f;
                leftLeg.yaw -= wave * 0.12f;
            }
            default -> {
                body.pitch += 0.08f;
                rightArm.pitch -= 0.35f;
                leftArm.pitch -= 0.35f;
                rightLeg.pitch += wave * 0.48f;
                leftLeg.pitch -= wave * 0.48f;
                head.yaw += wave * 0.08f;
            }
        }
    }
}
