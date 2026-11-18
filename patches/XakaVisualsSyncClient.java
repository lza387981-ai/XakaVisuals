package dev.xaka.visuals;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

/** Client-side cache and change-driven sender for remote cosmetic state. */
public final class XakaVisualsSyncClient {
    private static final Map<UUID, XakaVisualsSyncPayload> STATES = new ConcurrentHashMap<>();
    private static XakaVisualsSyncPayload lastSent;
    private static int tickCounter;

    private XakaVisualsSyncClient() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(XakaVisualsSyncPayload.ID, (payload, context) ->
                MinecraftClient.getInstance().execute(() -> {
                    if (MinecraftClient.getInstance().world != null) {
                        STATES.put(payload.playerId(), payload);
                    }
                }));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            STATES.clear();
            lastSent = null;
            tickCounter = 0;
        });
    }

    public static void tick(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null || client.world == null) {
            lastSent = null;
            tickCounter = 0;
            return;
        }
        if (++tickCounter % 5 != 0) return;

        XakaVisualsSyncPayload current = new XakaVisualsSyncPayload(
                player.getUuid(),
                XakaVisualsClient.petEnabled,
                XakaVisualsClient.petType,
                XakaVisualsClient.petSize,
                XakaVisualsClient.wingsEnabled,
                XakaVisualsClient.wingStyle,
                XakaVisualsClient.wingSpeed,
                XakaVisualsClient.dancePlaying,
                XakaVisualsClient.activeDance,
                XakaVisualsClient.danceSpeed,
                XakaVisualsClient.danceStartAge,
                XakaVisualsClient.accent());

        if (!current.equals(lastSent)) {
            // Vanilla servers simply do not advertise this channel, so no packet is sent there.
            if (ClientPlayNetworking.canSend(XakaVisualsSyncPayload.ID)) {
                ClientPlayNetworking.send(current);
            }
            lastSent = current;
        }
    }

    public static XakaVisualsSyncPayload getState(UUID playerId) {
        return STATES.get(playerId);
    }
}
