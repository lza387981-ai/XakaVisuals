package com.xaka.visualv2;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class XakaClickScreen extends Screen {
    private static final int ACCENT = 0xFF39BDF8;
    private static final int PANEL = 0xF0111725;
    private static final int CARD = 0xFF192438;
    private static final int MUTED = 0xFF91A4BE;

    private static final Map<String, List<String>> MODULES = new LinkedHashMap<>();
    private static final Map<String, Boolean> STATES = new LinkedHashMap<>();
    private static final Set<String> IMPLEMENTED = Set.of(
            "Target Marker", "Entity Halo", "Mini Creeper Pet", "Mini Wither Pet",
            "XAKA Watermark", "FPS Counter", "Coordinates", "Armor Status",
            "Keystrokes", "Target Info", "FPS Boost"
    );

    static {
        MODULES.put("Visual", List.of(
                "Target Marker", "Entity Halo", "Hit Particles",
                "Item Physics", "Motion Blur", "World Time Tint"
        ));
        MODULES.put("HUD", List.of(
                "XAKA Watermark", "FPS Counter", "Coordinates",
                "Armor Status", "Keystrokes", "Target Info"
        ));
        MODULES.put("Cosmetics", List.of(
                "Mini Creeper Pet", "Mini Wither Pet", "Cosmetic Trail",
                "Emote Wheel", "Name Tag Style"
        ));
        MODULES.put("Performance", List.of(
                "FPS Boost", "Reduced Menu Animations",
                "Particles: Minimal", "Background Effects"
        ));
        MODULES.put("Settings", List.of(
                "Marker Size", "Halo Size", "Hand X Offset",
                "Hand Y Offset", "Hand Z Offset", "FPS Boost"
        ));
        MODULES.values().forEach(items ->
                items.forEach(name -> STATES.putIfAbsent(name, false)));
    }

    private String selectedCategory = "Visual";
    private int scrollOffset;
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int contentLeft, contentTop, contentWidth, visibleRows;

    public XakaClickScreen() {
        super(Text.literal("XAKA VISUAL.V2"));
    }

    public static boolean isEnabled(String name) {
        return STATES.getOrDefault(name, false);
    }

    @Override
    protected void init() {
        panelWidth = Math.min(660, Math.max(320, width - 24));
        panelHeight = Math.min(430, Math.max(250, height - 24));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        contentLeft = panelLeft + Math.min(154, panelWidth / 3);
        contentTop = panelTop + 86;
        contentWidth = panelLeft + panelWidth - 18 - contentLeft;
        visibleRows = Math.max(1, (panelTop + panelHeight - 22 - contentTop) / 58);
        clampScroll();
    }

    private void clampScroll() {
        int count = MODULES.get(selectedCategory).size();
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, count - visibleRows)));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xA9000000);
        context.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, PANEL);
        context.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 3, ACCENT);
        context.fill(panelLeft + 14, panelTop + 70, panelLeft + panelWidth - 14, panelTop + 71, 0xFF2A3850);

        context.drawText(textRenderer, Text.literal("XAKA VISUAL.V2"),
                panelLeft + 18, panelTop + 18, 0xFFFFFFFF, false);
        context.drawText(textRenderer, Text.literal("CLIENT EDITION  /  FABRIC 1.21.4"),
                panelLeft + 18, panelTop + 37, MUTED, false);
        context.drawText(textRenderer, Text.literal("RIGHT SHIFT  •  CLOSE"),
                panelLeft + panelWidth - 150, panelTop + 24, MUTED, false);

        int navWidth = contentLeft - panelLeft - 22;
        int navX = panelLeft + 12;
        int navY = panelTop + 88;
        int index = 0;
        for (String category : MODULES.keySet()) {
            int y = navY + index * 38;
            boolean active = selectedCategory.equals(category);
            context.fill(navX, y, navX + navWidth, y + 30, active ? 0xFF203B55 : 0x00111725);
            if (active) context.fill(navX, y, navX + 3, y + 30, ACCENT);
            context.drawText(textRenderer, Text.literal(category),
                    navX + 12, y + 10, active ? 0xFFFFFFFF : MUTED, false);
            index++;
        }

        context.drawText(textRenderer, Text.literal(selectedCategory.toUpperCase()),
                contentLeft, panelTop + 66, ACCENT, false);
        context.drawText(textRenderer, Text.literal(selectedCategory.equals("Settings")
                        ? "Drag across a bar to adjust its value"
                        : "Select a module to enable or disable it"),
                contentLeft, panelTop + 84, MUTED, false);

        context.enableScissor(contentLeft, contentTop,
                contentLeft + contentWidth, panelTop + panelHeight - 18);
        List<String> items = MODULES.get(selectedCategory);
        if (selectedCategory.equals("Settings")) renderSettings(context, items);
        else renderModules(context, items);
        context.disableScissor();

        if (items.size() > visibleRows) {
            context.drawText(textRenderer,
                    Text.literal("SCROLL  •  " + (scrollOffset + 1) + "/" + items.size()),
                    contentLeft, panelTop + panelHeight - 13, MUTED, false);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderModules(DrawContext context, List<String> items) {
        int right = contentLeft + contentWidth;
        for (int row = 0; row < items.size(); row++) {
            int y = contentTop + (row - scrollOffset) * 58;
            if (y + 48 < contentTop || y > panelTop + panelHeight - 18) continue;
            String name = items.get(row);
            boolean supported = IMPLEMENTED.contains(name);
            boolean enabled = isEnabled(name);
            context.fill(contentLeft, y, right, y + 48, CARD);
            context.drawText(textRenderer, Text.literal(name),
                    contentLeft + 12, y + 10, 0xFFF1F6FF, false);
            context.drawText(textRenderer, Text.literal(supported ? "READY  •  VISUAL MODULE" : "COMING SOON"),
                    contentLeft + 12, y + 27, supported ? MUTED : 0xFFB58A62, false);

            int tx = right - 60;
            context.fill(tx, y + 13, tx + 48, y + 35,
                    !supported ? 0xFF293241 : enabled ? 0xFF176B83 : 0xFF344257);
            if (supported) {
                context.fill(tx + (enabled ? 28 : 4), y + 17,
                        tx + (enabled ? 44 : 20), y + 31, 0xFFFFFFFF);
            } else {
                context.drawText(textRenderer, Text.literal("—"), tx + 19, y + 18, MUTED, false);
            }
        }
    }

    private void renderSettings(DrawContext context, List<String> items) {
        int right = contentLeft + contentWidth;
        for (int row = 0; row < items.size(); row++) {
            int y = contentTop + (row - scrollOffset) * 58;
            if (y + 48 < contentTop || y > panelTop + panelHeight - 18) continue;
            context.fill(contentLeft, y, right, y + 48, CARD);
            context.drawText(textRenderer, Text.literal(items.get(row)),
                    contentLeft + 12, y + 7, 0xFFF1F6FF, false);

            if (row == 5) {
                boolean enabled = isEnabled("FPS Boost");
                context.drawText(textRenderer, Text.literal(enabled
                                ? "ON  •  REDUCES PARTICLE DETAIL"
                                : "OFF  •  RESTORES ORIGINAL SETTING"),
                        contentLeft + 12, y + 27, enabled ? ACCENT : MUTED, false);
                int tx = right - 60;
                context.fill(tx, y + 13, tx + 48, y + 35,
                        enabled ? 0xFF176B83 : 0xFF344257);
                context.fill(tx + (enabled ? 28 : 4), y + 17,
                        tx + (enabled ? 44 : 20), y + 31, 0xFFFFFFFF);
            } else {
                float value = switch (row) {
                    case 0 -> XakaVisualV2Client.markerSize;
                    case 1 -> XakaVisualV2Client.haloSize;
                    case 2 -> XakaVisualV2Client.handOffsetX;
                    case 3 -> XakaVisualV2Client.handOffsetY;
                    default -> XakaVisualV2Client.handOffsetZ;
                };
                String valueText = row < 2 ? Integer.toString((int) value)
                        : String.format(java.util.Locale.ROOT, "%.2f", value);
                context.drawText(textRenderer, Text.literal(valueText),
                        right - textRenderer.getWidth(valueText) - 12, y + 7, ACCENT, false);
                int sx = contentLeft + 12;
                int sw = contentWidth - 24;
                int sy = y + 34;
                context.fill(sx, sy, sx + sw, sy + 4, 0xFF344257);
                float ratio = row == 0 ? (value - 2.0f) / 12.0f
                        : row == 1 ? (value - 1.0f) / 15.0f : value + 0.5f;
                int fill = Math.max(0, Math.min(sw, Math.round(sw * ratio)));
                context.fill(sx, sy, sx + fill, sy + 4, ACCENT);
                context.fill(sx + fill - 3, sy - 2, sx + fill + 3, sy + 6, 0xFFFFFFFF);
            }
        }
    }

    private void applySettingAt(int row, double mouseX) {
        if (row == 5) {
            STATES.put("FPS Boost", !isEnabled("FPS Boost"));
            return;
        }
        int sx = contentLeft + 12;
        int sw = Math.max(1, contentWidth - 24);
        float ratio = (float) Math.max(0.0, Math.min(1.0, (mouseX - sx) / sw));
        switch (row) {
            case 0 -> XakaVisualV2Client.markerSize = 2 + Math.round(ratio * 12);
            case 1 -> XakaVisualV2Client.haloSize = 1 + Math.round(ratio * 15);
            case 2 -> XakaVisualV2Client.handOffsetX = ratio - 0.5f;
            case 3 -> XakaVisualV2Client.handOffsetY = ratio - 0.5f;
            case 4 -> XakaVisualV2Client.handOffsetZ = ratio - 0.5f;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int navWidth = contentLeft - panelLeft - 22;
        int navX = panelLeft + 12;
        int navY = panelTop + 88;
        int index = 0;
        for (String category : MODULES.keySet()) {
            int y = navY + index * 38;
            if (inside(mouseX, mouseY, navX, y, navWidth, 30)) {
                selectedCategory = category;
                scrollOffset = 0;
                return true;
            }
            index++;
        }

        if (inside(mouseX, mouseY, contentLeft, contentTop,
                contentWidth, visibleRows * 58)) {
            int row = (int) ((mouseY - contentTop) / 58.0) + scrollOffset;
            List<String> items = MODULES.get(selectedCategory);
            if (row >= 0 && row < items.size()) {
                if (selectedCategory.equals("Settings")) applySettingAt(row, mouseX);
                else {
                    String name = items.get(row);
                    if (IMPLEMENTED.contains(name)) STATES.put(name, !isEnabled(name));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {
        if (inside(mouseX, mouseY, contentLeft, contentTop,
                contentWidth, panelTop + panelHeight - contentTop - 18)) {
            scrollOffset -= (int) Math.signum(verticalAmount);
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private static boolean inside(double x, double y, int left, int top, int w, int h) {
        return x >= left && x <= left + w && y >= top && y <= top + h;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
