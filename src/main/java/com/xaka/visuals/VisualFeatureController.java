package com.xaka.visuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;

/**
 * Applies client-only visual features without changing server state.
 */
public final class VisualFeatureController {
    private static Double previousGamma;
    private static int trailTick;

    private VisualFeatureController() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(VisualFeatureController::tick);
    }

    private static void tick(MinecraftClient client) {
        Feature fullbright = FeatureRegistry.find("Fullbright");
        boolean fullbrightEnabled = fullbright != null && fullbright.enabled();
        if (client.options != null) {
            if (fullbrightEnabled) {
                if (previousGamma == null) previousGamma = client.options.getGamma().getValue();
                if (client.options.getGamma().getValue() < 16.0) {
                    client.options.getGamma().setValue(16.0);
                }
            } else if (previousGamma != null) {
                client.options.getGamma().setValue(previousGamma);
                previousGamma = null;
            }
        }

        Feature trails = FeatureRegistry.find("Trails");
        if (trails == null || !trails.enabled() || client.player == null || client.world == null) return;
        if (++trailTick % 2 != 0) return;

        // Cosmetic client-side particles only; no packets or server-side effects.
        client.world.addParticle(ParticleTypes.END_ROD,
                client.player.getX(),
                client.player.getY() + 0.08,
                client.player.getZ(),
                0.0, 0.015, 0.0);
    }
}
