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
            // Several tightly spaced rings create a clearly visible, thick luminous band.
            for (int band = -3; band <= 3; band++) {
                float bandRadius = radius + band * 0.012f;
                drawCircle(consumers.getBuffer(net.minecraft.client.render.RenderLayer.getLines()),
                        bx, by, bz, bandRadius, y, color, 64);
            }
        }

        if (pet) {
            // Camera-relative left: the companion stays on the screen's left as the view turns.
            Vector3f left = context.camera().getRotation().transform(new Vector3f(-1, 0, 0));
            left.y = 0;
            if (left.lengthSquared() > 0.0001f) left.normalize();
            double bob = Math.sin(client.world.getTime() * 0.12) * 0.045;
            double px = playerPos.x + left.x * 0.78;
            double py = playerPos.y + 0.55 + bob;
            double pz = playerPos.z + left.z * 0.78;
            Entity companion = XakaVisualsClient.petWither
                    ? new WitherEntity(EntityType.WITHER, client.world)
                    : new CreeperEntity(EntityType.CREEPER, client.world);
            companion.setPosition(px, py, pz);
            companion.setYaw(player.getYaw());
            matrices.push();
            matrices.translate(px - camera.x, py - camera.y, pz - camera.z);
            float scale = XakaVisualsClient.petSize <= 0 ? 0.32f
                    : Math.min(0.62f, 0.25f + XakaVisualsClient.petSize * 0.035f);
            matrices.scale(scale, scale, scale);
            EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
            dispatcher.render(companion, 0.0, 0.0, 0.0, 0.0f,
                    context.tickCounter().getTickDelta(false), matrices, consumers, 0x00F000F0);
            matrices.pop();
        }

        if (marker && client.targetedEntity instanceof PlayerEntity target && target != player) {
            Vec3d targetPos = target.getLerpedPos(context.tickCounter().getTickDelta(false));
            double tx = targetPos.x - camera.x;
            double ty = targetPos.y - camera.y;
            double tz = targetPos.z - camera.z;
            float radius = 0.30f + XakaVisualsClient.bodyMarkerSize * 0.035f;
            float y = target.getHeight() * 0.53f + XakaVisualsClient.bodyMarkerOffsetY;
            var out = consumers.getBuffer(net.minecraft.client.render.RenderLayer.getLines());
            if (XakaVisualsClient.bodyMarkerSquare) drawSquare(out, tx, ty, tz, radius, y, color);
            else drawCircle(out, tx, ty, tz, radius, y, color, 48);
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
