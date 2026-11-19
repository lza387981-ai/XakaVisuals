package dev.xaka.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class XakaVisualsClient implements ClientModInitializer {
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("xakavisuals", "main"));
    private static KeyBinding openMenu;

    @Override
    public void onInitializeClient() {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xakavisuals.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                client.setScreen(new XakaVisualsScreen());
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            int y = 8;
            if (VisualSettings.watermark) {
                drawContext.fill(8, y - 3, 116, y + 12, 0xA9151824);
                drawContext.fill(8, y - 3, 10, y + 12, 0xFF8B5CF6);
                drawContext.drawTextWithShadow(client.textRenderer,
                        Text.literal("XAKA").styled(s -> s.withColor(0xFFC4B5FD)),
                        16, y, 0xFFFFFFFF);
                drawContext.drawTextWithShadow(client.textRenderer,
                        Text.literal("VISUALS V2"), 52, y, 0xFFFFFFFF);
                y += 20;
            }
            if (VisualSettings.fpsCounter) {
                String fps = client.getCurrentFps() + " FPS";
                drawContext.drawTextWithShadow(client.textRenderer, fps, 8, y, 0xFFB8F3D1);
            }
        });
    }
}
