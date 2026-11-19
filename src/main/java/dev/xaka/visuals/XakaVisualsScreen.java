package dev.xaka.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public final class XakaVisualsScreen extends Screen {
    private static final int PANEL_W = 520;
    private static final int PANEL_H = 340;
    private static final List<String> TABS = List.of("Visual", "Cosmetics", "Animations", "HUD", "Client");

    private String activeTab = "Visual";
    private int scroll;

    public XakaVisualsScreen() {
        super(Text.literal("XakaVisuals.V2"));
    }

    private List<Toggle> visibleToggles() {
        return switch (activeTab) {
            case "Animations" -> List.of(
                    new Toggle("Emotes", "Character emote and animation presets", "", false),
                    new Toggle("Movement animations", "Additional cosmetic movement styles", "", false)
            );
            case "HUD" -> List.of(
                    new Toggle("Watermark", "Compact XAKA logo in the corner", "watermark", true),
                    new Toggle("FPS counter", "Display current client FPS", "fpsCounter", true)
            );
            case "Cosmetics" -> List.of(
                    new Toggle("Mini pets", "Decorative companion system", "", false),
                    new Toggle("Emotions", "Character animation and emote system", "", false)
            );
            case "Client" -> List.of(
                    new Toggle("First-person hand controls", "Hand position customization", "", false),
                    new Toggle("Safe FPS mode", "Reversible visual workload reduction", "", false)
            );
            default -> List.of(
                    new Toggle("Fullbright", "World brightness customization", "", false),
                    new Toggle("Player halo", "Player highlight visual", "", false),
                    new Toggle("Target marker", "Configurable target marker visual", "", false),
                    new Toggle("Hit particles", "Custom hit particle effects", "", false),
                    new Toggle("Motion blur", "Motion blur visual effect", "", false),
                    new Toggle("Zoom", "Camera zoom control", "", false),
                    new Toggle("View model", "First-person item positioning", "", false)
            );
        };
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xB9000000);
        int panelW = Math.min(PANEL_W, width - 24);
        int panelH = Math.min(PANEL_H, height - 24);
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        ctx.fill(left, top, left + panelW, top + panelH, 0xF0121420);
        ctx.fill(left, top, left + panelW, top + 3, 0xFF8B5CF6);
        ctx.fill(left, top + 3, left + 118, top + panelH, 0xFF171925);
        ctx.drawTextWithShadow(textRenderer, Text.literal("XakaVisuals").styled(s -> s.withColor(0xFFC4B5FD)),
                left + 17, top + 17, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "VISUAL CLIENT", left + 17, top + 34, 0xFF9298AA);
        ctx.drawTextWithShadow(textRenderer, "RIGHT SHIFT  •  ESC TO CLOSE",
                left + panelW - 174, top + 20, 0xFF777F94);

        for (int i = 0; i < TABS.size(); i++) {
            String tab = TABS.get(i);
            int y = top + 72 + i * 31;
            boolean selected = activeTab.equals(tab);
            if (selected) {
                ctx.fill(left + 9, y - 5, left + 109, y + 20, 0xFF332B4D);
                ctx.fill(left + 9, y - 5, left + 12, y + 20, 0xFF9B7BFF);
            }
            ctx.drawTextWithShadow(textRenderer, tab, left + 22, y + 3,
                    selected ? 0xFFE9DDFF : 0xFF9CA3AF);
        }

        int contentX = left + 139;
        ctx.drawTextWithShadow(textRenderer, activeTab.toUpperCase() + " SETTINGS",
                contentX, top + 59, 0xFFF3F4F6);
        ctx.drawTextWithShadow(textRenderer, "Feature port status",
                contentX, top + 76, 0xFF8E95A8);

        List<Toggle> toggles = visibleToggles();
        int maxScroll = Math.max(0, toggles.size() * 49 - (panelH - 140));
        scroll = Math.min(scroll, maxScroll);
        int rowY = top + 111 - scroll;
        for (Toggle toggle : toggles) {
            if (rowY + 35 >= top + 96 && rowY <= top + panelH - 26) {
                ctx.fill(contentX - 8, rowY - 7, left + panelW - 16, rowY + 34, 0xFF1E2230);
                ctx.drawTextWithShadow(textRenderer, toggle.title, contentX + 7, rowY,
                        0xFFF3F4F6);
                ctx.drawTextWithShadow(textRenderer, toggle.description, contentX + 7, rowY + 15,
                        0xFF929AAF);
                if (toggle.ready) {
                    boolean enabled = toggle.enabled();
                    int sx = left + panelW - 68;
                    int sy = rowY + 3;
                    ctx.fill(sx, sy, sx + 38, sy + 17, enabled ? 0xFF7655C7 : 0xFF45495A);
                    ctx.fill(enabled ? sx + 22 : sx + 3, sy + 3,
                            enabled ? sx + 35 : sx + 16, sy + 14, 0xFFFFFFFF);
                } else {
                    ctx.drawTextWithShadow(textRenderer, "PORTING",
                            left + panelW - 75, rowY + 5, 0xFFFFC978);
                }
            }
            rowY += 49;
        }

        ctx.drawTextWithShadow(textRenderer, "Only implemented features can be toggled",
                left + 17, top + panelH - 19, 0xFF777F94);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int panelW = Math.min(PANEL_W, width - 24);
        int panelH = Math.min(PANEL_H, height - 24);
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        for (int i = 0; i < TABS.size(); i++) {
            int y = top + 72 + i * 31;
            if (mouseX >= left + 9 && mouseX <= left + 109
                    && mouseY >= y - 5 && mouseY <= y + 20) {
                activeTab = TABS.get(i);
                scroll = 0;
                return true;
            }
        }

        List<Toggle> toggles = visibleToggles();
        int rowY = top + 111 - scroll;
        for (Toggle toggle : toggles) {
            if (toggle.ready && mouseX >= left + 131 && mouseX <= left + panelW - 16
                    && mouseY >= rowY - 7 && mouseY <= rowY + 34) {
                toggle.flip();
                VisualSettings.save();
                return true;
            }
            rowY += 49;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, visibleToggles().size() * 49 - (Math.min(PANEL_H, height - 24) - 140));
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (verticalAmount * 24)));
        return true;
    }

    @Override
    public void close() {
        VisualSettings.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private record Toggle(String title, String description, String id, boolean ready) {
        boolean enabled() {
            return switch (id) {
                case "watermark" -> VisualSettings.watermark;
                case "fpsCounter" -> VisualSettings.fpsCounter;
                default -> false;
            };
        }

        void flip() {
            switch (id) {
                case "watermark" -> VisualSettings.watermark = !VisualSettings.watermark;
                case "fpsCounter" -> VisualSettings.fpsCounter = !VisualSettings.fpsCounter;
            }
        }
    }
}
