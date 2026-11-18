package dev.xaka.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class XakaVisualsClient implements ClientModInitializer {
    public static boolean animations = true;
    public static boolean compactHud = false;
    public static boolean watermark = true;
    public static boolean fps = true;
    public static boolean coordinates = false;
    public static boolean markerEnabled = false;
    public static boolean markerSquare = false;
    public static int markerSize = 5;
    public static int accentIndex = 0;

    private static final int[] ACCENTS = {
            0xFF9B78FF, 0xFF69D6B0, 0xFFFF7EAA, 0xFFFFB45E, 0xFF69A9FF
    };

    public static int accent() {
        return ACCENTS[Math.floorMod(accentIndex, ACCENTS.length)];
    }

    @Override
    public void onInitializeClient() {
        KeyBinding openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xakavisuals.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.xakavisuals.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new XakaVisualsScreen());
            }
        });

        // Decorative HUD marker only: it does not target, track, or select entities.
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            if (!markerEnabled) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.hudHidden) return;
            int cx = client.getWindow().getScaledWidth() / 2;
            int cy = client.getWindow().getScaledHeight() / 2 + 38;
            int size = Math.max(2, Math.min(14, markerSize));
            int color = accent();
            if (markerSquare) {
                drawContext.fill(cx - size, cy - size, cx + size + 1, cy + size + 1, color);
            } else {
                for (int y = -size; y <= size; y++) {
                    int half = (int) Math.floor(Math.sqrt(size * size - y * y));
                    drawContext.fill(cx - half, cy + y, cx + half + 1, cy + y + 1, color);
                }
            }
        });
    }
}
