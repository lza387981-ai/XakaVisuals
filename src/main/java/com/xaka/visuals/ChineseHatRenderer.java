package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class ChineseHatRenderer {
    private static final int SEGMENTS = 48;
    private ChineseHatRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            Feature feature = FeatureRegistry.find("Chinese Hat");
            PlayerEntity player = client.player;
            if (feature == null || !feature.enabled() || player == null || client.world == null) return;
            Vec3d camera = context.camera().getPos(), position = player.getPos();
            float x=(float)(position.x-camera.x), y=(float)(position.y+player.getHeight()+0.12-camera.y), z=(float)(position.z-camera.z);
            float scale=(float)FeatureSettings.hatScale;
            var matrices=context.matrixStack();
            matrices.push();
            matrices.translate(x,y,z);
            matrices.scale(scale,scale,scale);
            Matrix4f matrix=matrices.peek().getPositionMatrix();
            MatrixStack.Entry entry=matrices.peek();
            VertexConsumer vertices=context.consumers().getBuffer(RenderLayer.getDebugQuads());
            float brimRadius=0.78f, brimInner=0.10f, brimY=0.015f;
            float coneHeight=(float)FeatureSettings.hatHeight, coneRadius=0.43f;
            int alpha=FeatureSettings.hatAlpha;
            for(int i=0;i<SEGMENTS;i++){
                float a0=(float)(Math.PI*2*i/SEGMENTS),a1=(float)(Math.PI*2*(i+1)/SEGMENTS);
                float x0=MathHelper.cos(a0),z0=MathHelper.sin(a0),x1=MathHelper.cos(a1),z1=MathHelper.sin(a1);
                vertex(vertices,matrix,entry,x0*brimRadius,brimY,z0*brimRadius,0,1,0,alpha);
                vertex(vertices,matrix,entry,x1*brimRadius,brimY,z1*brimRadius,0,1,0,alpha);
                vertex(vertices,matrix,entry,x1*brimInner,brimY,z1*brimInner,0,1,0,alpha);
                vertex(vertices,matrix,entry,x0*brimInner,brimY,z0*brimInner,0,1,0,alpha);
            }
            for(int i=0;i<SEGMENTS;i++){
                float a0=(float)(Math.PI*2*i/SEGMENTS),a1=(float)(Math.PI*2*(i+1)/SEGMENTS);
                float x0=MathHelper.cos(a0)*coneRadius,z0=MathHelper.sin(a0)*coneRadius;
                float x1=MathHelper.cos(a1)*coneRadius,z1=MathHelper.sin(a1)*coneRadius;
                // Debug quads consume four vertices per face; duplicate the apex
                // to form a correctly grouped triangle for each cone segment.
                vertex(vertices,matrix,entry,x0,0.025f,z0,x0,0.35f,z0,alpha);
                vertex(vertices,matrix,entry,x1,0.025f,z1,x1,0.35f,z1,alpha);
                vertex(vertices,matrix,entry,0,coneHeight,0,0,1,0,alpha);
                vertex(vertices,matrix,entry,0,coneHeight,0,0,1,0,alpha);
            }
            matrices.pop();
        });
    }

    private static void vertex(VertexConsumer c,Matrix4f p,MatrixStack.Entry n,float x,float y,float z,
                               float nx,float ny,float nz,int alpha){
        c.vertex(p,x,y,z).color(90,235,145,alpha).normal(n,nx,ny,nz);
    }
}
