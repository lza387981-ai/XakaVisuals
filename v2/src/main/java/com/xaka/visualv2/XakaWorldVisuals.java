package com.xaka.visualv2;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class XakaWorldVisuals {
    private static final int ACCENT = 0xFF39BDF8;

    private XakaWorldVisuals() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(XakaWorldVisuals::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.world == null || context.matrixStack() == null
                || context.consumers() == null || client.options.hudHidden) return;

        boolean creeperPet = XakaClickScreen.isEnabled("Mini Creeper Pet");
        boolean witherPet = XakaClickScreen.isEnabled("Mini Wither Pet");
        boolean halo = XakaClickScreen.isEnabled("Entity Halo");
        boolean marker = XakaClickScreen.isEnabled("Target Marker");
        if (!creeperPet && !witherPet && !halo && !marker) return;

        float tickDelta = context.tickCounter().getTickDelta(false);
        Vec3d camera = context.camera().getPos();
        Vec3d playerPos = player.getLerpedPos(tickDelta);
        float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);

        if (creeperPet) renderPet(context, client, player, playerPos, camera, bodyYaw,
                new CreeperEntity(EntityType.CREEPER, client.world), 0.88, 0.48f);
        if (witherPet) renderPet(context, client, player, playerPos, camera, bodyYaw,
                new WitherEntity(EntityType.WITHER, client.world), 1.18, 0.34f);

        Entity target = client.crosshairTarget instanceof EntityHitResult hit ? hit.getEntity() : null;
        if (target instanceof LivingEntity living && target != player && target.isAlive()) {
            Vec3d targetPos = target.getLerpedPos(tickDelta);
            if (halo) {
                VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
                float radius = Math.max(0.42f, Math.min(0.95f, target.getWidth() * 0.42f + XakaVisualV2Client.haloSize * 0.025f));
                double centerY = targetPos.y + Math.min(target.getHeight() * 0.55, 1.0);
                drawRing(lines, targetPos.x - camera.x, centerY - camera.y,
                        targetPos.z - camera.z, radius, ACCENT, 64);
                drawRing(lines, targetPos.x - camera.x, centerY - camera.y + 0.035,
                        targetPos.z - camera.z, radius * 0.94f, 0x9939BDF8, 64);
            }
            if (marker) {
                VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
                double markerY = targetPos.y - camera.y + 0.04;
                drawRing(lines, targetPos.x - camera.x, markerY,
                        targetPos.z - camera.z, Math.max(0.48f, target.getWidth() * 0.40f + XakaVisualV2Client.markerSize * 0.025f),
                        0xFFFFD166, 4);
            }
        }
    }

    private static void renderPet(WorldRenderContext context, MinecraftClient client,
                                  PlayerEntity player, Vec3d playerPos, Vec3d camera,
                                  float bodyYaw, Entity pet, double distance, float scale) {
        float yaw = (float) Math.toRadians(bodyYaw);
        double sideX = Math.cos(yaw);
        double sideZ = Math.sin(yaw);
        double bob = Math.sin(client.world.getTime() * 0.12) * 0.045;
        double px = playerPos.x + sideX * distance;
        double py = playerPos.y + 0.58 + bob;
        double pz = playerPos.z + sideZ * distance;

        pet.setPosition(px, py, pz);
        pet.setYaw(0.0f);
        pet.setBodyYaw(0.0f);
        pet.setHeadYaw(0.0f);
        pet.setPitch(0.0f);

        var matrices = context.matrixStack();
        matrices.push();
        matrices.translate(px - camera.x, py - camera.y, pz - camera.z);
        matrices.scale(scale, scale, scale);
        client.getEntityRenderDispatcher().render(pet, 0.0, 0.0, 0.0, 0.0f,
                matrices, context.consumers(), 0x00F000F0);
        matrices.pop();
    }

    private static void drawRing(VertexConsumer out, double x, double y, double z,
                                 float radius, int color, int segments) {
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i + 1) / segments;
            line(out, x + Math.cos(a0) * radius, y, z + Math.sin(a0) * radius,
                    x + Math.cos(a1) * radius, y, z + Math.sin(a1) * radius, color);
        }
    }

    private static void line(VertexConsumer out, double x1, double y1, double z1,
                             double x2, double y2, double z2, int color) {
        int light = 0x00F000F0;
        out.vertex((float) x1, (float) y1, (float) z1, color, 0, 0, 0, light, 0, 1, 0);
        out.vertex((float) x2, (float) y2, (float) z2, color, 0, 0, 0, light, 0, 1, 0);
    }
}
