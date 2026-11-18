package dev.xaka.visuals.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MainMenuBackgroundMixin {
    // Draw the complete 1280x720 image, uniformly scaled to fit the GUI.
    // drawTexture's width/height are source-region dimensions, not a resize target.
    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At("TAIL"), require = 1)
    private void xakavisuals$drawMainMenuBackground(DrawContext context, int mouseX,
                                                   int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Identifier texture = Identifier.of("xakavisuals", "textures/gui/main_menu_bg.png");
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        final int imageWidth = 1280;
        final int imageHeight = 720;
        float scale = Math.min(screenWidth / (float) imageWidth,
                               screenHeight / (float) imageHeight);
        float drawWidth = imageWidth * scale;
        float drawHeight = imageHeight * scale;
        float x = (screenWidth - drawWidth) * 0.5f;
        float y = (screenHeight - drawHeight) * 0.5f;

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0.0f);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawTexture(RenderLayer::getGuiTextured, texture,
                0, 0, 0.0f, 0.0f, imageWidth, imageHeight, imageWidth, imageHeight);
        context.getMatrices().pop();
    }
}
