package dev.xaka.visuals.mixin;

import dev.xaka.visuals.XakaVisualsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies original local-only cosmetic emotes to the player's rendered model. */
@Mixin(BipedEntityModel.class)
public abstract class PlayerEntityModelMixin {
    @Shadow @Final protected ModelPart head;
    @Shadow @Final protected ModelPart body;
    @Shadow @Final protected ModelPart rightArm;
    @Shadow @Final protected ModelPart leftArm;
    @Shadow @Final protected ModelPart rightLeg;
    @Shadow @Final protected ModelPart leftLeg;

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void xaka$applyLocalDance(BipedEntityRenderState state, CallbackInfo ci) {
        if (!(state instanceof PlayerEntityRenderState playerState)) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null
                || client.world.getEntityById(playerState.id) != client.player
                || !XakaVisualsClient.dancePlaying) return;

        float t = state.age * 0.22f * XakaVisualsClient.danceSpeed;
        float wave = (float) Math.sin(t);
        float wave2 = (float) Math.sin(t * 2.0f);
        switch (Math.floorMod(XakaVisualsClient.activeDance, 6)) {
            case 0 -> { // Robot
                body.yaw += wave * 0.10f;
                rightArm.pitch += wave2 * 0.75f - 0.45f;
                leftArm.pitch -= wave2 * 0.75f + 0.45f;
                rightArm.roll += 0.16f;
                leftArm.roll -= 0.16f;
                rightLeg.pitch += wave * 0.22f;
                leftLeg.pitch -= wave * 0.22f;
            }
            case 1 -> { // Shuffle
                body.roll += wave * 0.16f;
                rightArm.pitch -= 0.75f + wave2 * 0.30f;
                leftArm.pitch -= 0.75f - wave2 * 0.30f;
                rightLeg.pitch += wave * 0.62f;
                leftLeg.pitch -= wave * 0.62f;
                rightLeg.yaw += 0.12f;
                leftLeg.yaw -= 0.12f;
            }
            case 2 -> { // Floss
                body.yaw += wave * 0.24f;
                rightArm.pitch -= 1.10f + wave * 0.25f;
                leftArm.pitch -= 1.10f - wave * 0.25f;
                rightArm.roll += wave * 0.45f;
                leftArm.roll -= wave * 0.45f;
                rightLeg.yaw += wave * 0.12f;
                leftLeg.yaw -= wave * 0.12f;
            }
            case 3 -> { // Moonwalk-style glide
                body.pitch += 0.08f;
                rightArm.pitch -= 0.35f;
                leftArm.pitch -= 0.35f;
                rightLeg.pitch += wave * 0.48f;
                leftLeg.pitch -= wave * 0.48f;
                head.yaw += wave * 0.08f;
            }
            case 4 -> { // XakaDance
                float spin = t * 1.65f;
                float spinWave = (float) Math.sin(spin);
                float spinWave2 = (float) Math.cos(spin);
                body.pitch += 0.65f + spinWave2 * 0.12f;
                body.roll += spinWave * 0.40f;
                body.yaw += wave * 0.22f;
                rightArm.pitch += 0.95f + spinWave * 0.55f;
                leftArm.pitch += 0.15f - spinWave * 0.45f;
                rightArm.roll += 0.40f + spinWave2 * 0.35f;
                leftArm.roll -= 0.40f + spinWave2 * 0.35f;
                rightLeg.pitch += -0.60f + spinWave * 0.75f;
                leftLeg.pitch += 0.60f - spinWave * 0.75f;
            }
            case 5 -> { // HohoDance short finishing pose
                float elapsed = Math.max(0.0f, state.age - XakaVisualsClient.danceStartAge);
                if (elapsed < 30.0f) {
                    float spin = elapsed / 30.0f * (float) (Math.PI * 2.0);
                    body.pitch += 0.90f;
                    body.roll += spin;
                    rightArm.pitch += 1.10f;
                    leftArm.pitch += 1.10f;
                    rightLeg.pitch -= 0.55f;
                    leftLeg.pitch += 0.55f;
                } else {
                    body.pitch += 0.28f;
                    rightArm.pitch -= 0.30f;
                    leftArm.pitch -= 0.30f;
                    rightLeg.pitch += 1.20f;
                }
            }
        }
    }
}
