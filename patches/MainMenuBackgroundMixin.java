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
    // Fill the whole GUI: stretch slightly to remove vanilla edges on wide screens.
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
        float scaleX = screenWidth / (float) imageWidth;
        float scaleY = screenHeight / (float) imageHeight;

        context.getMatrices().push();
        context.getMatrices().scale(scaleX, scaleY, 1.0f);
        context.drawTexture(RenderLayer::getGuiTextured, texture,
                0, 0, 0.0f, 0.0f, imageWidth, imageHeight, imageWidth, imageHeight);
        context.getMatrices().pop();
    }
}
