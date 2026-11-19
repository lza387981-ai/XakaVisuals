package com.xaka.visualv2;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.particle.ParticleTypes;

public final class XakaCosmeticTrail {
    private XakaCosmeticTrail() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!XakaClickScreen.isEnabled("Cosmetic Trail")
                    || client.player == null || client.world == null
                    || client.player.age % 3 != 0) return;

            var player = client.player;
            var behind = player.getRotationVec(1.0f).multiply(-0.32);
            client.world.addParticle(ParticleTypes.END_ROD,
                    player.getX() + behind.x,
                    player.getY() + 0.12,
                    player.getZ() + behind.z,
                    0.0, 0.015, 0.0);
        });
    }
}
