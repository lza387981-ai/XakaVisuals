package com.xaka.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class XakaMenuScreen extends Screen {
    private Feature.Category selected = Feature.Category.RENDER;
    private static final int PANEL_WIDTH = 690;
    private static final int PANEL_HEIGHT = 390;
    private int panelX;
    private int panelY;
    private String searchQuery = "";
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
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xE8D0D0D0);
        context.fill(panelX + 3, panelY + 3, panelX + PANEL_WIDTH - 3, panelY + PANEL_HEIGHT - 3, 0xE91A1A1A);
        context.fill(panelX + 12, panelY + 12, panelX + PANEL_WIDTH - 12, panelY + 48, 0xFF252525);
        context.drawText(textRenderer, "XakaVisuals V3", panelX + 20, panelY + 24, 0xFFFFFFFF, true);

        int tabWidth = 88;
        int tabsX = panelX + 210;
        for (Feature.Category category : Feature.Category.values()) {
            int x = tabsX + category.ordinal() * (tabWidth + 4);
            int color = category == selected ? 0xFFE8E2D8 : 0xFF343434;
            context.fill(x, panelY + 13, x + tabWidth, panelY + 43, color);
            context.drawCenteredTextWithShadow(textRenderer, category.title(), x + tabWidth / 2, panelY + 24,
                    category == selected ? 0xFF202020 : 0xFFFFFFFF);
        }

        // Search box and close control follow the reference layout.
        int searchX = panelX + PANEL_WIDTH - 164;
        context.fill(searchX, panelY + 56, panelX + PANEL_WIDTH - 14, panelY + 78, 0xFF090909);
        String searchLabel = searchQuery.isEmpty() ? "Search..." : searchQuery;
        context.drawText(textRenderer, searchLabel, searchX + 8, panelY + 63, 0xFFBBBBBB, false);
        context.fill(panelX + PANEL_WIDTH - 31, panelY + 8, panelX + PANEL_WIDTH - 10, panelY + 29, 0xFFD93636);
        context.drawCenteredTextWithShadow(textRenderer, "X", panelX + PANEL_WIDTH - 20, panelY + 14, 0xFFFFFFFF);

        context.drawText(textRenderer, selected.title(), panelX + 22, panelY + 62, 0xFFEAEAEA, true);
        List<Feature> features = visibleFeatures();
        int cardWidth = 210;
        int cardHeight = 48;
        int gapX = 10;
        int gapY = 8;
        int startX = panelX + 18;
        int startY = panelY + 84;

        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            int col = i % 3;
            int row = i / 3;
            int x = startX + col * (cardWidth + gapX);
            int y = startY + row * (cardHeight + gapY);
            context.fill(x, y, x + cardWidth, y + cardHeight, 0xFF101010);
            context.fill(x + 1, y + 1, x + cardWidth - 1, y + cardHeight - 1, 0xFF242424);
            context.drawText(textRenderer, feature.name(), x + 9, y + 8, 0xFFFFFFFF, true);
            int toggleColor = feature.enabled() ? 0xFFBFBFBF : 0xFF555555;
            context.fill(x + 9, y + 27, x + 22, y + 39, toggleColor);
            context.drawText(textRenderer, feature.enabled() ? "Enabled" : "Disabled",
                    x + 28, y + 29, 0xFFBBBBBB, false);
            context.fill(x + cardWidth - 48, y + 25, x + cardWidth - 8, y + 41, 0xFF3A3A3A);
            String bindLabel = bindingFeature == feature ? "Press..." :
                    feature.bindKey() < 0 ? "Bind" :
                    InputUtil.fromKeyCode(feature.bindKey(), 0).getLocalizedText().getString();
            context.drawCenteredTextWithShadow(textRenderer, bindLabel, x + cardWidth - 28, y + 29, 0xFFCCCCCC);
        }
        if (bindingFeature != null) {
            context.drawText(textRenderer, "Press a key to bind, Esc to cancel",
                    panelX + 22, panelY + PANEL_HEIGHT - 43, 0xFFFFD36A, false);
        }
        context.drawText(textRenderer, "Right Shift: close menu", panelX + 20,
                panelY + PANEL_HEIGHT - 24, 0xFF888888, false);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        if (mouseX >= panelX + PANEL_WIDTH - 31 && mouseX <= panelX + PANEL_WIDTH - 10
                && mouseY >= panelY + 8 && mouseY <= panelY + 29) {
            close();
            return true;
        }

        int tabWidth = 88;
        int tabsX = panelX + 210;
        for (Feature.Category category : Feature.Category.values()) {
            int x = tabsX + category.ordinal() * (tabWidth + 4);
            if (mouseX >= x && mouseX <= x + tabWidth && mouseY >= panelY + 13 && mouseY <= panelY + 43) {
                selected = category;
                return true;
            }
        }

        List<Feature> features = visibleFeatures();
        int cardWidth = 210, cardHeight = 48, gapX = 10, gapY = 8;
        int startX = panelX + 18, startY = panelY + 84;
        for (int i = 0; i < features.size(); i++) {
            int x = startX + (i % 3) * (cardWidth + gapX);
            int y = startY + (i / 3) * (cardHeight + gapY);
            if (mouseX >= x + cardWidth - 54 && mouseX <= x + cardWidth - 4
                    && mouseY >= y + 20 && mouseY <= y + cardHeight) {
                bindingFeature = features.get(i);
                return true;
            }
            if (mouseX >= x && mouseX <= x + cardWidth - 54
                    && mouseY >= y && mouseY <= y + cardHeight) {
                Feature feature = features.get(i);
                feature.setEnabled(!feature.enabled());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingFeature != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                bindingFeature = null;
            } else {
                bindingFeature.setBindKey(keyCode);
                bindingFeature = null;
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (bindingFeature == null && !Character.isISOControl(chr) && searchQuery.length() < 32) {
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
