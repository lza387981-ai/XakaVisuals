package dev.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Identifier;

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
        boolean wings = XakaVisualsClient.wingsEnabled;
        if (!halo && !marker && !pet && !wings) return;

        Vec3d camera = context.camera().getPos();
        Vec3d playerPos = player.getLerpedPos(context.tickCounter().getTickDelta(false));
        double bx = playerPos.x - camera.x;
        double by = playerPos.y - camera.y;
        double bz = playerPos.z - camera.z;
        var matrices = context.matrixStack();
        var consumers = context.consumers();
        int color = XakaVisualsClient.accent();
        int targetFrameColor = XakaVisualsClient.targetFrameColor();
        float tickDelta = context.tickCounter().getTickDelta(false);
        float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);

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
            // Interpolate across the shortest angular path to avoid 360-degree snapping.
            float playerYawRad = (float) Math.toRadians(bodyYaw);
            Vector3f left = new Vector3f((float) Math.cos(playerYawRad), 0.0f,
                    (float) Math.sin(playerYawRad));
            double bob = Math.sin(client.world.getTime() * 0.12) * 0.045;
            int petType = Math.floorMod(XakaVisualsClient.petType, 4);
            // Keep the larger Warden and Wither a little farther from the player.
            double petDistance = switch (petType) {
                case 1, 2 -> 1.22;
                default -> 0.92;
            };
            double px = playerPos.x + left.x * petDistance;
            double petBob = petType == 3
                    ? Math.sin(client.world.getTime() * 0.16) * 0.075
                    : bob;
            double py = playerPos.y + 0.55 + petBob;
            double pz = playerPos.z + left.z * petDistance;
            Entity companion = switch (petType) {
                case 1 -> new WitherEntity(EntityType.WITHER, client.world);
                case 2 -> new WardenEntity(EntityType.WARDEN, client.world);
                case 3 -> new AllayEntity(EntityType.ALLAY, client.world);
                default -> new CreeperEntity(EntityType.CREEPER, client.world);
            };
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
            if (petType == 3) {
                // Give the Allay a gentle hovering tilt while it follows the player.
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(
                        (float) Math.sin(client.world.getTime() * 0.10) * 5.0f));
            }
            // Slightly larger companion proportions so the pet stays clearly visible.
            float scale = XakaVisualsClient.petSize <= 0 ? 0.38f
                    : Math.min(0.82f, 0.30f + XakaVisualsClient.petSize * 0.05f);
            if (petType == 2) scale *= 0.82f;
            if (petType == 3) scale *= 1.22f;
            matrices.scale(scale, scale, scale);
            EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
            dispatcher.render(companion, 0.0, 0.0, 0.0, 0.0f,
                    matrices, consumers, 0x00F000F0);
            matrices.pop();
        }

        if (wings) {
            // Filled, feathered wing meshes: one textured membrane per side, not floating line segments.
            float yaw = (float) Math.toRadians(bodyYaw);
            float sideX = (float) Math.cos(yaw);
            float sideZ = (float) Math.sin(yaw);
            float backX = (float) Math.sin(yaw);
            float backZ = (float) -Math.cos(yaw);
            float phase = (float) (client.world.getTime() + tickDelta)
                    * 0.22f * XakaVisualsClient.wingSpeed;
            float flap = (float) Math.sin(phase) * 0.24f;
            float span = XakaVisualsClient.wingStyle == 2 ? 0.92f
                    : XakaVisualsClient.wingStyle == 1 ? 0.82f : 0.74f;
            Identifier wingTexture = Identifier.of("xakavisuals", "textures/entity/cosmetic_wing.png");
            var wingLayer = net.minecraft.client.render.RenderLayer.getEntityTranslucent(wingTexture);
            var wingMesh = consumers.getBuffer(wingLayer);
            var wingOutline = consumers.getBuffer(net.minecraft.client.render.RenderLayer.getLines());
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
                double innerZ = rootZ + sideZ * span * 0.24 * side + backZ * 0.40;
                drawWingMesh(wingMesh, rootX,rootY,rootZ, tipX,tipY,tipZ,
                        outerX,outerY,outerZ, innerX,innerY,innerZ,
                        color, side == 1);
                int outlineColor = (color & 0x00FFFFFF) | 0xD0000000;
                line(wingOutline, rootX,rootY,rootZ, tipX,tipY,tipZ, outlineColor);
                line(wingOutline, tipX,tipY,tipZ, outerX,outerY,outerZ, outlineColor);
                line(wingOutline, outerX,outerY,outerZ, innerX,innerY,innerZ, outlineColor);
                line(wingOutline, innerX,innerY,innerZ, rootX,rootY,rootZ, outlineColor);
            }
        }

        if (marker && client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit
                && hit.getEntity() instanceof PlayerEntity target && target != player) {
            Vec3d targetPos = target.getLerpedPos(context.tickCounter().getTickDelta(false));
            // Camera-facing ring, slightly toward the camera so the target model cannot hide it.
            Vector3f right = context.camera().getRotation().transform(new Vector3f(1, 0, 0));
            Vector3f up = context.camera().getRotation().transform(new Vector3f(0, 1, 0));
            Vector3f towardCamera = context.camera().getRotation().transform(new Vector3f(0, 0, 1));
            // Raise the whole marker slightly along the camera's up direction,
            // without changing its width or height.
            double markerLift = 0.12;
            // Keep vertical placement in world Y, not camera-up: pitching the camera
            // must not make the frame jump above/below the target.
            double verticalOffset = target.getHeight() * 0.53
                    + XakaVisualsClient.bodyMarkerOffsetY + markerLift;
            double centerX = targetPos.x - camera.x + towardCamera.x * 0.48
                    + right.x * XakaVisualsClient.bodyMarkerOffsetX
                    + towardCamera.x * XakaVisualsClient.bodyMarkerOffsetZ;
            double centerY = targetPos.y - camera.y + verticalOffset;
            double centerZ = targetPos.z - camera.z + towardCamera.z * 0.48
                    + right.z * XakaVisualsClient.bodyMarkerOffsetX
                    + towardCamera.z * XakaVisualsClient.bodyMarkerOffsetZ;
            // Render the PNG frame as a camera-facing textured quad; tint changes its color.
            float frameSize = 0.78f + XakaVisualsClient.markerSize * 0.015f;
            float frameWidth = frameSize;
            float frameHeight = frameSize;
            // Use a monotonic high-resolution clock so rotation never pauses at tick boundaries.
            float rotation = (float) (System.nanoTime() * -0.00000000045);
            drawTexturedTargetFrame(consumers, centerX, centerY, centerZ, right, up,
                    frameWidth, frameHeight, rotation, targetFrameColor);
        }
    }





    private static void drawWingMesh(net.minecraft.client.render.VertexConsumer out,
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
        // Front and back faces keep the decorative mesh visible from either camera angle.
        wingVertex(out,rx,ry,rz,color,u0,1,nx,ny,nz);
        wingVertex(out,tx,ty,tz,color,u1,0,nx,ny,nz);
        wingVertex(out,ox,oy,oz,color,u1,1,nx,ny,nz);
        wingVertex(out,ix,iy,iz,color,u0,0,nx,ny,nz);
        wingVertex(out,ox,oy,oz,color,u1,1,-nx,-ny,-nz);
        wingVertex(out,tx,ty,tz,color,u1,0,-nx,-ny,-nz);
        wingVertex(out,rx,ry,rz,color,u0,1,-nx,-ny,-nz);
        wingVertex(out,ix,iy,iz,color,u0,0,-nx,-ny,-nz);
    }

    private static void wingVertex(net.minecraft.client.render.VertexConsumer out,
            double x,double y,double z,int color,float u,float v,float nx,float ny,float nz) {
        out.vertex((float)x,(float)y,(float)z,color,u,v,0,0x00F000F0,nx,ny,nz);
    }

    private static void drawTexturedTargetFrame(net.minecraft.client.render.VertexConsumerProvider consumers,
            double x, double y, double z, Vector3f right, Vector3f up,
            float width, float height, float rotation, int tint) {
        Identifier texture = Identifier.of("xakavisuals", "textures/gui/target_frame.png");
        var layer = net.minecraft.client.render.RenderLayer.getEntityTranslucent(texture);
        var out = consumers.getBuffer(layer);
        float c = (float) Math.cos(rotation), s = (float) Math.sin(rotation);
        float hw = width * 0.5f, hh = height * 0.5f;
        // Clockwise texture coordinates, camera-facing plane.
        // Front face.
        texturedVertex(out, x,y,z,right,up,-hw,-hh,c,s,tint,0,1);
        texturedVertex(out, x,y,z,right,up, hw,-hh,c,s,tint,1,1);
        texturedVertex(out, x,y,z,right,up, hw, hh,c,s,tint,1,0);
        texturedVertex(out, x,y,z,right,up,-hw,-hh,c,s,tint,0,1);
        texturedVertex(out, x,y,z,right,up, hw, hh,c,s,tint,1,0);
        texturedVertex(out, x,y,z,right,up,-hw, hh,c,s,tint,0,0);
        // Back face with reversed winding: prevents one triangle/face disappearing
        // on render layers or drivers that apply back-face culling.
        texturedVertex(out, x,y,z,right,up, hw, hh,c,s,tint,1,0);
        texturedVertex(out, x,y,z,right,up, hw,-hh,c,s,tint,1,1);
        texturedVertex(out, x,y,z,right,up,-hw,-hh,c,s,tint,0,1);
        texturedVertex(out, x,y,z,right,up,-hw, hh,c,s,tint,0,0);
        texturedVertex(out, x,y,z,right,up, hw, hh,c,s,tint,1,0);
        texturedVertex(out, x,y,z,right,up,-hw,-hh,c,s,tint,0,1);
    }

    private static void texturedVertex(net.minecraft.client.render.VertexConsumer out,
            double x,double y,double z,Vector3f right,Vector3f up,
            float px,float py,float c,float s,int color,float u,float v) {
        float rx = px*c - py*s, ry = px*s + py*c;
        float nx = -right.z*up.y + right.y*up.z;
        float ny = -right.x*up.z + right.z*up.x;
        float nz = -right.y*up.x + right.x*up.y;
        out.vertex((float)(x + right.x*rx + up.x*ry),
                (float)(y + right.y*rx + up.y*ry),
                (float)(z + right.z*rx + up.z*ry),
                color,u,v,0,0x00F000F0,nx,ny,nz);
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    private static void drawRotatingTargetFrame(net.minecraft.client.render.VertexConsumer out,
            double x, double y, double z, Vector3f right, Vector3f up,
            float radius, float rotation, int color) {
        float c = (float) Math.cos(rotation), s = (float) Math.sin(rotation);
        float arm = radius * 0.43f;
        float[][] corners = {{-1,-1},{1,-1},{1,1},{-1,1}};
        for (float[] corner : corners) {
            float cx = corner[0] * radius, cy = corner[1] * radius;
            float ix = cx - corner[0] * arm, iy = cy;
            float jx = cx, jy = cy - corner[1] * arm;
            drawFrameSegment(out, x, y, z, right, up, ix, iy, cx, cy, c, s, color);
            drawFrameSegment(out, x, y, z, right, up, cx, cy, jx, jy, c, s, color);
        }
    }

    private static void drawFrameSegment(net.minecraft.client.render.VertexConsumer out,
            double x, double y, double z, Vector3f right, Vector3f up,
            float ax, float ay, float bx, float by, float c, float s, int color) {
        float rax = ax * c - ay * s, ray = ax * s + ay * c;
        float rbx = bx * c - by * s, rby = bx * s + by * c;
        double x1 = x + right.x * rax + up.x * ray;
        double y1 = y + right.y * rax + up.y * ray;
        double z1 = z + right.z * rax + up.z * ray;
        double x2 = x + right.x * rbx + up.x * rby;
        double y2 = y + right.y * rbx + up.y * rby;
        double z2 = z + right.z * rbx + up.z * rby;
        line(out, x1, y1, z1, x2, y2, z2, color);
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
