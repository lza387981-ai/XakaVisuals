package dev.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

/** Renders synced cosmetics for other players. Local cosmetics remain handled by XakaVisualsRenderer. */
public final class XakaVisualsRemoteRenderer {
    private XakaVisualsRemoteRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(XakaVisualsRemoteRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || context.matrixStack() == null
                || context.consumers() == null || client.options.hudHidden) return;

        Vec3d camera = context.camera().getPos();
        float tickDelta = context.tickCounter().getTickDelta(false);
        var matrices = context.matrixStack();
        var consumers = context.consumers();

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;
            XakaVisualsSyncPayload state = XakaVisualsSyncClient.getState(player.getUuid());
            if (state == null || (!state.petEnabled() && !state.wingsEnabled())) continue;

            Vec3d pos = player.getLerpedPos(tickDelta);
            double bx = pos.x - camera.x;
            double by = pos.y - camera.y;
            double bz = pos.z - camera.z;
            float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);
            float yaw = (float) Math.toRadians(bodyYaw);

            if (state.petEnabled()) {
                int petType = Math.floorMod(state.petType(), 4);
                double sideX = Math.cos(yaw);
                double sideZ = Math.sin(yaw);
                double distance = (petType == 1 || petType == 2) ? 1.22 : 0.92;
                double px = pos.x + sideX * distance;
                double pz = pos.z + sideZ * distance;
                double bob = petType == 3
                        ? Math.sin(client.world.getTime() * 0.16) * 0.075
                        : Math.sin(client.world.getTime() * 0.12) * 0.045;
                double py = pos.y + 0.55 + bob;

                Entity companion = switch (petType) {
                    case 1 -> new WitherEntity(EntityType.WITHER, client.world);
                    case 2 -> new WardenEntity(EntityType.WARDEN, client.world);
                    case 3 -> new AllayEntity(EntityType.ALLAY, client.world);
                    default -> new CreeperEntity(EntityType.CREEPER, client.world);
                };
                companion.setPosition(px, py, pz);
                companion.setYaw(0.0f);
                companion.setBodyYaw(0.0f);
                companion.setHeadYaw(0.0f);
                companion.setPitch(0.0f);

                matrices.push();
                matrices.translate(px - camera.x, py - camera.y, pz - camera.z);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-bodyYaw));
                if (petType == 3) {
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(
                            (float) Math.sin(client.world.getTime() * 0.10) * 5.0f));
                }
                float scale = state.petSize() <= 0 ? 0.38f
                        : Math.min(0.82f, 0.30f + state.petSize() * 0.05f);
                if (petType == 2) scale *= 0.82f;
                if (petType == 3) scale *= 1.22f;
                matrices.scale(scale, scale, scale);
                EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
                dispatcher.render(companion, 0.0, 0.0, 0.0, 0.0f,
                        matrices, consumers, 0x00F000F0);
                matrices.pop();
            }

            if (state.wingsEnabled()) {
                float sideX = (float) Math.cos(yaw);
                float sideZ = (float) Math.sin(yaw);
                float backX = (float) Math.sin(yaw);
                float backZ = (float) -Math.cos(yaw);
                float phase = (float) (client.world.getTime() + tickDelta) * 0.22f * state.wingSpeed();
                float flap = (float) Math.sin(phase) * 0.24f;
                float span = state.wingStyle() == 2 ? 0.92f : state.wingStyle() == 1 ? 0.82f : 0.74f;
                var mesh = consumers.getBuffer(RenderLayer.getEntityTranslucent(
                        net.minecraft.util.Identifier.of("xakavisuals", "textures/entity/cosmetic_wing.png")));
                var outline = consumers.getBuffer(RenderLayer.getLines());
                for (int side : new int[]{-1, 1}) {
                    double rootX = bx - backX * 0.17;
                    double rootY = by + 1.02;
                    double rootZ = bz - backZ * 0.17;
                    double tipX = rootX + sideX * span * side + backX * 0.12;
                    double tipY = by + 1.28 + flap * side;
                    double tipZ = rootZ + sideZ * span * side + backZ * 0.12;
                    double outerX = rootX + sideX * span * 0.86 * side + backX * 0.48;
                    double outerY = by + 1.48 + flap * side * 0.70;
                    double outerZ = rootZ + sideZ * span * 0.86 * side + backZ * 0.48;
                    double innerX = rootX + sideX * span * 0.24 * side + backX * 0.40;
                    double innerY = by + 1.30 + flap * side * 0.30;
                    double innerZ = bz - backZ * 0.17 + sideZ * span * 0.24 * side + backZ * 0.40;
                    drawWingMesh(mesh, rootX,rootY,rootZ, tipX,tipY,tipZ,
                            outerX,outerY,outerZ, innerX,innerY,innerZ, color, side == 1);
                    int outlineColor = (color & 0x00FFFFFF) | 0xD0000000;
                    line(outline,rootX,rootY,rootZ,tipX,tipY,tipZ,outlineColor);
                    line(outline,tipX,tipY,tipZ,outerX,outerY,outerZ,outlineColor);
                    line(outline,outerX,outerY,outerZ,innerX,innerY,innerZ,outlineColor);
                    line(outline,innerX,innerY,innerZ,rootX,rootY,rootZ,outlineColor);
                }
            }
        }
    }


    private static void drawWingMesh(VertexConsumer out,
            double rx,double ry,double rz, double tx,double ty,double tz,
            double ox,double oy,double oz, double ix,double iy,double iz,
            int color, boolean mirror) {
        double ax=tx-rx, ay=ty-ry, az=tz-rz;
        double bx=ix-rx, by=iy-ry, bz=iz-rz;
        double nx=ay*bz-az*by, ny=az*bx-ax*bz, nz=ax*by-ay*bx;
        double len=Math.sqrt(nx*nx+ny*ny+nz*nz);
        if (len < 1.0e-6) len=1.0;
        nx/=len; ny/=len; nz/=len;
        float u0=mirror?1.0f:0.0f, u1=mirror?0.0f:1.0f;
        wingVertex(out,rx,ry,rz,color,u0,1,nx,ny,nz);
        wingVertex(out,tx,ty,tz,color,u1,0,nx,ny,nz);
        wingVertex(out,ox,oy,oz,color,u1,1,nx,ny,nz);
        wingVertex(out,ix,iy,iz,color,u0,0,nx,ny,nz);
        wingVertex(out,ox,oy,oz,color,u1,1,-nx,-ny,-nz);
        wingVertex(out,tx,ty,tz,color,u1,0,-nx,-ny,-nz);
        wingVertex(out,rx,ry,rz,color,u0,1,-nx,-ny,-nz);
        wingVertex(out,ix,iy,iz,color,u0,0,-nx,-ny,-nz);
    }

    private static void wingVertex(VertexConsumer out,double x,double y,double z,
            int color,float u,float v,float nx,float ny,float nz) {
        out.vertex((float)x,(float)y,(float)z,color,u,v,0,0x00F000F0,nx,ny,nz);
    }

    private static void line(VertexConsumer out, double x1, double y1, double z1,
            double x2, double y2, double z2, int color) {
        out.vertex((float) x1, (float) y1, (float) z1, color, 0, 0, 0, 0x00F000F0, 0, 1, 0);
        out.vertex((float) x2, (float) y2, (float) z2, color, 0, 0, 0, 0x00F000F0, 0, 1, 0);
    }
}
