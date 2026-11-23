package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import java.util.Locale;

public final class TargetInfoHud {
    private TargetInfoHud() {}
    public static void register(){HudRenderCallback.EVENT.register((context,tickCounter)->render(context));}
    private static void render(DrawContext c){
        Feature f=FeatureRegistry.find("Target Info");
        if(f==null||!f.enabled())return;
        MinecraftClient client=MinecraftClient.getInstance();
        if(client.player==null||client.world==null||client.currentScreen!=null)return;
        HitResult hit=client.crosshairTarget;
        if(!(hit instanceof EntityHitResult entityHit))return;
        Entity entity=entityHit.getEntity();
        if(!(entity instanceof LivingEntity target)||target==client.player||!target.isAlive())return;
        float max=Math.max(1,target.getMaxHealth()),health=Math.max(0,Math.min(target.getHealth(),max)),ratio=health/max;
        double scale=FeatureSettings.hudScale;
        int w=(int)Math.round(190*scale),h=(int)Math.round(60*scale);
        int x=HudDragController.x("target",client.getWindow().getScaledWidth()-w-12);
        int y=HudDragController.y("target",client.getWindow().getScaledHeight()-h-(int)Math.round(42*scale));
        int alpha=FeatureSettings.hudAlpha;
        c.fill(x,y,x+w,y+h,(alpha<<24)|0x00101010);
        c.fill(x,y,x+w,y+Math.max(1,(int)(2*scale)),0xFFE18ACB);
        c.drawText(client.textRenderer,target.getDisplayName(),x+(int)(8*scale),y+(int)(7*scale),0xFFFFFFFF,true);
        c.drawText(client.textRenderer,String.format(Locale.ROOT,"%.1f / %.1f HP",health,max),x+(int)(8*scale),y+(int)(20*scale),0xFFE6E6E6,false);
        c.drawText(client.textRenderer,"БРОНЯ ВРАГА: "+target.getArmor(),x+(int)(8*scale),y+(int)(32*scale),0xFFD8B5D0,false);
        int bx=x+(int)(8*scale),by=y+(int)(46*scale),bw=w-(int)(16*scale),bh=Math.max(3,(int)(6*scale));
        c.fill(bx,by,bx+bw,by+bh,0xFF454545);
        int color=ratio>0.6f?0xFF63D471:ratio>0.3f?0xFFE6C45A:0xFFE45B67;
        c.fill(bx,by,bx+Math.round(bw*ratio),by+bh,color);
    }
}
