package dev.xaka.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class XakaVisualsScreen extends Screen {
    private final List<Toggle> toggles = new ArrayList<>();
    private int scroll;
    private String activeTab = "Visual";

    public XakaVisualsScreen() {
        super(Text.literal("XakaVisuals.V2"));
        toggles.add(new Toggle("Watermark", "Compact XAKA corner logo", () -> VisualSettings.watermark, v -> VisualSettings.watermark = v));
        toggles.add(new Toggle("FPS counter", "Show current client FPS", () -> VisualSettings.fpsCounter, v -> VisualSettings.fpsCounter = v));
        toggles.add(new Toggle("Player halo", "Decorative local visual effect", () -> VisualSettings.playerHalo, v -> VisualSettings.playerHalo = v));
        toggles.add(new Toggle("Target marker", "Local marker display option", () -> VisualSettings.targetMarker, v -> VisualSettings.targetMarker = v));
        toggles.add(new Toggle("Mini pets", "Decorative cosmetic companions", () -> VisualSettings.cosmeticPets, v -> VisualSettings.cosmeticPets = v));
        toggles.add(new Toggle("Hand position", "Enable first-person hand adjustment UI", () -> VisualSettings.handPosition, v -> VisualSettings.handPosition = v));
        toggles.add(new Toggle("Safe FPS mode", "Reduce optional visual workload only", () -> VisualSettings.safeFpsBoost, v -> VisualSettings.safeFpsBoost = v));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int w = width, h = height;
        ctx.fill(0, 0, w, h, 0xB9000000);
        int panelW = Math.min(520, w - 36);
        int panelH = Math.min(340, h - 36);
        int left = (w - panelW) / 2, top = (h - panelH) / 2;
        ctx.fill(left, top, left + panelW, top + panelH, 0xF0121420);
        ctx.fill(left, top, left + panelW, top + 2, 0xFF8B5CF6);
        ctx.fill(left, top + 2, left + 116, top + panelH, 0xFF191B2A);
        ctx.drawTextWithShadow(textRenderer, Text.literal("XAKA").styled(s -> s.withColor(0xFFC4B5FD)), left + 18, top + 18, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "VISUALS  /  V2", left + 18, top + 34, 0xFF9CA3AF);
        ctx.drawTextWithShadow(textRenderer, "RIGHT SHIFT  •  CLOSE", left + panelW - 150, top + 22, 0xFF777F94);

        String[] tabs = {"Visual", "Cosmetics", "HUD", "CheatV2"};
        for (int i = 0; i < tabs.length; i++) {
            int ty = top + 72 + i * 30;
            boolean selected = activeTab.equals(tabs[i]);
            if (selected) ctx.fill(left + 10, ty - 5, left + 106, ty + 19, 0xFF34304F);
            ctx.drawTextWithShadow(textRenderer, (selected ? "› " : "  ") + tabs[i], left + 20, ty, selected ? 0xFFE9D5FF : 0xFF9CA3AF);
        }

        ctx.drawTextWithShadow(textRenderer, activeTab.toUpperCase() + " SETTINGS", left + 136, top + 62, 0xFFE5E7EB);
        ctx.drawTextWithShadow(textRenderer, "Personalize your client experience", left + 136, top + 78, 0xFF8B93A7);

        int rowY = top + 108 - scroll;
        for (Toggle toggle : toggles) {
            if (!visibleInTab(toggle)) continue;
            if (rowY > top + 92 && rowY < top + panelH - 16) {
                ctx.fill(left + 132, rowY - 7, left + panelW - 16, rowY + 34, 0xFF1D2030);
                ctx.drawTextWithShadow(textRenderer, toggle.name, left + 144, rowY, 0xFFF3F4F6);
                ctx.drawTextWithShadow(textRenderer, toggle.description, left + 144, rowY + 14, 0xFF929AAF);
                int bx = left + panelW - 66, by = rowY + 3;
                ctx.fill(bx, by, bx + 34, by + 16, toggle.get() ? 0xFF7655C7 : 0xFF45495A);
                ctx.fill(toggle.get() ? bx + 19 : bx + 3, by + 3, toggle.get() ? bx + 31 : bx + 15, by + 13, 0xFFFFFFFF);
            }
            rowY += 49;
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    private boolean visibleInTab(Toggle t) {
        return switch (activeTab) {
            case "HUD" -> t.name.equals("Watermark") || t.name.equals("FPS counter");
            case "Cosmetics" -> t.name.equals("Mini pets");
            case "CheatV2" -> t.name.equals("Player halo") || t.name.equals("Target marker");
            default -> !t.name.equals("Mini pets") && !t.name.equals("Watermark") && !t.name.equals("FPS counter");
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int panelW = Math.min(520, width - 36), panelH = Math.min(340, height - 36);
        int left = (width - panelW) / 2, top = (height - panelH) / 2;
        String[] tabs = {"Visual", "Cosmetics", "HUD", "CheatV2"};
        for (int i = 0; i < tabs.length; i++) {
            int ty = top + 72 + i * 30;
            if (mouseX >= left + 8 && mouseX <= left + 112 && mouseY >= ty - 7 && mouseY <= ty + 22) {
                activeTab = tabs[i]; scroll = 0; return true;
            }
        }
        int rowY = top + 108 - scroll;
        for (Toggle toggle : toggles) {
            if (!visibleInTab(toggle)) continue;
            if (mouseX >= left + 132 && mouseX <= left + panelW - 16 && mouseY >= rowY - 7 && mouseY <= rowY + 34) {
                toggle.set(!toggle.get()); return true;
            }
            rowY += 49;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0, Math.min(220, scroll - (int)(verticalAmount * 24)));
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }

    private record Toggle(String name, String description, Getter getter, Setter setter) {
        boolean get() { return getter.get(); }
        void set(boolean value) { setter.set(value); }
    }
    @FunctionalInterface private interface Getter { boolean get(); }
    @FunctionalInterface private interface Setter { void set(boolean value); }
}
