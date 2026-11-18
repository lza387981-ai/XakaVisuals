package dev.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/** Local-only decorative geometry attached to the player's own model. */
public final class XakaVisualsRenderer {
    private XakaVisualsRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(XakaVisualsRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || context.matrixStack() == null || context.consumers() == null) return;
        if (client.options.hudHidden) return;

        boolean halo = XakaVisualsClient.haloEnabled;
        boolean marker = XakaVisualsClient.bodyMarkerEnabled;
        if (!halo && !marker) return;

        Vec3d camera = context.camera().getPos();
        Vec3d position = player.getPos();
        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(position.x - camera.x, position.y - camera.y, position.z - camera.z);

        VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
        int color = XakaVisualsClient.accent();
        int a = (color >>> 24) & 255;
        int r = (color >>> 16) & 255;
        int g = (color >>> 8) & 255;
        int b = color & 255;
        MatrixStack.Entry entry = matrices.peek();

        if (halo) {
            float radius = 0.22f + XakaVisualsClient.haloSize * 0.025f;
            drawCircle(lines, entry, radius, player.getHeight() + 0.18f, r, g, b, a, 48);
        }
        if (marker) {
            float radius = 0.18f + XakaVisualsClient.bodyMarkerSize * 0.025f;
            float y = player.getHeight() * 0.55f + XakaVisualsClient.bodyMarkerOffsetY;
            if (XakaVisualsClient.bodyMarkerSquare) {
                drawSquare(lines, entry, radius, y, r, g, b, a);
            } else {
                drawCircle(lines, entry, radius, y, r, g, b, a, 40);
            }
        }
        matrices.pop();
    }

    private static void drawCircle(VertexConsumer out, MatrixStack.Entry matrix, float radius,
                                   float y, int r, int g, int b, int a, int segments) {
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i + 1) / segments;
            line(out, matrix,
                    (float) Math.cos(a0) * radius, y, (float) Math.sin(a0) * radius,
                    (float) Math.cos(a1) * radius, y, (float) Math.sin(a1) * radius,
                    r, g, b, a);
        }
    }

    private static void drawSquare(VertexConsumer out, MatrixStack.Entry matrix, float radius,
                                   float y, int r, int g, int b, int a) {
        line(out, matrix, -radius, y, -radius, radius, y, -radius, r, g, b, a);
        line(out, matrix, radius, y, -radius, radius, y, radius, r, g, b, a);
        line(out, matrix, radius, y, radius, -radius, y, radius, r, g, b, a);
        line(out, matrix, -radius, y, radius, -radius, y, -radius, r, g, b, a);
    }

    private static void line(VertexConsumer out, MatrixStack.Entry matrix,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             int r, int g, int b, int a) {
        out.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(matrix, 0.0f, 1.0f, 0.0f).next();
        out.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(matrix, 0.0f, 1.0f, 0.0f).next();
    }
}
