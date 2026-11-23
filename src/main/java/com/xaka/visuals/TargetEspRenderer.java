package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Camera-facing neon corner brackets around the target, not an entity hitbox. */
public final class TargetEspRenderer {
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

            Vec3d camera = context.camera().getPos();
            double centerY = target.getBodyY(0.5) + FeatureSettings.targetEspHeight;
            double halfWidth = 0.34 + FeatureSettings.targetEspLength * 0.48;
            double halfHeight = 0.48;
            double armX = Math.min(0.27, halfWidth * 0.58);
            double armY = 0.25;

            MatrixStack matrices = context.matrixStack();
            matrices.push();
            matrices.translate(target.getX() - camera.x, centerY - camera.y, target.getZ() - camera.z);
            matrices.multiply(context.camera().getRotation());

            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();
            VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());

            // Four separate L-shaped neon brackets: shoulder-height and hip-height.
            bracket(lines, matrix, entry, -halfWidth, halfHeight, armX, -armY, red, green, blue);
            bracket(lines, matrix, entry, halfWidth, halfHeight, -armX, -armY, red, green, blue);
            bracket(lines, matrix, entry, -halfWidth, -halfHeight, armX, armY, red, green, blue);
            bracket(lines, matrix, entry, halfWidth, -halfHeight, -armX, armY, red, green, blue);

            matrices.pop();
        });
    }

    private static void bracket(VertexConsumer out, Matrix4f matrix, MatrixStack.Entry entry,
                                double x, double y, double inwardX, double inwardY,
                                float red, float green, float blue) {
        double sx = Math.signum(inwardX);
        double sy = Math.signum(inwardY);
        double endX = x + inwardX;
        double endY = y + inwardY;
        // Layered parallel strokes create a broad colored body and a soft neon-like halo.
        stroke(out, matrix, entry, x, y, endX, y, red, green, blue);
        stroke(out, matrix, entry, endX, y, endX, endY, red, green, blue);
    }

    private static void stroke(VertexConsumer out, Matrix4f matrix, MatrixStack.Entry entry,
                               double x1, double y1, double x2, double y2,
                               float red, float green, float blue) {
        double dx = x2 - x1, dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 1.0e-5) return;
        double px = -dy / length, py = dx / length;
        double[] offsets = {-0.030, -0.020, -0.010, 0.0, 0.010, 0.020, 0.030};
        float[] alphas = {0.16f, 0.25f, 0.62f, 1.0f, 0.62f, 0.25f, 0.16f};
        for (int i = 0; i < offsets.length; i++) {
            double ox = px * offsets[i], oy = py * offsets[i];
            line(out, matrix, entry, x1 + ox, y1 + oy, x2 + ox, y2 + oy,
                    red, green, blue, alphas[i]);
        }
    }

    private static void line(VertexConsumer out, Matrix4f matrix, MatrixStack.Entry entry,
                             double x1, double y1, double x2, double y2,
                             float red, float green, float blue, float alpha) {
        float nx = (float) (x2 - x1);
        float ny = (float) (y2 - y1);
        float length = (float) Math.sqrt(nx * nx + ny * ny);
        if (length > 0) { nx /= length; ny /= length; }
        out.vertex(matrix, (float) x1, (float) y1, 0).color(red, green, blue, alpha).normal(entry, nx, ny, 0);
        out.vertex(matrix, (float) x2, (float) y2, 0).color(red, green, blue, alpha).normal(entry, nx, ny, 0);
    }
}
