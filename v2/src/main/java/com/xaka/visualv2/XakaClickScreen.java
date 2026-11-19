package com.xaka.visualv2;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;\nimport java.util.Set;\nimport org.lwjgl.glfw.GLFW;

public final class XakaClickScreen extends Screen {
    private static final int ACCENT = 0xFF39BDF8;
    private static final int PANEL = 0xF0111725;
    private static final int CARD = 0xFF192438;
    private static final int MUTED = 0xFF91A4BE;

    private static final Map<String, List<String>> MODULES = new LinkedHashMap<>();
    private static final Map<String, Boolean> STATES = new LinkedHashMap<>();
    private static final Set<String> IMPLEMENTED = Set.of(
            "XAKA Watermark", "FPS Counter", "Coordinates",
            "Armor Status", "Keystrokes", "Target Info"
    );

    public static boolean isEnabled(String name) {
        return STATES.getOrDefault(name, false);
    }

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
                "Reduced Menu Animations", "Particles: Minimal",
                "Background Effects"
        ));

        MODULES.values().forEach(items ->
                items.forEach(name -> STATES.putIfAbsent(name, false)));
    }

    private String selectedCategory = "Visual";
    private int scrollOffset = 0;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int contentLeft;
    private int contentTop;
    private int contentWidth;
    private int visibleRows;

    public XakaClickScreen() {
        super(Text.literal("XAKA VISUAL.V2"));
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
        context.drawText(textRenderer, Text.literal("RIGHT SHIFT  •  MENU"),
                panelLeft + panelWidth - 150, panelTop + 24, MUTED, false);

        int navWidth = contentLeft - panelLeft - 22;
        int navX = panelLeft + 12;
        int navY = panelTop + 88;
        int index = 0;
        for (String category : MODULES.keySet()) {
            int y = navY + index * 38;
            boolean active = selectedCategory.equals(category);
            context.fill(navX, y, navX + navWidth, y + 30, active ? 0xFF203B55 : 0x00111725);
            if (active) {
                context.fill(navX, y, navX + 3, y + 30, ACCENT);
            }
            context.drawText(textRenderer, Text.literal(category),
                    navX + 12, y + 10, active ? 0xFFFFFFFF : MUTED, false);
            index++;
        }

        context.drawText(textRenderer, Text.literal(selectedCategory.toUpperCase()),
                contentLeft, panelTop + 84 - 18, ACCENT, false);
        context.drawText(textRenderer, Text.literal("Enable or disable a visual option"),
                contentLeft, panelTop + 84, MUTED, false);

        context.enableScissor(contentLeft, contentTop,
                contentLeft + contentWidth, panelTop + panelHeight - 18);

        List<String> items = MODULES.get(selectedCategory);
        for (int row = 0; row < items.size(); row++) {
            int visibleRow = row - scrollOffset;
            int y = contentTop + visibleRow * 58;
            if (y + 48 < contentTop || y > panelTop + panelHeight - 18) continue;

            String name = items.get(row);
            boolean enabled = STATES.getOrDefault(name, false);
            int right = contentLeft + contentWidth;
            context.fill(contentLeft, y, right, y + 48, CARD);
            context.drawText(textRenderer, Text.literal(name),
                    contentLeft + 12, y + 10, 0xFFF1F6FF, false);
            context.drawText(textRenderer, Text.literal("VISUAL MODULE"),
                    contentLeft + 12, y + 27, MUTED, false);

            int toggleWidth = 48;
            int toggleX = right - toggleWidth - 12;
            int toggleY = y + 13;
            context.fill(toggleX, toggleY, toggleX + toggleWidth, toggleY + 22,
                    enabled ? 0xFF176B83 : 0xFF344257);
            int knobX = enabled ? toggleX + 28 : toggleX + 4;
            context.fill(knobX, toggleY + 4, knobX + 16, toggleY + 18, 0xFFFFFFFF);
        }
        context.disableScissor();

        if (items.size() > visibleRows) {
            context.drawText(textRenderer,
                    Text.literal("SCROLL  •  " + (scrollOffset + 1) + "/" + items.size()),
                    contentLeft, panelTop + panelHeight - 13, MUTED, false);
        }
        super.render(context, mouseX, mouseY, delta);
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
                String name = items.get(row);
                STATES.put(name, !STATES.getOrDefault(name, false));
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
    public boolean shouldPause() {
        return false;
    }
}
