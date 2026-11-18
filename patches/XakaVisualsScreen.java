package dev.xaka.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class XakaVisualsScreen extends Screen {
    private static final int BG = 0xF00C0F18;
    private static final int PANEL = 0xFF141925;
    private static final int CARD = 0xFF1B2231;
    private static final int CARD_HOVER = 0xFF242D40;
    private static final int TEXT = 0xFFF1F2F8;
    private static final int MUTED = 0xFF929BAF;

    private static final String[] CATEGORIES = {"Visuals", "HUD", "Effects", "Settings"};
    private static final String[] CATEGORY_HINTS = {
            "Small details. A cleaner game.",
            "Your information, your layout.",
            "Make every transition feel smooth.",
            "Tune XakaVisuals to your taste."
    };

    private int selectedCategory;
    private float openProgress;
    private long lastFrame;

    public XakaVisualsScreen() {
        super(Text.literal("XakaVisuals"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = System.currentTimeMillis();
        float frame = lastFrame == 0 ? 0.016f : Math.min(0.05f, (now - lastFrame) / 1000.0f);
        lastFrame = now;
        if (XakaVisualsClient.animations) openProgress = Math.min(1f, openProgress + frame * 5.5f);
        else openProgress = 1f;

        // Avoid Minecraft's background-blur shader: it can blur the whole UI on some mobile OpenGL drivers.
        // A simple translucent overlay keeps the game dimmed while all menu text remains sharp.
        context.fill(0, 0, width, height, 0x99060910);

        int panelW = Math.min(620, width - 28);
        int panelH = Math.min(390, height - 28);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int accent = XakaVisualsClient.accent();

        int slide = XakaVisualsClient.animations ? (int) ((1f - easeOut(openProgress)) * 16) : 0;
        y += slide;
        context.fill(x + 3, y + 4, x + panelW + 3, y + panelH + 4, 0x55000000);
        context.fill(x, y, x + panelW, y + panelH, BG);
        context.fill(x, y, x + panelW, y + 2, accent);
        context.fill(x, y + 2, x + panelW, y + panelH, PANEL);

        context.fill(x + 12, y + 14, x + 166, y + panelH - 14, 0xFF101520);
        context.fill(x + 12, y + 14, x + 14, y + panelH - 14, accent);
        context.fill(x + 25, y + 27, x + 49, y + 51, accent);
        context.drawCenteredTextWithShadow(textRenderer, "X", x + 37, y + 33, 0xFF10131C);
        context.drawTextWithShadow(textRenderer, "XAKA", x + 57, y + 27, TEXT);
        context.drawTextWithShadow(textRenderer, "VISUALS", x + 57, y + 39, accent);
        context.drawTextWithShadow(textRenderer, "CLIENT  /  1.21.4", x + 25, y + 61, MUTED);
        context.fill(x + 25, y + 79, x + 151, y + 80, 0xFF2B3343);

        int navY = y + 96;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int rowY = navY + i * 36;
            boolean active = selectedCategory == i;
            boolean hover = inside(mouseX, mouseY, x + 20, rowY - 4, x + 155, rowY + 26);
            if (active) {
                context.fill(x + 21, rowY - 4, x + 155, rowY + 26, 0xFF28243B);
                context.fill(x + 21, rowY - 4, x + 23, rowY + 26, accent);
            } else if (hover) {
                context.fill(x + 21, rowY - 4, x + 155, rowY + 26, 0xFF1A2130);
            }
            context.drawTextWithShadow(textRenderer, categoryIcon(i), x + 32, rowY + 5, active ? accent : MUTED);
            context.drawTextWithShadow(textRenderer, CATEGORIES[i], x + 51, rowY + 5, active ? TEXT : MUTED);
        }

        context.drawTextWithShadow(textRenderer, "XAKAVISUALS  /  SETTINGS", x + 25, y + panelH - 43, MUTED);
        context.drawTextWithShadow(textRenderer, "RIGHT SHIFT", x + 25, y + panelH - 29, accent);

        int contentX = x + 188;
        int right = x + panelW - 18;
        context.drawTextWithShadow(textRenderer, CATEGORIES[selectedCategory], contentX, y + 25, TEXT);
        context.drawTextWithShadow(textRenderer, CATEGORY_HINTS[selectedCategory], contentX, y + 40, MUTED);
        context.fill(contentX, y + 59, right, y + 60, 0xFF293142);
        renderCategory(context, mouseX, mouseY, contentX, right, y + 74, accent);

        context.drawTextWithShadow(textRenderer, "XakaVisuals  •  crafted for your game", contentX,
                y + panelH - 27, MUTED);
        context.drawTextWithShadow(textRenderer, "v1.0.0", right - 35, y + panelH - 27, MUTED);
        // Do not call super.render() here: it renders the screen background after our UI,
        // which can apply the blur shader over the entire frame on some Minecraft versions.
    }

    private void renderCategory(DrawContext c, int mx, int my, int left, int right, int top, int accent) {
        switch (selectedCategory) {
            case 0 -> {
                row(c, mx, my, left, right, top, "Clean interface", "A calm, distraction-free visual style", true, accent, false);
                row(c, mx, my, left, right, top + 49, "Smooth animations", "Animated menu transitions", XakaVisualsClient.animations, accent, true);
                row(c, mx, my, left, right, top + 98, "Compact HUD", "Reduce the height of HUD panels", XakaVisualsClient.compactHud, accent, true);
                row(c, mx, my, left, right, top + 147, "Center marker", "Decorative circle or square on the HUD", XakaVisualsClient.markerEnabled, accent, true);
            }
            case 1 -> {
                row(c, mx, my, left, right, top, "Watermark", "Show the XakaVisuals brand in-game", XakaVisualsClient.watermark, accent, true);
                row(c, mx, my, left, right, top + 49, "FPS counter", "Display your current frame rate", XakaVisualsClient.fps, accent, true);
                row(c, mx, my, left, right, top + 98, "Coordinates", "Show your current block position", XakaVisualsClient.coordinates, accent, true);
                row(c, mx, my, left, right, top + 147, "Marker shape", XakaVisualsClient.markerSquare ? "Square — click to use circle" : "Circle — click to use square", XakaVisualsClient.markerSquare, accent, false);
            }
            case 2 -> {
                infoCard(c, left, top, right, "Visual polish", "The interface uses subtle motion and a restrained accent palette.");
                row(c, mx, my, left, right, top + 72, "Menu animations", "Enable or disable interface motion", XakaVisualsClient.animations, accent, true);
                row(c, mx, my, left, right, top + 121, "Marker size: " + XakaVisualsClient.markerSize, "Click to cycle through marker sizes", false, accent, false);
            }
            case 3 -> {
                infoCard(c, left, top, right, "Accent color", "Choose a signature color for the XakaVisuals interface.");
                String[] names = {"Violet", "Mint", "Rose", "Amber", "Blue"};
                int index = Math.floorMod(XakaVisualsClient.accentIndex, names.length);
                button(c, mx, my, left, top + 78, 118, 31, "‹  Previous", false, accent);
                button(c, mx, my, left + 126, top + 78, 118, 31, names[index] + "  ›", true, accent);
                row(c, mx, my, left, right, top + 126, "Compact HUD", "Use smaller in-game HUD panels", XakaVisualsClient.compactHud, accent, true);
                contextText(c, left, top + 188, "Changes apply instantly. No restart needed.");
            }
        }
    }

    private void row(DrawContext c, int mx, int my, int left, int right, int y, String title,
                     String description, boolean enabled, int accent, boolean toggle) {
        boolean hover = inside(mx, my, left, y, right, y + 42);
        c.fill(left, y, right, y + 42, hover ? CARD_HOVER : CARD);
        c.drawTextWithShadow(textRenderer, title, left + 12, y + 8, TEXT);
        c.drawTextWithShadow(textRenderer, description, left + 12, y + 22, MUTED);
        if (toggle) {
            int sx = right - 38;
            int sy = y + 13;
            c.fill(sx, sy, sx + 26, sy + 15, enabled ? accent : 0xFF414A5D);
            int knobX = enabled ? sx + 14 : sx + 2;
            c.fill(knobX, sy + 2, knobX + 11, sy + 13, 0xFFFFFFFF);
        } else {
            c.fill(right - 28, y + 16, right - 17, y + 27, accent);
        }
    }

    private void infoCard(DrawContext c, int left, int top, int right, String title, String body) {
        c.fill(left, top, right, top + 60, CARD);
        c.fill(left, top, left + 2, top + 60, XakaVisualsClient.accent());
        c.drawTextWithShadow(textRenderer, title, left + 12, top + 10, TEXT);
        c.drawTextWithShadow(textRenderer, body, left + 12, top + 28, MUTED);
    }

    private void button(DrawContext c, int mx, int my, int x, int y, int w, int h,
                        String label, boolean selected, int accent) {
        boolean hover = inside(mx, my, x, y, x + w, y + h);
        c.fill(x, y, x + w, y + h, selected ? 0xFF29243D : (hover ? CARD_HOVER : CARD));
        if (selected) c.fill(x, y, x + w, y + 2, accent);
        c.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + 11, selected ? accent : TEXT);
    }

    private void contextText(DrawContext c, int x, int y, String value) {
        c.drawTextWithShadow(textRenderer, value, x, y, MUTED);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int panelW = Math.min(620, width - 28);
        int panelH = Math.min(390, height - 28);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int navY = y + 96;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int rowY = navY + i * 36;
            if (inside(mouseX, mouseY, x + 20, rowY - 4, x + 155, rowY + 26)) {
                selectedCategory = i;
                return true;
            }
        }

        int left = x + 188;
        int right = x + panelW - 18;
        int top = y + 74;
        if (selectedCategory == 0) {
            if (hitRow(mouseX, mouseY, left, right, top + 49)) XakaVisualsClient.animations = !XakaVisualsClient.animations;
            if (hitRow(mouseX, mouseY, left, right, top + 98)) XakaVisualsClient.compactHud = !XakaVisualsClient.compactHud;
            if (hitRow(mouseX, mouseY, left, right, top + 147)) XakaVisualsClient.markerEnabled = !XakaVisualsClient.markerEnabled;
        } else if (selectedCategory == 1) {
            if (hitRow(mouseX, mouseY, left, right, top)) XakaVisualsClient.watermark = !XakaVisualsClient.watermark;
            if (hitRow(mouseX, mouseY, left, right, top + 49)) XakaVisualsClient.fps = !XakaVisualsClient.fps;
            if (hitRow(mouseX, mouseY, left, right, top + 98)) XakaVisualsClient.coordinates = !XakaVisualsClient.coordinates;
            if (hitRow(mouseX, mouseY, left, right, top + 147)) XakaVisualsClient.markerSquare = !XakaVisualsClient.markerSquare;
        } else if (selectedCategory == 2) {
            if (hitRow(mouseX, mouseY, left, right, top + 72)) XakaVisualsClient.animations = !XakaVisualsClient.animations;
            if (hitRow(mouseX, mouseY, left, right, top + 121)) XakaVisualsClient.markerSize = XakaVisualsClient.markerSize >= 12 ? 3 : XakaVisualsClient.markerSize + 3;
        } else if (selectedCategory == 3) {
            if (inside(mouseX, mouseY, left, top + 78, left + 118, top + 109))
                XakaVisualsClient.accentIndex = Math.floorMod(XakaVisualsClient.accentIndex - 1, 5);
            if (inside(mouseX, mouseY, left + 126, top + 78, left + 244, top + 109))
                XakaVisualsClient.accentIndex = Math.floorMod(XakaVisualsClient.accentIndex + 1, 5);
            if (hitRow(mouseX, mouseY, left, right, top + 126)) XakaVisualsClient.compactHud = !XakaVisualsClient.compactHud;
        }
        return true;
    }

    private boolean hitRow(double mx, double my, int left, int right, int y) {
        return inside(mx, my, left, y, right, y + 42);
    }

    private boolean inside(double mx, double my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }

    private String categoryIcon(int index) {
        return switch (index) { case 0 -> "✦"; case 1 -> "▤"; case 2 -> "◈"; default -> "⚙"; };
    }

    private float easeOut(float t) { return 1f - (float) Math.pow(1f - t, 3); }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Right Shift toggles the menu closed, matching the key used to open it.
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
