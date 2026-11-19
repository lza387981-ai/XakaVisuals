package dev.xaka.visuals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/** Server relay for XakaVisuals cosmetic state. This class contains no client-only references. */
public final class XakaVisualsSync implements ModInitializer {
    private static final Map<UUID, XakaVisualsSyncPayload> STATES = new HashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(XakaVisualsSyncPayload.ID, XakaVisualsSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(XakaVisualsSyncPayload.ID, XakaVisualsSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(XakaVisualsSyncPayload.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            XakaVisualsSyncPayload safe = sanitize(sender.getUuid(), payload);
            STATES.put(sender.getUuid(), safe);

            // Only send to clients that have declared support for this payload.
            for (ServerPlayerEntity recipient : sender.getServerWorld().getPlayers()) {
                if (ServerPlayNetworking.canSend(recipient, XakaVisualsSyncPayload.ID)) {
                    ServerPlayNetworking.send(recipient, safe);
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity joining = handler.player;
            if (!ServerPlayNetworking.canSend(joining, XakaVisualsSyncPayload.ID)) return;
            for (XakaVisualsSyncPayload state : STATES.values()) {
                ServerPlayNetworking.send(joining, state);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.player.getUuid();
            STATES.remove(id);
            XakaVisualsSyncPayload cleared = new XakaVisualsSyncPayload(
                    id, false, 0, 5, false, 0, 1.0f, false, 0, 1.0f, 0.0f, 0xFF9B78FF, false, 0);
            server.execute(() -> {
                for (ServerPlayerEntity recipient : server.getPlayerManager().getPlayerList()) {
                    if (ServerPlayNetworking.canSend(recipient, XakaVisualsSyncPayload.ID)) {
                        ServerPlayNetworking.send(recipient, cleared);
                    }
                }
            });
        });
    }

    private static XakaVisualsSyncPayload sanitize(UUID sender, XakaVisualsSyncPayload input) {
        int petType = clamp(input.petType(), 0, 3);
        int petSize = clamp(input.petSize(), 0, 10);
        int wingStyle = clamp(input.wingStyle(), 0, 2);
        int dance = clamp(input.activeDance(), 0, 5);
        float wingSpeed = finiteClamp(input.wingSpeed(), 0.5f, 2.0f, 1.0f);\n        int hatStyle = clamp(input.hatStyle(), 0, 3);
        float danceSpeed = finiteClamp(input.danceSpeed(), 0.5f, 2.0f, 1.0f);
        float startAge = Float.isFinite(input.danceStartAge())
                ? Math.max(0.0f, Math.min(input.danceStartAge(), 1.0e7f)) : 0.0f;
        return new XakaVisualsSyncPayload(sender, input.petEnabled(), petType, petSize,
                input.wingsEnabled(), wingStyle, wingSpeed, input.dancePlaying(), dance,
                danceSpeed, startAge, input.accentColor() | 0xFF000000, input.hatEnabled(), hatStyle);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float finiteClamp(float value, float min, float max, float fallback) {
        return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
}
