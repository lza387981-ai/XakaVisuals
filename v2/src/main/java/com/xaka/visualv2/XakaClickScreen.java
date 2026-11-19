package com.xaka.visualv2;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

public final class XakaClickScreen extends Screen {
    private static final Map<String, Boolean> MODULE_STATES = new LinkedHashMap<>();
    private static final String[] CATEGORIES = {"Visual", "HUD", "Utility", "Cosmetics"};
    private String selectedCategory = "Visual";

    public XakaClickScreen() {
        super(Text.literal("XAKA VISUAL.V2"));
    }

    @Override
    protected void init() {
        clearChildren();
        int panelLeft = Math.max(18, (width - 520) / 2);
        int panelTop = Math.max(18, (height - 330) / 2);

        for (int i = 0; i < CATEGORIES.length; i++) {
            final String category = CATEGORIES[i];
            addDrawableChild(ButtonWidget.builder(Text.literal(category), button -> {
                selectedCategory = category;
                clearAndInit();
            }).dimensions(panelLeft + 14, panelTop + 62 + i * 38, 112, 30).build());
        }

        String[] modules = selectedCategory.equals("Visual")
                ? new String[]{"Target Marker", "China Hat", "Hit Particles", "Fullbright", "Item Physics", "Motion Blur"}
                : selectedCategory.equals("HUD")
                ? new String[]{"Watermark", "Coordinates", "Armor Status", "Keystrokes", "Target Info"}
                : selectedCategory.equals("Utility")
                ? new String[]{"Auto Sprint", "Zoom", "Name Protect", "Chat Helper"}
                : new String[]{"Mini Pet", "Emotes", "Cosmetic Trail"};

        for (int i = 0; i < modules.length; i++) {
            String module = modules[i];
            boolean enabled = MODULE_STATES.getOrDefault(module, false);
            int col = i % 2;
            int row = i / 2;
            int x = panelLeft + 150 + col * 174;
            int y = panelTop + 66 + row * 72;
            addDrawableChild(ButtonWidget.builder(Text.literal(module + "  " + (enabled ? "ON" : "OFF")), button -> {
                boolean next = !MODULE_STATES.getOrDefault(module, false);
                MODULE_STATES.put(module, next);
                clearAndInit();
            }).dimensions(x, y, 160, 32).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xB8000000);
        int left = Math.max(18, (width - 520) / 2);
        int top = Math.max(18, (height - 330) / 2);
        context.fill(left, top, left + 520, top + 330, 0xFF101522);
        context.fill(left, top, left + 520, top + 3, 0xFF39BDF8);
        context.fill(left + 138, top + 48, left + 140, top + 316, 0xFF293449);
        context.drawText(textRenderer, Text.literal("XAKA VISUAL.V2"), left + 16, top + 16, 0xFFFFFFFF, false);
        context.drawText(textRenderer, Text.literal("CLIENT EDITION  •  1.21.4"), left + 16, top + 32, 0xFF8FA4C2, false);
        context.drawText(textRenderer, Text.literal(selectedCategory.toUpperCase()), left + 152, top + 43, 0xFF39BDF8, false);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
