package com.xaka.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public final class XakaVisualsClient implements ClientModInitializer {
    private static KeyBinding openMenuKey;
    private final Set<Integer> keysDown = new HashSet<>();

    @Override
    public void onInitializeClient() {
        FeatureRegistry.init();
        TargetInfoHud.register();
        MeArmorHud.register();
        VisualFeatureController.register();
        HudDragController.register();
        CrosshairHud.register();
        TargetEspRenderer.register();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xakavisuals.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.xakavisuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new XakaMenuScreen());
                } else if (client.currentScreen instanceof XakaMenuScreen) {
                    client.setScreen(null);
                }
            }

            if (client.getWindow() == null) return;
            for (Feature feature : FeatureRegistry.all()) {
                int key = feature.bindKey();
                if (key < 0) continue;
                boolean down = InputUtil.isKeyPressed(client.getWindow().getHandle(), key);
                if (down && keysDown.add(key)) {
                    feature.setEnabled(!feature.enabled());
                    FeatureConfig.save();
                } else if (!down) {
                    keysDown.remove(key);
                }
            }
        });
    }
}
