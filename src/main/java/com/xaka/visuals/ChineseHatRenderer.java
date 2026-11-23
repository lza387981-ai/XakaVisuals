package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Local-only cosmetic Chinese hat. It renders a translucent brim and cone
 * above the local player's head; it does not send packets or modify gameplay.
 */
public final class ChineseHatRenderer {
    private static final int SEGMENTS = 48;
    private static final float RED = 90.0f / 255.0f;
    private static final float GREEN = 235.0f / 255.0f;
    private static final float BLUE = 145.0f / 255.0f;
    private static final int ALPHA = 145;

    private ChineseHatRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            Feature feature = FeatureRegistry.find("Chinese Hat");
            PlayerEntity player = client.player;
            if (feature == null || !feature.enabled() || player == null || client.world == null) return;

            Vec3d camera = context.camera().getPos();
            Vec3d position = player.getPos();
            float x = (float) (position.x - camera.x);
            float y = (float) (position.y + player.getHeight() + 0.12 - camera.y);
            float z = (float) (position.z - camera.z);

            var matrices = context.matrixStack();
            matrices.push();
            matrices.translate(x, y, z);
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            Matrix3f normalMatrix = matrices.peek().getNormalMatrix();
            VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getDebugQuads());

            float brimRadius = 0.78f;
            float brimInner = 0.10f;
            float brimY = 0.015f;
            float coneHeight = 0.82f;
            float coneRadius = 0.43f;

            // Wide, slightly flattened annular brim.
            for (int i = 0; i < SEGMENTS; i++) {
                float a0 = (float) (Math.PI * 2.0 * i / SEGMENTS);
                float a1 = (float) (Math.PI * 2.0 * (i + 1) / SEGMENTS);
                float x0 = MathHelper.cos(a0), z0 = MathHelper.sin(a0);
                float x1 = MathHelper.cos(a1), z1 = MathHelper.sin(a1);
                vertex(vertices, positionMatrix, normalMatrix, x0 * brimRadius, brimY, z0 * brimRadius, 0, 1, 0);
                vertex(vertices, positionMatrix, normalMatrix, x1 * brimRadius, brimY, z1 * brimRadius, 0, 1, 0);
                vertex(vertices, positionMatrix, normalMatrix, x1 * brimInner, brimY, z1 * brimInner, 0, 1, 0);
                vertex(vertices, positionMatrix, normalMatrix, x0 * brimInner, brimY, z0 * brimInner, 0, 1, 0);
            }

            // Translucent conical crown.
            for (int i = 0; i < SEGMENTS; i++) {
                float a0 = (float) (Math.PI * 2.0 * i / SEGMENTS);
                float a1 = (float) (Math.PI * 2.0 * (i + 1) / SEGMENTS);
                float x0 = MathHelper.cos(a0) * coneRadius;
                float z0 = MathHelper.sin(a0) * coneRadius;
                float x1 = MathHelper.cos(a1) * coneRadius;
                float z1 = MathHelper.sin(a1) * coneRadius;
                vertex(vertices, positionMatrix, normalMatrix, x0, 0.025f, z0, x0, 0.35f, z0);
                vertex(vertices, positionMatrix, normalMatrix, x1, 0.025f, z1, x1, 0.35f, z1);
                vertex(vertices, positionMatrix, normalMatrix, 0, coneHeight, 0, 0, 1, 0);
            }
            matrices.pop();
        });
    }

    private static void vertex(VertexConsumer consumer, Matrix4f position, Matrix3f normal,
                               float x, float y, float z, float nx, float ny, float nz) {
        consumer.vertex(position, x, y, z)
                .color((int) (RED * 255), (int) (GREEN * 255), (int) (BLUE * 255), ALPHA)
                .normal(normal, nx, ny, nz);
    }
}
