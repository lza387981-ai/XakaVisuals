package com.xaka.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.List;

public final class XakaMenuScreen extends Screen {
    private static final int PANEL_WIDTH=690, PANEL_HEIGHT=390, ACCENT=0xFFE58BCB;
    private Feature.Category selected=Feature.Category.RENDER;
    private int panelX,panelY;
    private String searchQuery="";
    private boolean searchFocused,settingsOpen;
    private Feature bindingFeature,settingsFeature;

    public XakaMenuScreen(){super(Text.literal("XakaVisuals V3"));}
    @Override protected void init(){panelX=(width-PANEL_WIDTH)/2;panelY=(height-PANEL_HEIGHT)/2;}

    private List<Feature> visibleFeatures(){
        return FeatureRegistry.in(selected).stream().filter(f->f.name().toLowerCase().contains(searchQuery.toLowerCase())).toList();
    }

    @Override public void render(DrawContext c,int mouseX,int mouseY,float delta){
        renderBackground(c,mouseX,mouseY,delta);
        c.fill(panelX,panelY,panelX+PANEL_WIDTH,panelY+PANEL_HEIGHT,0xFFE3E3E3);
        c.fill(panelX+2,panelY+2,panelX+PANEL_WIDTH-2,panelY+PANEL_HEIGHT-2,0xF20E1015);
        c.fill(panelX+3,panelY+3,panelX+PANEL_WIDTH-3,panelY+54,0xFF171920);
        c.fill(panelX+3,panelY+3,panelX+6,panelY+54,ACCENT);
        c.drawText(textRenderer,"XAKA",panelX+18,panelY+12,ACCENT,true);
        c.drawText(textRenderer,"VISUALS  /  V3",panelX+18,panelY+27,0xFFE6E7EC,true);
        c.drawText(textRenderer,"CLIENT EDITION",panelX+112,panelY+29,0xFF777C89,false);

        int tabWidth=77,tabsX=panelX+232;
        for(Feature.Category category:Feature.Category.values()){
            int x=tabsX+category.ordinal()*(tabWidth+5);boolean active=category==selected;
            c.fill(x,panelY+13,x+tabWidth,panelY+43,active?0xFFE9C4DE:0xFF292C35);
            if(active)c.fill(x,panelY+40,x+tabWidth,panelY+43,ACCENT);
            c.drawCenteredTextWithShadow(textRenderer,category.title(),x+tabWidth/2,panelY+24,active?0xFF25202A:0xFFD7D9E0);
        }
        int searchX=panelX+PANEL_WIDTH-158;
        c.fill(searchX,panelY+62,panelX+PANEL_WIDTH-42,panelY+85,searchFocused?0xFF34313B:0xFF20232B);
        c.drawText(textRenderer,searchQuery.isEmpty()?"Search modules...":searchQuery,searchX+7,panelY+70,searchQuery.isEmpty()?0xFF858996:0xFFF1EAF0,false);
        c.fill(panelX+PANEL_WIDTH-32,panelY+62,panelX+PANEL_WIDTH-12,panelY+85,0xFF3B252F);
        c.drawCenteredTextWithShadow(textRenderer,"×",panelX+PANEL_WIDTH-22,panelY+68,0xFFFF9DAF);
        c.drawText(textRenderer,selected.title().toUpperCase(),panelX+18,panelY+69,0xFFEDEEF2,true);
        c.drawText(textRenderer,visibleFeatures().size()+" MODULES",panelX+18,panelY+82,0xFF777D8B,false);

        List<Feature> features=visibleFeatures();
        for(int i=0;i<features.size();i++){
            Feature f=features.get(i);int x=panelX+18+(i%3)*220,y=panelY+103+(i/3)*61;
            boolean hover=mouseX>=x&&mouseX<=x+210&&mouseY>=y&&mouseY<=y+52;
            c.fill(x,y,x+210,y+52,hover?0xFF35313B:0xFF282B34);
            c.fill(x+1,y+1,x+209,y+51,0xFF1C1F27);
            c.fill(x,y,x+2,y+52,f.enabled()?ACCENT:0xFF555A66);
            c.drawText(textRenderer,f.name(),x+10,y+8,0xFFF0F1F5,true);
            c.drawText(textRenderer,f.enabled()?"ACTIVE":"INACTIVE",x+10,y+29,f.enabled()?0xFFE7A5D2:0xFF858A96,false);
            int tx=x+156;c.fill(tx,y+27,tx+44,y+44,f.enabled()?0xFF744765:0xFF353944);
            c.fill(f.enabled()?tx+29:tx+4,y+30,(f.enabled()?tx+29:tx+4)+11,y+41,f.enabled()?0xFFFFD9F0:0xFF9AA0AC);
            String bind=bindingFeature==f?"PRESS KEY":f.bindKey()<0?"BIND":InputUtil.fromKeyCode(f.bindKey(),0).getLocalizedText().getString();
            c.drawText(textRenderer,bind,x+10,y+43,0xFF858A96,false);
        }
        if(features.isEmpty())c.drawCenteredTextWithShadow(textRenderer,"No modules found",panelX+PANEL_WIDTH/2,panelY+190,0xFF9A9EAA);
        c.drawText(textRenderer,"LEFT CLICK: TOGGLE   •   RIGHT CLICK: SETTINGS   •   RIGHT SHIFT: CLOSE",panelX+18,panelY+PANEL_HEIGHT-23,0xFF858A96,false);
        c.drawText(textRenderer,"XAKAVISUALS",panelX+PANEL_WIDTH-105,panelY+PANEL_HEIGHT-23,0xFF777D8B,false);
        if(settingsOpen)renderSettings(c);
        if(bindingFeature!=null)c.drawCenteredTextWithShadow(textRenderer,"PRESS A KEY  •  ESC TO CANCEL",panelX+PANEL_WIDTH/2,panelY+PANEL_HEIGHT-42,0xFFFFD0E8);
        super.render(c,mouseX,mouseY,delta);
    }

    private String[] settingNames(){
        if(settingsFeature==null)return new String[0];
        return switch(settingsFeature.name()){
            case "Target ESP" -> new String[]{"Marker size","Vertical offset","Shape","Frame color"};
            case "Chinese Hat" -> new String[]{"Hat size","Hat height","Opacity"};
            case "Target Info","Me Armor" -> new String[]{"HUD scale","HUD opacity"};
            case "Hand Position" -> new String[]{"Hand X","Hand Y","Hand Z"};
            case "View Model" -> new String[]{"View X","View Y","View Z","View scale"};
            case "Crosshair" -> new String[]{"Size","Gap","Color"};
            case "Aspect Ratio" -> new String[]{"Horizontal ratio"};
            default -> new String[0];
        };
    }
    private String settingValue(int i){
        return switch(settingsFeature.name()){
            case "Target ESP" -> i==0?String.format("%.2f",FeatureSettings.targetEspLength):i==1?String.format("%.2f",FeatureSettings.targetEspHeight):i==2?(FeatureSettings.targetEspCircle?"Circle":"Square"):colorName(FeatureSettings.targetEspColor);
            case "Chinese Hat" -> i==0?String.format("%.2f",FeatureSettings.hatScale):i==1?String.format("%.2f",FeatureSettings.hatHeight):FeatureSettings.hatAlpha+"";
            case "Target Info","Me Armor" -> i==0?String.format("%.2f",FeatureSettings.hudScale):FeatureSettings.hudAlpha+"";
            case "Hand Position" -> String.format("%.2f",i==0?FeatureSettings.handX:i==1?FeatureSettings.handY:FeatureSettings.handZ);
            case "View Model" -> String.format("%.2f",i==0?FeatureSettings.viewModelX:i==1?FeatureSettings.viewModelY:i==2?FeatureSettings.viewModelZ:FeatureSettings.viewModelScale);
            case "Crosshair" -> i==0?String.format("%.1f",FeatureSettings.crosshairSize):i==1?String.format("%.1f",FeatureSettings.crosshairGap):colorName(FeatureSettings.crosshairColor);
            case "Aspect Ratio" -> String.format("%.2f",FeatureSettings.aspectRatio);
            default -> "";
        };
    }
    private void changeSetting(int i,int dir){
        switch(settingsFeature.name()){
            case "Target ESP" -> {if(i==0)FeatureSettings.targetEspLength=clamp(FeatureSettings.targetEspLength+dir*0.04,0.10,0.50);else if(i==1)FeatureSettings.targetEspHeight=clamp(FeatureSettings.targetEspHeight+dir*0.05,-0.8,0.8);else if(i==2)FeatureSettings.targetEspCircle=!FeatureSettings.targetEspCircle;else FeatureSettings.targetEspColor=nextColor(FeatureSettings.targetEspColor,dir);}
            case "Chinese Hat" -> {if(i==0)FeatureSettings.hatScale=clamp(FeatureSettings.hatScale+dir*0.1,0.4,2);else if(i==1)FeatureSettings.hatHeight=clamp(FeatureSettings.hatHeight+dir*0.1,0.2,1.6);else FeatureSettings.hatAlpha=(int)clamp(FeatureSettings.hatAlpha+dir*15,30,255);}
            case "Target Info","Me Armor" -> {if(i==0)FeatureSettings.hudScale=clamp(FeatureSettings.hudScale+dir*0.1,0.6,1.8);else FeatureSettings.hudAlpha=(int)clamp(FeatureSettings.hudAlpha+dir*15,50,255);}
            case "Hand Position" -> {if(i==0)FeatureSettings.handX=clamp(FeatureSettings.handX+dir*0.05,-1,1);else if(i==1)FeatureSettings.handY=clamp(FeatureSettings.handY+dir*0.05,-1,1);else FeatureSettings.handZ=clamp(FeatureSettings.handZ+dir*0.05,-1,1);}
            case "View Model" -> {if(i==0)FeatureSettings.viewModelX=clamp(FeatureSettings.viewModelX+dir*0.05,-1,1);else if(i==1)FeatureSettings.viewModelY=clamp(FeatureSettings.viewModelY+dir*0.05,-1,1);else if(i==2)FeatureSettings.viewModelZ=clamp(FeatureSettings.viewModelZ+dir*0.05,-1,1);else FeatureSettings.viewModelScale=clamp(FeatureSettings.viewModelScale+dir*0.05,0.5,1.5);}
            case "Crosshair" -> {if(i==0)FeatureSettings.crosshairSize=clamp(FeatureSettings.crosshairSize+dir,2,14);else if(i==1)FeatureSettings.crosshairGap=clamp(FeatureSettings.crosshairGap+dir,0,8);else FeatureSettings.crosshairColor=nextColor(FeatureSettings.crosshairColor,dir);}
            case "Aspect Ratio" -> FeatureSettings.aspectRatio=clamp(FeatureSettings.aspectRatio+dir*0.05,0.5,2.0);
        }
        FeatureConfig.save();
    }
    private static double clamp(double v,double min,double max){return Math.max(min,Math.min(max,v));}
    private static final int[] COLORS={0xFFE9A0D1,0xFF65D9E8,0xFF8DE18B,0xFFFFD166,0xFFFFFFFF};
    private static int nextColor(int current,int direction){int index=0;for(int i=0;i<COLORS.length;i++)if(COLORS[i]==current){index=i;break;}return COLORS[Math.floorMod(index+direction,COLORS.length)];}
    private static String colorName(int color){return switch(color){case 0xFFE9A0D1->"Pink";case 0xFF65D9E8->"Cyan";case 0xFF8DE18B->"Green";case 0xFFFFD166->"Gold";case 0xFFFFFFFF->"White";default->"Custom";};}

    private void renderSettings(DrawContext c){
        int w=360,h=220,x=panelX+(PANEL_WIDTH-w)/2,y=panelY+(PANEL_HEIGHT-h)/2;
        c.fill(x-2,y-2,x+w+2,y+h+2,0xFFE58BCB);c.fill(x,y,x+w,y+h,0xFF171920);
        c.drawText(textRenderer,"SETTINGS  /  "+settingsFeature.name().toUpperCase(),x+14,y+12,ACCENT,true);
        c.drawText(textRenderer,"−",x+w-48,y+11,0xFFFFAFCF,true);
        String[] names=settingNames();
        if(names.length==0)c.drawText(textRenderer,"No adjustable values for this module yet.",x+16,y+62,0xFFB8BBC5,false);
        for(int i=0;i<names.length;i++){
            int ry=y+48+i*34;
            c.drawText(textRenderer,names[i],x+16,ry+8,0xFFE7E8ED,false);
            c.fill(x+214,ry,x+242,ry+25,0xFF34313B);c.drawCenteredTextWithShadow(textRenderer,"−",x+228,ry+7,0xFFE8C4DC);
            c.drawCenteredTextWithShadow(textRenderer,settingValue(i),x+278,ry+7,0xFFFFFFFF);
            c.fill(x+314,ry,x+342,ry+25,0xFF34313B);c.drawCenteredTextWithShadow(textRenderer,"+",x+328,ry+7,0xFFE8C4DC);
        }
        c.drawText(textRenderer,"Changes are saved automatically",x+16,y+h-20,0xFF858A96,false);
    }

    @Override public boolean mouseClicked(double mx,double my,int button){
        if(settingsOpen){
            int w=360,h=220,x=panelX+(PANEL_WIDTH-w)/2,y=panelY+(PANEL_HEIGHT-h)/2;
            if(mx>=x+w-58&&mx<=x+w&&my>=y&&my<=y+38){settingsOpen=false;settingsFeature=null;return true;}
            String[] rows=settingNames();
            for(int i=0;i<rows.length;i++){
                int ry=y+48+i*34;
                if(my>=ry&&my<=ry+25){
                    if(mx>=x+214&&mx<=x+242){changeSetting(i,-1);return true;}
                    if(mx>=x+314&&mx<=x+342){changeSetting(i,1);return true;}
                }
            }
            return true;
        }
        if(button==1){
            List<Feature> fs=visibleFeatures();
            for(int i=0;i<fs.size();i++){
                int x=panelX+18+(i%3)*220,y=panelY+103+(i/3)*61;
                if(mx>=x&&mx<=x+210&&my>=y&&my<=y+52){settingsFeature=fs.get(i);settingsOpen=true;searchFocused=false;return true;}
            }
            return super.mouseClicked(mx,my,button);
        }
        if(button!=0)return super.mouseClicked(mx,my,button);
        if(mx>=panelX+PANEL_WIDTH-32&&mx<=panelX+PANEL_WIDTH-12&&my>=panelY+62&&my<=panelY+85){close();return true;}
        int searchX=panelX+PANEL_WIDTH-158;searchFocused=mx>=searchX&&mx<=panelX+PANEL_WIDTH-42&&my>=panelY+62&&my<=panelY+85;
        int tabWidth=77,tabsX=panelX+232;
        for(Feature.Category category:Feature.Category.values()){
            int x=tabsX+category.ordinal()*(tabWidth+5);
            if(mx>=x&&mx<=x+tabWidth&&my>=panelY+13&&my<=panelY+43){selected=category;return true;}
        }
        List<Feature> fs=visibleFeatures();
        for(int i=0;i<fs.size();i++){
            int x=panelX+18+(i%3)*220,y=panelY+103+(i/3)*61;
            if(mx<x||mx>x+210||my<y||my>y+52)continue;
            Feature f=fs.get(i);
            if(mx>=x+152&&my>=y+24){f.setEnabled(!f.enabled());FeatureConfig.save();}
            else if(my>=y+39){bindingFeature=f;searchFocused=false;}
            else {f.setEnabled(!f.enabled());FeatureConfig.save();}
            return true;
        }
        return super.mouseClicked(mx,my,button);
    }

    @Override public boolean keyPressed(int key,int scan,int modifiers){
        if(settingsOpen){if(key==GLFW.GLFW_KEY_ESCAPE){settingsOpen=false;settingsFeature=null;}return true;}
        if(bindingFeature!=null){
            if(key==GLFW.GLFW_KEY_ESCAPE)bindingFeature=null;
            else{bindingFeature.setBindKey(key);FeatureConfig.save();bindingFeature=null;}
            return true;
        }
        if(searchFocused){
            if(key==GLFW.GLFW_KEY_ESCAPE)searchFocused=false;
            else if(key==GLFW.GLFW_KEY_BACKSPACE&&!searchQuery.isEmpty())searchQuery=searchQuery.substring(0,searchQuery.length()-1);
            else if(key==GLFW.GLFW_KEY_ENTER)searchFocused=false;
            else return true;
            return true;
        }
        return super.keyPressed(key,scan,modifiers);
    }
    @Override public boolean charTyped(char chr,int modifiers){
        if(searchFocused&&bindingFeature==null&&!Character.isISOControl(chr)&&searchQuery.length()<32){searchQuery+=chr;return true;}
        return super.charTyped(chr,modifiers);
    }
    @Override public boolean shouldPause(){return false;}
}
