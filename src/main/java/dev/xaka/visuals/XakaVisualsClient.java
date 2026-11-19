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
    private static KeyBinding zoomKey;
    private static boolean zoomActive;
    private static int previousFov = -1;
    private static double previousGamma = Double.NaN;

    @Override
    public void onInitializeClient() {
        VisualSettings.load();
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xakavisuals.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.xakavisuals"
        ));

        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xakavisuals.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.xakavisuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                if (VisualSettings.fullbright) {
                    if (Double.isNaN(previousGamma)) previousGamma = client.options.getGamma().getValue();
                    client.options.getGamma().setValue(16.0);
                } else if (!Double.isNaN(previousGamma)) {
                    client.options.getGamma().setValue(previousGamma);
                    previousGamma = Double.NaN;
                }
                if (zoomKey.isPressed()) {
                    if (!zoomActive) {
                        previousFov = client.options.getFov().getValue();
                        zoomActive = true;
                    }
                    client.options.getFov().setValue(30);
                } else if (zoomActive) {
                    if (previousFov >= 0) client.options.getFov().setValue(previousFov);
                    previousFov = -1;
                    zoomActive = false;
                }
            } else if (zoomActive) {
                if (previousFov >= 0) client.options.getFov().setValue(previousFov);
                previousFov = -1;
                zoomActive = false;
            }
            while (openMenu.wasPressed()) {
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
            if (VisualSettings.coordinates) {
                String coords = String.format(java.util.Locale.ROOT, "XYZ: %.1f / %.1f / %.1f",
                        client.player.getX(), client.player.getY(), client.player.getZ());
                drawContext.drawTextWithShadow(client.textRenderer, coords, 8, y, 0xFFE5E7EB);
                y += 12;
            }
            if (VisualSettings.fpsCounter) {
                drawContext.drawTextWithShadow(client.textRenderer,
                        client.getCurrentFps() + " FPS", 8, y, 0xFFB8F3D1);
            }
        });
    }
}
