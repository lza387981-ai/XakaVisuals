package dev.xaka.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class XakaHudPositionScreen extends Screen {
    private static final int CARD_W = 136;
    private static final int CARD_H = 32;
    private boolean dragging;
    private int offsetX;
    private int offsetY;

    public XakaHudPositionScreen() { super(Text.literal("Move HP HUD")); }

    private int panelX() {
        return XakaVisualsClient.enemyHpX < 0 ? (width - CARD_W) / 2 : XakaVisualsClient.enemyHpX;
    }
    private int panelY() {
        return XakaVisualsClient.enemyHpY < 0 ? Math.max(8, height / 2 - 112) : XakaVisualsClient.enemyHpY;
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, width, height, 0x99060910);
        c.drawCenteredTextWithShadow(textRenderer, "MOVE ENEMY HP HUD", width / 2, 22, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, "Drag the panel with your mouse", width / 2, 38, 0xFFB8C1D4);
        int x = panelX(), y = panelY();
        c.fill(x, y, x + CARD_W, y + CARD_H, 0xE9101420);
        c.fill(x, y, x + 2, y + CARD_H, XakaVisualsClient.accent());
        c.drawTextWithShadow(textRenderer, "Target Player", x + 7, y + 4, 0xFFF2F3F8);
        c.drawTextWithShadow(textRenderer, "18.0 / 20.0 HP", x + 7, y + 17, 0xFFE2E7F0);
        c.fill(x + 83, y + 23, x + 128, y + 27, 0xFF343B49);
        c.fill(x + 83, y + 23, x + 124, y + 27, 0xFF55D68B);
        int bx = width / 2 - 58, by = height - 54;
        c.fill(bx, by, bx + 116, by + 28, 0xFF242D40);
        c.drawCenteredTextWithShadow(textRenderer, "Reset position", width / 2, by + 9, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, "Position: " + x + ", " + y, width / 2, height - 17, 0xFF929BAF);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        int bx = width / 2 - 58, by = height - 54;
        if (mx >= bx && mx <= bx + 116 && my >= by && my <= by + 28) {
            XakaVisualsClient.enemyHpX = -1;
            XakaVisualsClient.enemyHpY = -1;
            dragging = false;
            return true;
        }
        int x = panelX(), y = panelY();
        if (mx >= x && mx <= x + CARD_W && my >= y && my <= y + CARD_H) {
            dragging = true;
            offsetX = (int) mx - x;
            offsetY = (int) my - y;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging && button == 0) {
            XakaVisualsClient.enemyHpX = Math.max(0, Math.min(width - CARD_W, (int) mx - offsetX));
            XakaVisualsClient.enemyHpY = Math.max(0, Math.min(height - CARD_H, (int) my - offsetY));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
