package dev.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
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
        double baseX = position.x - camera.x;
        double baseY = position.y - camera.y;
        double baseZ = position.z - camera.z;
        VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
        int color = XakaVisualsClient.accent();
        int a = (color >>> 24) & 255;
        int r = (color >>> 16) & 255;
        int g = (color >>> 8) & 255;
        int b = color & 255;
        int argb = (a << 24) | (r << 16) | (g << 8) | b;
        if (halo) {
            float radius = 0.22f + XakaVisualsClient.haloSize * 0.025f;
            drawCircle(lines, baseX, baseY, baseZ, radius, player.getHeight() + 0.18f, argb, 48);
        }
        if (marker) {
            float radius = 0.18f + XakaVisualsClient.bodyMarkerSize * 0.025f;
            float y = player.getHeight() * 0.55f + XakaVisualsClient.bodyMarkerOffsetY;
            // Rotate horizontal offsets with the player's facing direction so
            // left/right and forward/back stay intuitive while the player turns.
            double yaw = Math.toRadians(player.getYaw());
            double localX = XakaVisualsClient.bodyMarkerOffsetX;
            double localZ = XakaVisualsClient.bodyMarkerOffsetZ;
            double markerX = baseX + localX * Math.cos(yaw) - localZ * Math.sin(yaw);
            double markerZ = baseZ + localX * Math.sin(yaw) + localZ * Math.cos(yaw);
            if (XakaVisualsClient.bodyMarkerSquare) {
                drawSquare(lines, markerX, baseY, markerZ, radius, y, argb);
            } else {
                drawCircle(lines, markerX, baseY, markerZ, radius, y, argb, 40);
            }
        }
    }

    private static void drawCircle(VertexConsumer out, double x, double y0, double z, float radius,
                                   float y, int color, int segments) {
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i + 1) / segments;
            line(out, x + Math.cos(a0) * radius, y0 + y, z + Math.sin(a0) * radius,
                    x + Math.cos(a1) * radius, y0 + y, z + Math.sin(a1) * radius, color);
        }
    }

    private static void drawSquare(VertexConsumer out, double x, double y0, double z,
                                   float radius, float y, int color) {
        line(out, x - radius, y0 + y, z - radius, x + radius, y0 + y, z - radius, color);
        line(out, x + radius, y0 + y, z - radius, x + radius, y0 + y, z + radius, color);
        line(out, x + radius, y0 + y, z + radius, x - radius, y0 + y, z + radius, color);
        line(out, x - radius, y0 + y, z + radius, x - radius, y0 + y, z - radius, color);
    }

    private static void line(VertexConsumer out, double x1, double y1, double z1,
                             double x2, double y2, double z2, int color) {
        int light = 0x00F000F0;
        out.vertex((float)x1, (float)y1, (float)z1, color, 0.0f, 0.0f, 0, light, 0.0f, 1.0f, 0.0f);
        out.vertex((float)x2, (float)y2, (float)z2, color, 0.0f, 0.0f, 0, light, 0.0f, 1.0f, 0.0f);
    }
}
