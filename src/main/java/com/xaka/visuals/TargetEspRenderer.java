package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
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
            Vec3d camera = context.camera().getPos();
            double yOffset = FeatureSettings.targetEspHeight;
            Box box = target.getBoundingBox().expand(0.025).offset(-camera.x, -camera.y + yOffset, -camera.z);
            double fraction = FeatureSettings.targetEspLength;
            double sx = (box.maxX - box.minX) * fraction;
            double sy = (box.maxY - box.minY) * fraction;
            double sz = (box.maxZ - box.minZ) * fraction;
            MatrixStack.Entry entry = context.matrixStack().peek();
            Matrix4f matrix = entry.getPositionMatrix();
            VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
            for (int ix = 0; ix < 2; ix++) for (int iy = 0; iy < 2; iy++) for (int iz = 0; iz < 2; iz++) {
                double x = ix == 0 ? box.minX : box.maxX, y = iy == 0 ? box.minY : box.maxY;
                double z = iz == 0 ? box.minZ : box.maxZ;
                double dx = ix == 0 ? sx : -sx, dy = iy == 0 ? sy : -sy, dz = iz == 0 ? sz : -sz;
                line(lines, matrix, entry, x, y, z, x + dx, y, z, red, green, blue);
                line(lines, matrix, entry, x, y, z, x, y + dy, z, red, green, blue);
                line(lines, matrix, entry, x, y, z, x, y, z + dz, red, green, blue);
            }
        });
    }

    private static void line(VertexConsumer c, Matrix4f m, MatrixStack.Entry e,
                             double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue) {
        float nx=(float)(x2-x1), ny=(float)(y2-y1), nz=(float)(z2-z1);
        float length=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);
        if(length>0){nx/=length;ny/=length;nz/=length;}
        c.vertex(m,(float)x1,(float)y1,(float)z1).color(red,green,blue,ALPHA).normal(e,nx,ny,nz);
        c.vertex(m,(float)x2,(float)y2,(float)z2).color(RED,GREEN,BLUE,ALPHA).normal(e,nx,ny,nz);
    }
}
