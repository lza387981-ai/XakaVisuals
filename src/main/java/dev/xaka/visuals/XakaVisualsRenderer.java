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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Local-only cosmetics: a thick halo, camera-relative companion and crosshair player marker. */
public final class XakaVisualsRenderer {
    private static final List<JumpWave> JUMP_WAVES = new ArrayList<>();
    private static final List<JumpCubeParticle> JUMP_CUBES = new ArrayList<>();

    private XakaVisualsRenderer() {}

    public static void spawnJumpEffect(double x, double y, double z) {
        if (!XakaVisualsClient.jumpEffect) return;
        if (JUMP_WAVES.size() >= 12) JUMP_WAVES.remove(0);
        JUMP_WAVES.add(new JumpWave(x, y, z, System.nanoTime(),
                XakaVisualsClient.jumpEffectRadius, XakaVisualsClient.jumpEffectDuration));
    }

    public static void spawnJumpCubes(double x, double y, double z) {
        if (!XakaVisualsClient.jumpCubes) return;
        int count = Math.max(1, Math.min(10, XakaVisualsClient.jumpCubeCount));
        for (int i = 0; i < count && JUMP_CUBES.size() < 80; i++) {
            double angle = Math.random() * Math.PI * 2.0;
            double distance = 0.22 + Math.random() * 0.52;
            JUMP_CUBES.add(new JumpCubeParticle(x + Math.cos(angle) * distance, y + 0.12, z + Math.sin(angle) * distance,
                    (Math.random() - 0.5) * 0.52, 0.62 + Math.random() * 0.72, (Math.random() - 0.5) * 0.52,
                    XakaVisualsClient.jumpCubeSize / 100.0f, System.nanoTime()));
        }
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(XakaVisualsRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.world == null || context.matrixStack() == null || context.consumers() == null) {
            JUMP_WAVES.clear();
            JUMP_CUBES.clear();
            return;
        }
        if (client.options.hudHidden) return;
        if (!XakaVisualsClient.jumpEffect) JUMP_WAVES.clear();
        if (!XakaVisualsClient.jumpCubes) JUMP_CUBES.clear();

        boolean halo = XakaVisualsClient.haloEnabled;
        boolean marker = XakaVisualsClient.bodyMarkerEnabled;
        boolean pet = XakaVisualsClient.petEnabled;
        boolean cape = XakaVisualsClient.wingsEnabled;
        boolean hat = XakaVisualsClient.hatEnabled;
        boolean jumpWave = !JUMP_WAVES.isEmpty();
        boolean jumpCubes = !JUMP_CUBES.isEmpty();
        if (!halo && !marker && !pet && !cape && !hat && !jumpWave && !jumpCubes) return;

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

        if (jumpWave) renderJumpWaves(consumers, camera);
        if (jumpCubes) renderJumpCubes(consumers, camera);

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

        if (cape || hat) {
            XakaCosmeticsRenderer.render(consumers, bx, by, bz, bodyYaw,
                    player.headYaw, cape ? Math.floorMod(XakaVisualsClient.wingStyle, 3) + 1 : 0,
                    hat ? Math.floorMod(XakaVisualsClient.hatStyle, 6) + 1 : 0,
                    client.world.getTime() + tickDelta);
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
            // Lightweight camera-facing Target ESP at the target's torso.
            // Shape selection is local and uses line primitives only (no texture or shader).
            float frameSize = 0.78f + XakaVisualsClient.markerSize * 0.015f;
            var markerLines = consumers.getBuffer(net.minecraft.client.render.RenderLayer.getLines());
            if (XakaVisualsClient.markerSquare) {
                drawBillboardSquare(markerLines, centerX, centerY, centerZ, right, up,
                        frameSize * 0.5f, targetFrameColor);
            } else {
                drawBillboardCircle(markerLines, centerX, centerY, centerZ, right, up,
                        frameSize * 0.5f, targetFrameColor, 32);
            }
        }
    }





    private static void renderJumpCubes(net.minecraft.client.render.VertexConsumerProvider consumers, Vec3d camera) {
        long now = System.nanoTime();
        var out = consumers.getBuffer(net.minecraft.client.render.RenderLayer.getLines());
        Iterator<JumpCubeParticle> iterator = JUMP_CUBES.iterator();
        while (iterator.hasNext()) {
            JumpCubeParticle cube = iterator.next();
            float age = (now - cube.startedAt) / 1_000_000.0f, progress = age / 1150.0f;
            if (progress >= 1.0f) { iterator.remove(); continue; }
            double seconds = age / 1000.0;
            double cx = cube.x + cube.vx * seconds - camera.x;
            double cy = cube.y + cube.vy * seconds - 0.62 * seconds * seconds - camera.y;
            double cz = cube.z + cube.vz * seconds - camera.z;
            float half = cube.size * (0.72f + progress * 0.28f);
            float yaw = (float)(seconds * 2.8 + cube.rotation), pitch = (float)(seconds * 2.1 + cube.rotation * 0.7);
            double[][] vertices = new double[8][3];
            for (int i = 0; i < 8; i++) {
                double px = (i & 1) == 0 ? -half : half, py = (i & 2) == 0 ? -half : half, pz = (i & 4) == 0 ? -half : half;
                double x1 = px * Math.cos(yaw) - pz * Math.sin(yaw), z1 = px * Math.sin(yaw) + pz * Math.cos(yaw);
                double y2 = py * Math.cos(pitch) - z1 * Math.sin(pitch), z2 = py * Math.sin(pitch) + z1 * Math.cos(pitch);
                vertices[i][0] = cx + x1; vertices[i][1] = cy + y2; vertices[i][2] = cz + z2;
            }
            int alpha = Math.max(0, Math.min(230, Math.round(230.0f * (1.0f - progress))));
            int color = withAlpha(XakaVisualsClient.accent(), alpha);
            int[][] edges = {{0,1},{0,2},{0,4},{1,3},{1,5},{2,3},{2,6},{3,7},{4,5},{4,6},{5,7},{6,7}};
            for (int[] edge : edges) { double[] a = vertices[edge[0]], b = vertices[edge[1]]; line(out,a[0],a[1],a[2],b[0],b[1],b[2],color); }
        }
    }

    private static final class JumpCubeParticle {
        private final double x,y,z,vx,vy,vz; private final float size,rotation; private final long startedAt;
        private JumpCubeParticle(double x,double y,double z,double vx,double vy,double vz,float size,long startedAt) {
            this.x=x; this.y=y; this.z=z; this.vx=vx; this.vy=vy; this.vz=vz;
            this.size=Math.max(0.05f,Math.min(0.40f,size)); this.startedAt=startedAt;
            this.rotation=(float)(Math.random()*Math.PI*2.0);
        }
    }

    private static void renderJumpWaves(net.minecraft.client.render.VertexConsumerProvider consumers, Vec3d camera) {
        long now = System.nanoTime();
        var out = consumers.getBuffer(net.minecraft.client.render.RenderLayer.getLines());
        Iterator<JumpWave> iterator = JUMP_WAVES.iterator();
        while (iterator.hasNext()) {
            JumpWave wave = iterator.next();
            float progress = (now - wave.startedAt) / 1_000_000.0f / wave.durationMs;
            if (progress >= 1.0f) {
                iterator.remove();
                continue;
            }
            float radius = 0.22f + (wave.radius - 0.22f) * progress;
            int alpha = Math.max(0, Math.min(220, Math.round(220.0f * (1.0f - progress))));
            int color = withAlpha(XakaVisualsClient.accent(), alpha);
            drawCircle(out, wave.x - camera.x, wave.y - camera.y, wave.z - camera.z,
                    radius, progress * 0.12f, color, 64);

            float delayed = progress - 0.22f;
            if (delayed >= 0.0f && delayed < 1.0f) {
                float secondRadius = 0.22f + (wave.radius - 0.22f) * delayed;
                int secondAlpha = Math.max(0, Math.min(180, Math.round(180.0f * (1.0f - delayed))));
                drawCircle(out, wave.x - camera.x, wave.y - camera.y, wave.z - camera.z,
                        secondRadius, 0.08f + delayed * 0.12f,
                        withAlpha(0xFFFFFFFF, secondAlpha), 64);
            }
        }
    }

    private static final class JumpWave {
        private final double x, y, z;
        private final long startedAt;
        private final float radius;
        private final int durationMs;

        private JumpWave(double x, double y, double z, long startedAt, float radius, int durationMs) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.startedAt = startedAt;
            this.radius = Math.max(1.0f, radius);
            this.durationMs = Math.max(100, durationMs);
        }
    }

    private static void drawWingLayer(net.minecraft.client.render.VertexConsumer out,
            double rx,double ry,double rz, double tx,double ty,double tz,
            double ox,double oy,double oz, double ix,double iy,double iz,
            int color, boolean mirror, double offsetX, double offsetZ, double scale) {
        double stx = rx + (tx-rx)*scale, sty = ry + (ty-ry)*scale, stz = rz + (tz-rz)*scale;
        double sox = rx + (ox-rx)*scale, soy = ry + (oy-ry)*scale, soz = rz + (oz-rz)*scale;
        double six = rx + (ix-rx)*scale, siy = ry + (iy-ry)*scale, siz = rz + (iz-rz)*scale;
        drawWingMesh(out, rx+offsetX,ry,rz+offsetZ, stx+offsetX,sty,stz+offsetZ,
                sox+offsetX,soy,soz+offsetZ, six+offsetX,siy,siz+offsetZ, color, mirror);
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
        float nxf=(float)nx, nyf=(float)ny, nzf=(float)nz;
        float u0=mirror?1.0f:0.0f, u1=mirror?0.0f:1.0f;
        // Front and back faces keep the decorative mesh visible from either camera angle.
        wingVertex(out,rx,ry,rz,color,u0,1,nxf,nyf,nzf);
        wingVertex(out,tx,ty,tz,color,u1,0,nxf,nyf,nzf);
        wingVertex(out,ox,oy,oz,color,u1,1,nxf,nyf,nzf);
        wingVertex(out,ix,iy,iz,color,u0,0,nxf,nyf,nzf);
        wingVertex(out,ox,oy,oz,color,u1,1,-nxf,-nyf,-nzf);
        wingVertex(out,tx,ty,tz,color,u1,0,-nxf,-nyf,-nzf);
        wingVertex(out,rx,ry,rz,color,u0,1,-nxf,-nyf,-nzf);
        wingVertex(out,ix,iy,iz,color,u0,0,-nxf,-nyf,-nzf);
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
