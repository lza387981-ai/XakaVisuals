package com.xaka.visualv2;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class XakaVisualV2Client implements ClientModInitializer {
    public static int markerSize = 5;
    public static int haloSize = 8;
    public static float handOffsetX = 0.0f;
    public static float handOffsetY = 0.0f;
    public static float handOffsetZ = 0.0f;
    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xakavisualv2.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.MISC_CATEGORY
        ));

        XakaHud.register();
        XakaWorldVisuals.register();
        XakaPerformance.register();
        XakaCosmeticTrail.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen instanceof XakaClickScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new XakaClickScreen());
                }
            }
        });
        System.out.println("[XakaVisual.V2] Initialized. Press Right Shift to toggle the menu.");
    }
}
