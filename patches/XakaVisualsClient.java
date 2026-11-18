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
    public static boolean haloEnabled = false;
    public static int haloSize = 8;
    public static boolean bodyMarkerEnabled = false;
    public static boolean bodyMarkerSquare = false;
    public static int bodyMarkerSize = 5;
    public static float bodyMarkerOffsetX = 0.0f;
    public static float bodyMarkerOffsetY = 0.0f;
    public static float bodyMarkerOffsetZ = 0.0f;
    public static int accentIndex = 0;
    public static boolean petEnabled = false;
    public static boolean petWither = false;
    public static int petSize = 5;
    // First-person hand/item offsets, in camera-local blocks.
    public static float handOffsetX = 0.0f;
    public static float handOffsetY = 0.0f;
    public static float handOffsetZ = 0.0f;
    public static boolean fpsBoost = false;
    private static Object previousParticles = null;

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

        XakaVisualsRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                Object particleOption = client.options.getParticles();
                java.lang.reflect.Method getter = particleOption.getClass().getMethod("getValue");
                java.lang.reflect.Method setter = particleOption.getClass().getMethod("setValue", getter.getReturnType());
                if (fpsBoost && previousParticles == null) {
                    previousParticles = getter.invoke(particleOption);
                    Object minimal = java.lang.Enum.valueOf((Class) getter.getReturnType(), "MINIMAL");
                    setter.invoke(particleOption, minimal);
                } else if (!fpsBoost && previousParticles != null) {
                    setter.invoke(particleOption, previousParticles);
                    previousParticles = null;
                }
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                // If this Minecraft version exposes a different option type, leave it untouched.
            }
        });

        // Decorative HUD marker only: it does not target, track, or select entities.
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.hudHidden) return;

            // Minimal XakaVisuals watermark: a custom X emblem and clean label.
            if (watermark) {
                int wx = 8;
                int wy = 8;
                int accent = accent();
                drawContext.fill(wx, wy, wx + 22, wy + 22, 0xCC111622);
                drawContext.fill(wx, wy, wx + 2, wy + 22, accent);
                drawContext.fill(wx + 4, wy + 3, wx + 20, wy + 19, 0xFF171326);
                drawContext.fill(wx + 6, wy + 8, wx + 18, wy + 10, accent);
                drawContext.fill(wx + 8, wy + 5, wx + 10, wy + 17, accent);
                drawContext.fill(wx + 14, wy + 5, wx + 16, wy + 17, accent);
                drawContext.fill(wx + 11, wy + 6, wx + 13, wy + 16, accent);
                drawContext.fill(wx + 27, wy + 2, wx + 116, wy + 20, 0xAA111622);
                drawContext.drawTextWithShadow(client.textRenderer, "XAKA", wx + 33, wy + 3, 0xFFF2F3F8);
                drawContext.drawTextWithShadow(client.textRenderer, "VISUALS", wx + 67, wy + 3, accent);
            }

            int hudY = watermark ? 36 : 8;
            if (fps) {
                String fpsText = client.getCurrentFps() + " FPS";
                drawContext.drawTextWithShadow(client.textRenderer, fpsText, 8, hudY, accent());
                hudY += 12;
            }
            if (coordinates && client.player != null) {
                var pos = client.player.getPos();
                String coordsText = String.format(java.util.Locale.ROOT, "XYZ %.1f / %.1f / %.1f", pos.x, pos.y, pos.z);
                drawContext.drawTextWithShadow(client.textRenderer, coordsText, 8, hudY, 0xFFE7EAF2);
            }

            if (!markerEnabled) return;
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
