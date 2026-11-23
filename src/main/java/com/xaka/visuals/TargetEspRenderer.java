package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** A lightweight outline around the living entity under the crosshair. */
public final class TargetEspRenderer {
    private TargetEspRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            Feature feature = FeatureRegistry.find("Target ESP");
            if (feature == null || !feature.enabled() || client.player == null
                    || client.world == null || client.crosshairTarget == null) return;
            if (!(client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit)) return;
            if (!(hit.getEntity() instanceof LivingEntity target) || target == client.player
                    || !target.isAlive()) return;

            Vec3d camera = context.camera().getPos();
            Box box = target.getBoundingBox().expand(0.035).offset(-camera.x, -camera.y, -camera.z);
            VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
            VertexRendering.drawBox(context.matrixStack(), lines, box,
                    0.92f, 0.42f, 0.82f, 0.92f);
        });
    }
}
