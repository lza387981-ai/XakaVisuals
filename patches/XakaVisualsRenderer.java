package dev.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.MathHelper;

/** Local-only cosmetics: a thick halo, camera-relative companion and crosshair player marker. */
public final class XakaVisualsRenderer {
    private XakaVisualsRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(XakaVisualsRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.world == null || context.matrixStack() == null || context.consumers() == null) return;
        if (client.options.hudHidden) return;

        boolean halo = XakaVisualsClient.haloEnabled;
        boolean marker = XakaVisualsClient.bodyMarkerEnabled;
        boolean pet = XakaVisualsClient.petEnabled;
        if (!halo && !marker && !pet) return;

        Vec3d camera = context.camera().getPos();
        Vec3d playerPos = player.getLerpedPos(context.tickCounter().getTickDelta(false));
        double bx = playerPos.x - camera.x;
        double by = playerPos.y - camera.y;
        double bz = playerPos.z - camera.z;
        var matrices = context.matrixStack();
        var consumers = context.consumers();
        int color = XakaVisualsClient.accent();

        if (halo) {
            float radius = 0.34f + XakaVisualsClient.haloSize * 0.035f;
            float y = player.getHeight() + 0.20f;
            // Stack rings vertically: increase halo thickness without widening its radius.
            for (int band = -5; band <= 5; band++) {
                float bandY = y + band * 0.022f;
                drawCircle(consumers.getBuffer(net.minecraft.client.render.RenderLayer.getLines()),
                        bx, by, bz, radius, bandY, color, 64);
            }
        }

        if (pet) {
            // Anchor the companion to the player body, not the camera, so turning the view
            // does not spin the pet around or make it orbit the screen.
            float tickDelta = context.tickCounter().getTickDelta(false);
            // Interpolate across the shortest angular path to avoid 360-degree snapping.
            float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);
            float playerYawRad = (float) Math.toRadians(bodyYaw);
            Vector3f left = new Vector3f((float) Math.cos(playerYawRad), 0.0f,
                    (float) Math.sin(playerYawRad));
            double bob = Math.sin(client.world.getTime() * 0.12) * 0.045;
            // The Wither is larger, so keep it farther from the player.
            double petDistance = XakaVisualsClient.petWither ? 1.28 : 0.92;
            double px = playerPos.x + left.x * petDistance;
            double py = playerPos.y + 0.55 + bob;
            double pz = playerPos.z + left.z * petDistance;
            Entity companion = XakaVisualsClient.petWither
                    ? new WitherEntity(EntityType.WITHER, client.world)
                    : new CreeperEntity(EntityType.CREEPER, client.world);
            companion.setPosition(px, py, pz);
            // Keep the pet facing the player's body direction. Camera yaw is intentionally ignored.
            companion.setYaw(0.0f);
            companion.setBodyYaw(0.0f);
            companion.setHeadYaw(0.0f);
            companion.setPitch(0.0f);
            matrices.push();
            matrices.translate(px - camera.x, py - camera.y, pz - camera.z);
            // Rotate the rendered model once; do not also pass player yaw to the dispatcher.
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-bodyYaw));
            float scale = XakaVisualsClient.petSize <= 0 ? 0.32f
                    : Math.min(0.62f, 0.25f + XakaVisualsClient.petSize * 0.035f);
            matrices.scale(scale, scale, scale);
            EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
            dispatcher.render(companion, 0.0, 0.0, 0.0, 0.0f,
                    matrices, consumers, 0x00F000F0);
            matrices.pop();
        }

        if (marker && client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit
                && hit.getEntity() instanceof PlayerEntity target && target != player) {
            Vec3d targetPos = target.getLerpedPos(context.tickCounter().getTickDelta(false));
            // Camera-facing ring, slightly toward the camera so the target model cannot hide it.
            Vector3f right = context.camera().getRotation().transform(new Vector3f(1, 0, 0));
            Vector3f up = context.camera().getRotation().transform(new Vector3f(0, 1, 0));
            Vector3f towardCamera = context.camera().getRotation().transform(new Vector3f(0, 0, 1));
            double centerX = targetPos.x - camera.x + towardCamera.x * 0.16
                    + right.x * XakaVisualsClient.bodyMarkerOffsetX;
            double centerY = targetPos.y - camera.y + target.getHeight() * 0.53
                    + up.y * XakaVisualsClient.bodyMarkerOffsetY;
            double centerZ = targetPos.z - camera.z + towardCamera.z * 0.16
                    + right.z * XakaVisualsClient.bodyMarkerOffsetX
                    + towardCamera.z * XakaVisualsClient.bodyMarkerOffsetZ;
            // Larger marker with three closely spaced outlines to make the ring visibly bolder.
            float radius = 0.42f + XakaVisualsClient.bodyMarkerSize * 0.045f;
            var out = consumers.getBuffer(net.minecraft.client.render.RenderLayer.getLines());
            for (int layer = -1; layer <= 1; layer++) {
                float layerRadius = radius + layer * 0.018f;
                if (XakaVisualsClient.bodyMarkerSquare) {
                    drawBillboardSquare(out, centerX, centerY, centerZ, right, up, layerRadius, color);
                } else {
                    drawBillboardCircle(out, centerX, centerY, centerZ, right, up, layerRadius, color, 64);
                }
            }
        }
    }


    private static void drawBillboardCircle(net.minecraft.client.render.VertexConsumer out,
                                            double x, double y, double z, Vector3f right, Vector3f up,
                                            float radius, int color, int segments) {
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i + 1) / segments;
            double x0 = x + (right.x * Math.cos(a0) + up.x * Math.sin(a0)) * radius;
            double y0 = y + (right.y * Math.cos(a0) + up.y * Math.sin(a0)) * radius;
            double z0 = z + (right.z * Math.cos(a0) + up.z * Math.sin(a0)) * radius;
            double x1 = x + (right.x * Math.cos(a1) + up.x * Math.sin(a1)) * radius;
            double y1 = y + (right.y * Math.cos(a1) + up.y * Math.sin(a1)) * radius;
            double z1 = z + (right.z * Math.cos(a1) + up.z * Math.sin(a1)) * radius;
            line(out, x0, y0, z0, x1, y1, z1, color);
        }
    }

    private static void drawBillboardSquare(net.minecraft.client.render.VertexConsumer out,
                                            double x, double y, double z, Vector3f right, Vector3f up,
                                            float radius, int color) {
        double[][] corners = {{-1,-1},{1,-1},{1,1},{-1,1},{-1,-1}};
        for (int i = 0; i < 4; i++) {
            double[] a = corners[i], b = corners[i + 1];
            double x1 = x + (right.x * a[0] + up.x * a[1]) * radius;
            double y1 = y + (right.y * a[0] + up.y * a[1]) * radius;
            double z1 = z + (right.z * a[0] + up.z * a[1]) * radius;
            double x2 = x + (right.x * b[0] + up.x * b[1]) * radius;
            double y2 = y + (right.y * b[0] + up.y * b[1]) * radius;
            double z2 = z + (right.z * b[0] + up.z * b[1]) * radius;
            line(out, x1, y1, z1, x2, y2, z2, color);
        }
    }

    private static void drawCircle(net.minecraft.client.render.VertexConsumer out, double x, double y0, double z,
                                   float radius, float y, int color, int segments) {
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i + 1) / segments;
            line(out, x + Math.cos(a0) * radius, y0 + y, z + Math.sin(a0) * radius,
                    x + Math.cos(a1) * radius, y0 + y, z + Math.sin(a1) * radius, color);
        }
    }

    private static void drawSquare(net.minecraft.client.render.VertexConsumer out, double x, double y0, double z,
                                   float radius, float y, int color) {
        line(out, x-radius,y0+y,z-radius, x+radius,y0+y,z-radius,color);
        line(out, x+radius,y0+y,z-radius, x+radius,y0+y,z+radius,color);
        line(out, x+radius,y0+y,z+radius, x-radius,y0+y,z+radius,color);
        line(out, x-radius,y0+y,z+radius, x-radius,y0+y,z-radius,color);
    }

    private static void line(net.minecraft.client.render.VertexConsumer out,
                             double x1,double y1,double z1,double x2,double y2,double z2,int color) {
        int light = 0x00F000F0;
        out.vertex((float)x1,(float)y1,(float)z1,color,0,0,0,light,0,1,0);
        out.vertex((float)x2,(float)y2,(float)z2,color,0,0,0,light,0,1,0);
    }
}
