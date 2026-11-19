package dev.xaka.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class XakaVisualsClient implements ClientModInitializer {
    private static KeyBinding openMenu;

    @Override
    public void onInitializeClient() {
        VisualSettings.load();
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xakavisuals.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.xakavisuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                if (client.currentScreen instanceof XakaVisualsScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new XakaVisualsScreen());
                }
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            int y = 8;
            if (VisualSettings.watermark) {
                drawContext.fill(8, y - 3, 112, y + 14, 0xA9151824);
                drawContext.fill(8, y - 3, 10, y + 12, 0xFF8B5CF6);
                drawContext.drawTextWithShadow(client.textRenderer,
                        Text.literal("Xaka").styled(s -> s.withColor(0xFFC4B5FD)),
                        16, y, 0xFFFFFFFF);
                drawContext.drawTextWithShadow(client.textRenderer,
                        Text.literal("Visuals"), 46, y, 0xFFFFFFFF);
                y += 20;
            }
            if (VisualSettings.fpsCounter) {
                drawContext.drawTextWithShadow(client.textRenderer,
                        client.getCurrentFps() + " FPS", 8, y, 0xFFB8F3D1);
            }
        });
    }
}
