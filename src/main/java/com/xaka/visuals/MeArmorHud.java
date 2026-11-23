package com.xaka.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public final class MeArmorHud {
    private MeArmorHud() {}
    public static void register(){HudRenderCallback.EVENT.register((context,tickCounter)->render(context));}
    private static void render(DrawContext c){
        Feature f=FeatureRegistry.find("Me Armor");
        MinecraftClient client=MinecraftClient.getInstance();
        if(f==null||!f.enabled()||client.player==null||client.currentScreen!=null)return;
        double scale=FeatureSettings.hudScale;
        int slot=(int)Math.round(32*scale),step=(int)Math.round(36*scale),w=step*4;
        int x=HudDragController.x("armor",client.getWindow().getScaledWidth()/2-w/2);
        int y=HudDragController.y("armor",client.getWindow().getScaledHeight()-slot-(int)Math.round(26*scale));
        int alpha=FeatureSettings.hudAlpha;
        c.fill(x-(int)(5*scale),y-(int)(19*scale),x+w-(int)(3*scale),y+slot+(int)(6*scale),(alpha<<24)|0x00101015);
        c.drawCenteredTextWithShadow(client.textRenderer,"МОЯ БРОНЯ",x+w/2,y-(int)(15*scale),0xFFE7A5D2);
        for(int i=0;i<4;i++){
            ItemStack stack=client.player.getInventory().armor.get(3-i);
            int sx=x+i*step;
            c.fill(sx,y,sx+slot,y+slot,0xFF252832);
            if(stack.isEmpty()){
                c.drawCenteredTextWithShadow(client.textRenderer,switch(i){case 0->"H";case 1->"C";case 2->"L";default->"B";},sx+slot/2,y+(int)(12*scale),0xFF686E7B);
                continue;
            }
            c.getMatrices().push();
            c.getMatrices().translate(sx+(slot-16*scale)/2,y+(slot-16*scale)/2,0);
            c.getMatrices().scale((float)scale,(float)scale,1);
            c.drawItem(stack,0,0);
            c.getMatrices().pop();
            if(!stack.isDamageable()){
                c.drawCenteredTextWithShadow(client.textRenderer,"—",sx+slot/2,y+(int)(22*scale),0xFFB9BEC9);
                continue;
            }
            int remaining=Math.max(0,stack.getMaxDamage()-stack.getDamage());
            int percent=Math.round(remaining*100.0f/stack.getMaxDamage());
            int color=percent>60?0xFF70D98B:percent>25?0xFFE6C66A:0xFFE66B75;
            c.drawCenteredTextWithShadow(client.textRenderer,percent+"%",sx+slot/2,y+(int)(22*scale),color);
            c.fill(sx+(int)(4*scale),y+(int)(29*scale),sx+(int)(28*scale),y+(int)(31*scale),0xFF494D57);
            c.fill(sx+(int)(4*scale),y+(int)(29*scale),sx+(int)((4+24*percent/100.0)*scale),y+(int)(31*scale),color);
        }
    }
}
