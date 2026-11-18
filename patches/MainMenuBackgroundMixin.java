package dev.xaka.visuals.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MainMenuBackgroundMixin {
    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At("TAIL"), require = 0)
    private void xakavisuals$drawMainMenuBackground(DrawContext context, int mouseX,
                                                   int mouseY, float delta, CallbackInfo ci) {
        if ((Object) this instanceof TitleScreen) {
            MinecraftClient client = MinecraftClient.getInstance();
            Identifier texture = Identifier.of("xakavisuals", "textures/gui/main_menu_bg.png");
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            context.drawTexture(RenderLayer::getGuiTextured, texture,
                    0, 0, 0.0f, 0.0f, width, height, 1280, 720);
        }
    }
}
