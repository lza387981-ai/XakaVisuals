package com.xaka.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public final class XakaMenuScreen extends Screen {
    private Feature.Category selected = Feature.Category.RENDER;
    private static final int PANEL_WIDTH = 690;
    private static final int PANEL_HEIGHT = 390;
    private int panelX;
    private int panelY;

    public XakaMenuScreen() {
        super(Text.literal("XakaVisuals V3"));
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_WIDTH) / 2;
        panelY = (height - PANEL_HEIGHT) / 2;
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

        context.drawText(textRenderer, selected.title(), panelX + 22, panelY + 62, 0xFFEAEAEA, true);
        List<Feature> features = FeatureRegistry.in(selected);
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
            context.drawCenteredTextWithShadow(textRenderer, "Bind", x + cardWidth - 28, y + 29, 0xFFCCCCCC);
        }
        context.drawText(textRenderer, "Right Shift: close menu", panelX + 20,
                panelY + PANEL_HEIGHT - 24, 0xFF888888, false);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int tabWidth = 88;
        int tabsX = panelX + 210;
        for (Feature.Category category : Feature.Category.values()) {
            int x = tabsX + category.ordinal() * (tabWidth + 4);
            if (mouseX >= x && mouseX <= x + tabWidth && mouseY >= panelY + 13 && mouseY <= panelY + 43) {
                selected = category;
                return true;
            }
        }

        List<Feature> features = FeatureRegistry.in(selected);
        int cardWidth = 210, cardHeight = 48, gapX = 10, gapY = 8;
        int startX = panelX + 18, startY = panelY + 84;
        for (int i = 0; i < features.size(); i++) {
            int x = startX + (i % 3) * (cardWidth + gapX);
            int y = startY + (i / 3) * (cardHeight + gapY);
            if (mouseX >= x && mouseX <= x + cardWidth - 54 && mouseY >= y && mouseY <= y + cardHeight) {
                Feature feature = features.get(i);
                feature.setEnabled(!feature.enabled());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
