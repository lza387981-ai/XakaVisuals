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

        // Animate the complete player model as one unit as well as its limbs.
        // The entity itself remains stationary; only the rendered model bobs and sways.
        net.minecraft.client.model.ModelPart root =
                ((net.minecraft.client.model.Model) (Object) this).getRootPart();
        root.pivotY -= Math.abs(wave2) * 1.35f;
        root.yaw += wave * 0.14f;
        root.roll += (float) Math.sin(t * 0.5f) * 0.045f;
        if (XakaVisualsClient.activeDance == 1 || XakaVisualsClient.activeDance == 4) {
            root.roll += wave * 0.10f;
        } else if (XakaVisualsClient.activeDance == 5) {
            root.yaw += wave2 * 0.08f;
        }

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
            case 4 -> {
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
            default -> {
                // HohoDance: headspin, one forward somersault, then a held kneeling finish.
                float elapsed = Math.max(0.0f, state.age - XakaVisualsClient.danceStartAge);
                if (elapsed < 30.0f) {
                    float spin = elapsed / 30.0f * (float) (Math.PI * 2.0);
                    body.pitch += 1.35f;
                    body.roll += spin;
                    head.pitch -= 0.28f;
                    rightArm.pitch += 1.25f;
                    leftArm.pitch += 1.25f;
                    rightArm.roll += 0.28f;
                    leftArm.roll -= 0.28f;
                    rightLeg.pitch -= 0.65f;
                    leftLeg.pitch += 0.65f;
                } else if (elapsed < 65.0f) {
                    float flip = (elapsed - 30.0f) / 35.0f;
                    body.pitch += (float) (Math.PI * 2.0) * flip;
                    body.roll += (float) Math.sin(flip * Math.PI) * 0.22f;
                    rightArm.pitch += 0.45f;
                    leftArm.pitch += 0.45f;
                    rightLeg.pitch -= 0.55f;
                    leftLeg.pitch -= 0.55f;
                } else {
                    // Final kneeling pose remains until the client tick stops playback at 100 ticks.
                    body.pitch += 0.32f;
                    rightArm.pitch -= 0.35f;
                    leftArm.pitch -= 0.35f;
                    rightLeg.pitch += 1.48f;
                    leftLeg.pitch += 0.18f;
                    rightLeg.yaw += 0.12f;
                    head.pitch -= 0.12f;
                }
            }
        }
    }
}
