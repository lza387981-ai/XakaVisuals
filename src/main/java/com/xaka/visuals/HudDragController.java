package com.xaka.visuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class HudDragController {
    private static int targetX = -1, targetY = -1;
    private static int armorX = -1, armorY = -1;
    private static String dragging = null;
    private static double grabX, grabY;
    private static boolean wasDown;

    private HudDragController() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(HudDragController::tick);
    }

    public static int x(String id, int fallback) {
        return "target".equals(id) ? (targetX < 0 ? fallback : targetX)
                : (armorX < 0 ? fallback : armorX);
    }

    public static int y(String id, int fallback) {
        return "target".equals(id) ? (targetY < 0 ? fallback : targetY)
                : (armorY < 0 ? fallback : armorY);
    }

    public static int targetX() { return targetX; }
    public static int targetY() { return targetY; }
    public static int armorX() { return armorX; }
    public static int armorY() { return armorY; }

    public static void load(int tx, int ty, int ax, int ay) {
        targetX = tx;
        targetY = ty;
        armorX = ax;
        armorY = ay;
    }

    private static void tick(MinecraftClient client) {
        if (client.getWindow() == null || client.player == null || client.currentScreen != null) {
            dragging = null;
            wasDown = false;
            return;
        }

        long handle = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        double[] cx = new double[1], cy = new double[1];
        GLFW.glfwGetCursorPos(handle, cx, cy);
        double mx = cx[0] * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
        double my = cy[0] * client.getWindow().getScaledHeight() / client.getWindow().getHeight();

        double scale = FeatureSettings.hudScale;
        int targetWidth = (int) Math.round(190 * scale);
        int targetHeight = (int) Math.round(82 * scale);
        int armorSlot = (int) Math.round(32 * scale);
        int armorStep = (int) Math.round(36 * scale);
        int armorWidth = armorStep * 4;
        int armorHeight = armorSlot + (int) Math.round(25 * scale);

        if (down && !wasDown) {
            Feature target = FeatureRegistry.find("Target Info");
            Feature armor = FeatureRegistry.find("Me Armor");

            if (target != null && target.enabled()) {
                int dx = x("target", client.getWindow().getScaledWidth() - targetWidth - 12);
                int dy = y("target", client.getWindow().getScaledHeight() - targetHeight - (int) Math.round(42 * scale));
                if (inside(mx, my, dx, dy, targetWidth, targetHeight)) {
                    dragging = "target";
                    grabX = mx - dx;
                    grabY = my - dy;
                }
            }

            if (dragging == null && armor != null && armor.enabled()) {
                int dx = x("armor", client.getWindow().getScaledWidth() / 2 - armorWidth / 2);
                int dy = y("armor", client.getWindow().getScaledHeight() - armorSlot - (int) Math.round(26 * scale));
                int inset = (int) Math.round(5 * scale);
                int header = (int) Math.round(19 * scale);
                if (inside(mx, my, dx - inset, dy - header,
                        armorWidth + (int) Math.round(2 * scale), armorHeight)) {
                    dragging = "armor";
                    grabX = mx - dx;
                    grabY = my - dy;
                }
            }
        }

        if (down && dragging != null) {
            int nx = (int) Math.round(mx - grabX);
            int ny = (int) Math.round(my - grabY);
            int maxX = client.getWindow().getScaledWidth();
            int maxY = client.getWindow().getScaledHeight();

            if ("target".equals(dragging)) {
                targetX = Math.max(0, Math.min(maxX - targetWidth, nx));
                targetY = Math.max(0, Math.min(maxY - targetHeight, ny));
            } else {
                armorX = Math.max((int) Math.round(5 * scale),
                        Math.min(maxX - armorWidth, nx));
                armorY = Math.max((int) Math.round(19 * scale),
                        Math.min(maxY - armorSlot - (int) Math.round(6 * scale), ny));
            }
        }

        if (!down && wasDown && dragging != null) {
            FeatureConfig.save();
            dragging = null;
        }
        wasDown = down;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
