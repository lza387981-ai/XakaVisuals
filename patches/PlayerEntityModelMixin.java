package dev.xaka.visuals.mixin;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
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
        if (!XakaVisualsClient.dancePlaying || !(state instanceof PlayerEntityRenderState playerState)
                || playerState.id != XakaVisualsClient.danceEntityId) return;

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
            case 3 -> {
                body.pitch += 0.08f;
                rightArm.pitch -= 0.35f;
                leftArm.pitch -= 0.35f;
                rightLeg.pitch += wave * 0.48f;
                leftLeg.pitch -= wave * 0.48f;
                head.yaw += wave * 0.08f;
            }
            default -> {
                // XakaDance: energetic breakdance-inspired low spin with alternating
                // floor-support arms and wide scissoring legs.
                float spin = t * 1.65f;
                float spinWave = (float) Math.sin(spin);
                float spinWave2 = (float) Math.cos(spin);
                body.pitch += 0.92f + spinWave2 * 0.16f;
                body.roll += spinWave * 0.48f;
                body.yaw += wave * 0.22f;
                head.pitch -= 0.12f;
                rightArm.pitch += 1.05f + spinWave * 0.72f;
                leftArm.pitch += 0.15f - spinWave * 0.55f;
                rightArm.roll += 0.48f + spinWave2 * 0.42f;
                leftArm.roll -= 0.48f + spinWave2 * 0.42f;
                rightLeg.pitch += -0.72f + spinWave * 0.92f;
                leftLeg.pitch += 0.72f - spinWave * 0.92f;
                rightLeg.yaw += 0.34f + spinWave2 * 0.24f;
                leftLeg.yaw -= 0.34f + spinWave2 * 0.24f;
            }
        }
    }
}
