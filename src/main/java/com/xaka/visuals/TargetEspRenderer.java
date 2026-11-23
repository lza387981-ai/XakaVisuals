package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class TargetEspRenderer {
    private static final float ALPHA = 0.96f;
    private TargetEspRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            Feature feature = FeatureRegistry.find("Target ESP");
            if (feature == null || !feature.enabled() || client.player == null || client.world == null
                    || !(client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit)) return;
            if (!(hit.getEntity() instanceof LivingEntity target) || target == client.player || !target.isAlive()) return;

            int color = FeatureSettings.targetEspColor;
            float red = ((color >> 16) & 255) / 255.0f;
            float green = ((color >> 8) & 255) / 255.0f;
            float blue = (color & 255) / 255.0f;

            // A camera-facing marker around the torso, deliberately independent
            // from the entity bounding box/hitbox.
            Vec3d camera = context.camera().getPos();
            double markerY = target.getBodyY(0.5) + FeatureSettings.targetEspHeight;
            double half = 0.24 + FeatureSettings.targetEspLength * 0.55;
            MatrixStack matrices = context.matrixStack();
            matrices.push();
            matrices.translate(target.getX() - camera.x, markerY - camera.y, target.getZ() - camera.z);
            matrices.multiply(context.camera().getRotation());

            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();
            VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
            if (FeatureSettings.targetEspCircle) {
                int segments = 32;
                for (int i = 0; i < segments; i++) {
                    double a0 = Math.PI * 2.0 * i / segments;
                    double a1 = Math.PI * 2.0 * (i + 1) / segments;
                    line(lines, matrix, entry, Math.cos(a0) * half, Math.sin(a0) * half,
                            Math.cos(a1) * half, Math.sin(a1) * half, red, green, blue);
                }
            } else {
                line(lines, matrix, entry, -half, -half, half, -half, red, green, blue);
                line(lines, matrix, entry, half, -half, half, half, red, green, blue);
                line(lines, matrix, entry, half, half, -half, half, red, green, blue);
                line(lines, matrix, entry, -half, half, -half, -half, red, green, blue);
            }
            matrices.pop();
        });
    }

    private static void line(VertexConsumer consumer, Matrix4f matrix, MatrixStack.Entry entry,
                             double x1, double y1, double x2, double y2,
                             float red, float green, float blue) {
        float nx = (float) (x2 - x1);
        float ny = (float) (y2 - y1);
        float length = (float) Math.sqrt(nx * nx + ny * ny);
        if (length > 0) { nx /= length; ny /= length; }
        consumer.vertex(matrix, (float) x1, (float) y1, 0)
                .color(red, green, blue, ALPHA).normal(entry, nx, ny, 0);
        consumer.vertex(matrix, (float) x2, (float) y2, 0)
                .color(red, green, blue, ALPHA).normal(entry, nx, ny, 0);
    }
}
