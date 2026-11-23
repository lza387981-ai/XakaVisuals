package com.xaka.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class XakaMenuScreen extends Screen {
    private static final int PANEL_WIDTH = 690;
    private static final int PANEL_HEIGHT = 390;
    private static final int ACCENT = 0xFFE58BCB;
    private Feature.Category selected = Feature.Category.RENDER;
    private int panelX;
    private int panelY;
    private String searchQuery = "";
    private boolean searchFocused;
    private Feature bindingFeature;

    public XakaMenuScreen() {
        super(Text.literal("XakaVisuals V3"));
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_WIDTH) / 2;
        panelY = (height - PANEL_HEIGHT) / 2;
    }

    private List<Feature> visibleFeatures() {
        return FeatureRegistry.in(selected).stream()
                .filter(feature -> feature.name().toLowerCase().contains(searchQuery.toLowerCase()))
                .toList();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFFE3E3E3);
        context.fill(panelX + 2, panelY + 2, panelX + PANEL_WIDTH - 2, panelY + PANEL_HEIGHT - 2, 0xF20E1015);

        // Header
        context.fill(panelX + 3, panelY + 3, panelX + PANEL_WIDTH - 3, panelY + 54, 0xFF171920);
        context.fill(panelX + 3, panelY + 3, panelX + 6, panelY + 54, ACCENT);
        context.drawText(textRenderer, "XAKA", panelX + 18, panelY + 12, ACCENT, true);
        context.drawText(textRenderer, "VISUALS  /  V3", panelX + 18, panelY + 27, 0xFFE6E7EC, true);
        context.drawText(textRenderer, "CLIENT EDITION", panelX + 112, panelY + 29, 0xFF777C89, false);

        int tabWidth = 77;
        int tabsX = panelX + 232;
        for (Feature.Category category : Feature.Category.values()) {
            int x = tabsX + category.ordinal() * (tabWidth + 5);
            boolean active = category == selected;
            context.fill(x, panelY + 13, x + tabWidth, panelY + 43, active ? 0xFFE9C4DE : 0xFF292C35);
            if (active) context.fill(x, panelY + 40, x + tabWidth, panelY + 43, ACCENT);
            context.drawCenteredTextWithShadow(textRenderer, category.title(), x + tabWidth / 2,
                    panelY + 24, active ? 0xFF25202A : 0xFFD7D9E0);
        }

        // Search and close
        int searchX = panelX + PANEL_WIDTH - 158;
        context.fill(searchX, panelY + 62, panelX + PANEL_WIDTH - 42, panelY + 85,
                searchFocused ? 0xFF34313B : 0xFF20232B);
        context.drawText(textRenderer, searchQuery.isEmpty() ? "Search modules..." : searchQuery,
                searchX + 7, panelY + 70, searchQuery.isEmpty() ? 0xFF858996 : 0xFFF1EAF0, false);
        context.fill(panelX + PANEL_WIDTH - 32, panelY + 62, panelX + PANEL_WIDTH - 12, panelY + 85, 0xFF3B252F);
        context.drawCenteredTextWithShadow(textRenderer, "×", panelX + PANEL_WIDTH - 22, panelY + 68, 0xFFFF9DAF);

        context.drawText(textRenderer, selected.title().toUpperCase(), panelX + 18, panelY + 69, 0xFFEDEEF2, true);
        context.drawText(textRenderer, visibleFeatures().size() + " MODULES",
                panelX + 18, panelY + 82, 0xFF777D8B, false);

        List<Feature> features = visibleFeatures();
        int cardWidth = 210;
        int cardHeight = 52;
        int startX = panelX + 18;
        int startY = panelY + 103;

        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            int x = startX + (i % 3) * 220;
            int y = startY + (i / 3) * 61;
            boolean hovered = mouseX >= x && mouseX <= x + cardWidth && mouseY >= y && mouseY <= y + cardHeight;
            context.fill(x, y, x + cardWidth, y + cardHeight, hovered ? 0xFF35313B : 0xFF282B34);
            context.fill(x + 1, y + 1, x + cardWidth - 1, y + cardHeight - 1, 0xFF1C1F27);
            context.fill(x, y, x + 2, y + cardHeight, feature.enabled() ? ACCENT : 0xFF555A66);
            context.drawText(textRenderer, feature.name(), x + 10, y + 8, 0xFFF0F1F5, true);

            context.drawText(textRenderer, feature.enabled() ? "ACTIVE" : "INACTIVE",
                    x + 10, y + 29, feature.enabled() ? 0xFFE7A5D2 : 0xFF858A96, false);

            int toggleX = x + cardWidth - 54;
            context.fill(toggleX, y + 27, toggleX + 44, y + 44,
                    feature.enabled() ? 0xFF744765 : 0xFF353944);
            int knobX = feature.enabled() ? toggleX + 29 : toggleX + 4;
            context.fill(knobX, y + 30, knobX + 11, y + 41,
                    feature.enabled() ? 0xFFFFD9F0 : 0xFF9AA0AC);

            String bindLabel = bindingFeature == feature ? "PRESS KEY" :
                    feature.bindKey() < 0 ? "BIND" :
                    InputUtil.fromKeyCode(feature.bindKey(), 0).getLocalizedText().getString();
            context.drawText(textRenderer, bindLabel, x + 10, y + 43, 0xFF858A96, false);
        }

        if (features.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, "No modules found",
                    panelX + PANEL_WIDTH / 2, panelY + 190, 0xFF9A9EAA);
        }
        if (bindingFeature != null) {
            context.drawCenteredTextWithShadow(textRenderer, "PRESS A KEY  •  ESC TO CANCEL",
                    panelX + PANEL_WIDTH / 2, panelY + PANEL_HEIGHT - 42, 0xFFFFD0E8);
        } else {
            context.drawText(textRenderer, "RIGHT SHIFT  •  TOGGLE MENU",
                    panelX + 18, panelY + PANEL_HEIGHT - 23, 0xFF777D8B, false);
        }
        context.drawText(textRenderer, "XAKAVISUALS", panelX + PANEL_WIDTH - 105,
                panelY + PANEL_HEIGHT - 23, 0xFF777D8B, false);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        if (mouseX >= panelX + PANEL_WIDTH - 32 && mouseX <= panelX + PANEL_WIDTH - 12
                && mouseY >= panelY + 62 && mouseY <= panelY + 85) {
            close();
            return true;
        }

        int searchX = panelX + PANEL_WIDTH - 158;
        searchFocused = mouseX >= searchX && mouseX <= panelX + PANEL_WIDTH - 42
                && mouseY >= panelY + 62 && mouseY <= panelY + 85;

        int tabWidth = 77;
        int tabsX = panelX + 232;
        for (Feature.Category category : Feature.Category.values()) {
            int x = tabsX + category.ordinal() * (tabWidth + 5);
            if (mouseX >= x && mouseX <= x + tabWidth && mouseY >= panelY + 13 && mouseY <= panelY + 43) {
                selected = category;
                return true;
            }
        }

        List<Feature> features = visibleFeatures();
        int cardWidth = 210, cardHeight = 52, startX = panelX + 18, startY = panelY + 103;
        for (int i = 0; i < features.size(); i++) {
            int x = startX + (i % 3) * 220;
            int y = startY + (i / 3) * 61;
            if (mouseX < x || mouseX > x + cardWidth || mouseY < y || mouseY > y + cardHeight) continue;
            Feature feature = features.get(i);
            if (mouseX >= x + cardWidth - 58 && mouseY >= y + 24) {
                feature.setEnabled(!feature.enabled());
                FeatureConfig.save();
            } else if (mouseY >= y + 39) {
                bindingFeature = feature;
                searchFocused = false;
            } else {
                feature.setEnabled(!feature.enabled());
                FeatureConfig.save();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingFeature != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) bindingFeature = null;
            else {
                bindingFeature.setBindKey(keyCode);
                FeatureConfig.save();
                bindingFeature = null;
            }
            return true;
        }
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) searchFocused = false;
            else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty())
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            else if (keyCode == GLFW.GLFW_KEY_ENTER) searchFocused = false;
            else return true;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused && bindingFeature == null && !Character.isISOControl(chr) && searchQuery.length() < 32) {
            searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
