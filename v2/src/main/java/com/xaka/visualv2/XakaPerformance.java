package com.xaka.visualv2;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Method;

public final class XakaPerformance {
    private static Object previousParticles;

    private XakaPerformance() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(XakaPerformance::tick);
    }

    private static void tick(MinecraftClient client) {
        try {
            Object particleOption = client.options.getParticles();
            Method getter = particleOption.getClass().getMethod("getValue");
            Method setter = particleOption.getClass().getMethod("setValue", getter.getReturnType());

            if (XakaClickScreen.isEnabled("FPS Boost") && previousParticles == null) {
                previousParticles = getter.invoke(particleOption);
                Object minimal = Enum.valueOf((Class) getter.getReturnType(), "MINIMAL");
                setter.invoke(particleOption, minimal);
            } else if (!XakaClickScreen.isEnabled("FPS Boost") && previousParticles != null) {
                setter.invoke(particleOption, previousParticles);
                previousParticles = null;
            }
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            // If the option API differs, leave the player's settings unchanged.
        }
    }
}
