package dev.xaka.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class XakaVisualsScreen extends Screen {
    private static final int BG = 0xF00C0F18;
    private static final int PANEL = 0xFF141925;
    private static final int CARD = 0xFF1B2231;
    private static final int CARD_HOVER = 0xFF242D40;
    private static final int TEXT = 0xFFF1F2F8;
    private static final int MUTED = 0xFF929BAF;

    private static final String[] CATEGORIES = {"Visuals", "HUD", "Effects", "Settings", "Hand", "Animations", "MUSIC", "Cosmetics", "Utilities", "World"};
    private static final String[] CATEGORY_HINTS = {
            "Small details. A cleaner game.",
            "Your information, your layout.",
            "Make every transition feel smooth.",
            "Tune XakaVisuals to your taste.",
            "Adjust your first-person hand and held item.",
            "Play a local character emote.",
            "Your MP3 playlist and floating mini-player.",
            "Volumetric capes and 3D hats for your character.",
            "Small client-side helpers and status alerts.",
            "Local visual time-of-day controls. Server time remains unchanged."
    };

    private int selectedCategory;
    private float openProgress;
    private long lastFrame;
    private int activeSlider = -1;
    private int navScrollOffset = 0;
    private int contentScrollOffset = 0;
    private boolean navDragging = false;
    private static final List<CursorSparkle> CURSOR_SPARKLES = new ArrayList<>();
    private double lastMouseX, lastMouseY;
    private boolean hasLastMouse;
    private long lastSparkleSpawn;

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
        int panelH = Math.min(440, height - 28);
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

        int navViewportTop = y + 85;
        int navViewportBottom = y + panelH - 55;
        int navStep = 64;
        int navRowHeight = Math.min(30, navStep - 2);
        int navY = navViewportTop + 4;
        int navMaxScroll = Math.max(0, CATEGORIES.length * navStep - (navViewportBottom - navViewportTop));
        navScrollOffset = Math.max(0, Math.min(navScrollOffset, navMaxScroll));
        context.enableScissor(x + 20, navViewportTop, x + 155, navViewportBottom);
        for (int i = 0; i < CATEGORIES.length; i++) {
            int rowTop = navY + i * navStep - navScrollOffset;
            int rowBottom = rowTop + navRowHeight;
            boolean active = selectedCategory == i;
            boolean hover = inside(mouseX, mouseY, x + 20, rowTop, x + 155, rowBottom);
            if (active) {
                context.fill(x + 21, rowTop, x + 155, rowBottom, 0xFF28243B);
                context.fill(x + 21, rowTop, x + 23, rowBottom, accent);
            } else if (hover) {
                context.fill(x + 21, rowTop, x + 155, rowBottom, 0xFF1A2130);
            }
            int textY = rowTop + Math.max(3, (navRowHeight - 9) / 2);
            context.drawTextWithShadow(textRenderer, categoryIcon(i), x + 32, textY, active ? accent : MUTED);
            context.drawTextWithShadow(textRenderer, CATEGORIES[i], x + 51, textY, active ? TEXT : MUTED);
        }
        context.disableScissor();

        context.drawTextWithShadow(textRenderer, "XAKAVISUALS  /  SETTINGS", x + 25, y + panelH - 43, MUTED);
        context.drawTextWithShadow(textRenderer, "RIGHT SHIFT", x + 25, y + panelH - 29, accent);

        int contentX = x + 188;
        int right = x + panelW - 18;
        context.drawTextWithShadow(textRenderer, CATEGORIES[selectedCategory], contentX, y + 25, TEXT);
        context.drawTextWithShadow(textRenderer, CATEGORY_HINTS[selectedCategory], contentX, y + 40, MUTED);
        context.fill(contentX, y + 59, right, y + 60, 0xFF293142);
        // Clip category cards to the panel so no control can spill outside on short mobile screens.
        int contentViewportTop = y + 68;
        int contentViewportBottom = y + panelH - 48;
        int contentMaxScroll = Math.max(0, categoryContentHeight() - (contentViewportBottom - contentViewportTop));
        contentScrollOffset = Math.max(0, Math.min(contentScrollOffset, contentMaxScroll));
        context.enableScissor(contentX, contentViewportTop, right, contentViewportBottom);
        renderCategory(context, mouseX, mouseY, contentX, right, y + 74 - contentScrollOffset, accent);
        context.disableScissor();

        if (selectedCategory != 2 && selectedCategory != 3) {
            context.drawTextWithShadow(textRenderer, "XakaVisuals  •  crafted for your game", contentX,
                    y + panelH - 27, MUTED);
            context.drawTextWithShadow(textRenderer, "v1.0.0", right - 35, y + panelH - 27, MUTED);
        }
        renderCursorSparkles(context);
        // Do not call super.render() here: it renders the screen background after our UI,
        // which can apply the blur shader over the entire frame on some Minecraft versions.
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        long now = System.nanoTime();
        double dx = mouseX - lastMouseX, dy = mouseY - lastMouseY;
        if (XakaVisualsClient.mouseParticles && hasLastMouse && dx * dx + dy * dy >= 9.0
                && now - lastSparkleSpawn >= 10_000_000L && CURSOR_SPARKLES.size() < 72) {
            double speed = Math.sqrt(dx * dx + dy * dy);
            double spread = Math.min(1.8, 0.35 + speed * 0.035);
            CURSOR_SPARKLES.add(new CursorSparkle(mouseX, mouseY,
                    (Math.random() - 0.5) * spread, -0.25 - Math.random() * spread,
                    XakaVisualsClient.mouseParticleSize, now));
            lastSparkleSpawn = now;
        }
        lastMouseX = mouseX; lastMouseY = mouseY; hasLastMouse = true;
    }

    private void renderCursorSparkles(DrawContext context) {
        if (!XakaVisualsClient.mouseParticles) { CURSOR_SPARKLES.clear(); return; }
        long now = System.nanoTime();
        Iterator<CursorSparkle> iterator = CURSOR_SPARKLES.iterator();
        while (iterator.hasNext()) {
            CursorSparkle sparkle = iterator.next();
            float age = (now - sparkle.startedAt) / 1_000_000.0f;
            float progress = age / 680.0f;
            if (progress >= 1.0f) { iterator.remove(); continue; }
            int alpha = Math.max(0, Math.min(230, Math.round(230.0f * (1.0f - progress))));
            int color = (alpha << 24) | (XakaVisualsClient.accent() & 0x00FFFFFF);
            int x = Math.round((float)(sparkle.x + sparkle.vx * age * 0.055));
            int y = Math.round((float)(sparkle.y + sparkle.vy * age * 0.055));
            int size = Math.max(2, Math.round(sparkle.size * (1.0f - progress * 0.35f)));
            context.fill(x - size / 2, y - 1, x + size / 2 + 1, y + 2, color);
            context.fill(x - 1, y - size / 2, x + 2, y + size / 2 + 1, color);
            context.fill(x - 1, y - 1, x + 1, y + 1, 0xFFFFFFFF);
        }
    }

    private static final class CursorSparkle {
        private final double x, y, vx, vy;
        private final int size;
        private final long startedAt;
        private CursorSparkle(double x, double y, double vx, double vy, int size, long startedAt) {
            this.x=x; this.y=y; this.vx=vx; this.vy=vy; this.size=size; this.startedAt=startedAt;
        }
    }

    private void renderCategory(DrawContext c, int mx, int my, int left, int right, int top, int accent) {
        switch (selectedCategory) {
            case 0 -> {
                row(c, mx, my, left, right, top, "Clean interface", "A calm, distraction-free visual style", true, accent, false);
                row(c, mx, my, left, right, top + 49, "Smooth animations", "Animated menu transitions", XakaVisualsClient.animations, accent, true);
                row(c, mx, my, left, right, top + 98, "Compact HUD", "Reduce the height of HUD panels", XakaVisualsClient.compactHud, accent, true);
                row(c, mx, my, left, right, top + 147, "Target ESP", "A lightweight marker on the targeted player’s torso", XakaVisualsClient.bodyMarkerEnabled, accent, true);
                row(c, mx, my, left, right, top + 196, "Floating pet", "Toggle your local decorative companion", XakaVisualsClient.petEnabled, accent, true);
                row(c, mx, my, left, right, top + 245, "Pet: " + XakaVisualsClient.petName(), "Click right side to switch; left side changes size", false, accent, false);
                row(c, mx, my, left, right, top + 294, "Frame color: " + XakaVisualsClient.targetFrameColorName(), "Tap to cycle through 8 colors", false, XakaVisualsClient.targetFrameColor(), false);
                row(c, mx, my, left, right, top + 343, "Mouse particles", "Small sparkles follow your cursor in this menu", XakaVisualsClient.mouseParticles, accent, true);
                sliderRow(c, mx, my, left, right, top + 392, "Sparkle size", XakaVisualsClient.mouseParticleSize, 2, 10, accent, 16);
            }
            case 1 -> {
                row(c, mx, my, left, right, top, "Watermark", "Show the XakaVisuals brand in-game", XakaVisualsClient.watermark, accent, true);
                row(c, mx, my, left, right, top + 49, "FPS counter", "Display your current frame rate", XakaVisualsClient.fps, accent, true);
                row(c, mx, my, left, right, top + 98, "Coordinates", "Show your current block position", XakaVisualsClient.coordinates, accent, true);
                row(c, mx, my, left, right, top + 147, "Target ESP shape", XakaVisualsClient.markerSquare ? "Square — click to use circle" : "Circle — click to use square", XakaVisualsClient.markerSquare, accent, false);
                row(c, mx, my, left, right, top + 196, "FPS Boost", "Minimal particles; restores your previous setting", XakaVisualsClient.fpsBoost, accent, true);
                row(c, mx, my, left, right, top + 245, "Enemy HP", "Show the targeted player's health", XakaVisualsClient.enemyHpEnabled, accent, true);
                row(c, mx, my, left, right, top + 294, "Move HP HUD", "Drag the health panel anywhere on screen", false, accent, false);
                row(c, mx, my, left, right, top + 882, "Health indicator", "Show your own HP and absorption bar", XakaVisualsClient.selfHealthHud, accent, true);
                row(c, mx, my, left, right, top + 343, "Fullbright", "Temporarily raise gamma; restores the previous value", VisualSettings.fullbright, accent, true);
                row(c, mx, my, left, right, top + 392, "Custom crosshair", "Draw a compact accent-colored crosshair", XakaVisualsClient.customCrosshair, accent, true);
                sliderRow(c, mx, my, left, right, top + 441, "Crosshair size", XakaVisualsClient.crosshairSize, 2, 12, accent, 13);
                row(c, mx, my, left, right, top + 490, "Cooldowns", "Show your attack cooldown percentage", XakaVisualsClient.cooldownHud, accent, true);
                sliderRow(c, mx, my, left, right, top + 539, "Crosshair gap", XakaVisualsClient.crosshairGap, 0, 12, accent, 20);
                sliderRow(c, mx, my, left, right, top + 588, "Crosshair thickness", XakaVisualsClient.crosshairThickness, 1, 4, accent, 21);
                row(c, mx, my, left, right, top + 637, "Center dot", "Show a small dot in the middle", XakaVisualsClient.crosshairDot, accent, true);
                sliderRow(c, mx, my, left, right, top + 686, "Dot size", XakaVisualsClient.crosshairDotSize, 1, 6, accent, 22);
                row(c, mx, my, left, right, top + 735, "Crosshair outline", "Add a dark edge for contrast", XakaVisualsClient.crosshairOutline, accent, true);
                sliderRow(c, mx, my, left, right, top + 784, "Outline width", XakaVisualsClient.crosshairOutlineWidth, 1, 3, accent, 23);
                row(c, mx, my, left, right, top + 833, "Dropped item tilt", "Add a gentle local tilt to floating item models", XakaVisualsClient.droppedItemTilt, accent, true);
            }
            case 2 -> {
                infoCard(c, left, top, right, "Player cosmetics", "Local decorative effects attached to your own character.");
                row(c, mx, my, left, right, top + 72, "Halo above head", "Toggle the decorative ring above your player", XakaVisualsClient.haloEnabled, accent, true);
                sliderRow(c, mx, my, left, right, top + 121, "Halo size", XakaVisualsClient.haloSize, 4, 18, accent, 0);
                row(c, mx, my, left, right, top + 170, "Target ESP", "Toggle the marker on the targeted player's torso", XakaVisualsClient.bodyMarkerEnabled, accent, true);
                row(c, mx, my, left, right, top + 219, XakaVisualsClient.markerSquare ? "Target shape: square" : "Target shape: circle", "Click to switch between square and circle", false, accent, false);
                sliderRow(c, mx, my, left, right, top + 268, "Target ESP size", XakaVisualsClient.markerSize, 2, 14, accent, 1);
                row(c, mx, my, left, right, top + 317, "Hit particles", "Show a small burst when the targeted entity takes damage", XakaVisualsClient.hitParticles, accent, true);
                row(c, mx, my, left, right, top + 366, "Jump circle", "Create a particle ring when you land from a jump", XakaVisualsClient.jumpCircle, accent, true);
                row(c, mx, my, left, right, top + 415, "Movement trails", "Leave a subtle particle trail while airborne", XakaVisualsClient.playerTrails, accent, true);
                row(c, mx, my, left, right, top + 464, "Jump effect", "Draw two expanding rings when you take off", XakaVisualsClient.jumpEffect, accent, true);
                sliderRow(c, mx, my, left, right, top + 513, "Wave radius", XakaVisualsClient.jumpEffectRadius, 5, 100, accent, 11);
                sliderRow(c, mx, my, left, right, top + 562, "Wave duration", XakaVisualsClient.jumpEffectDuration, 300, 2000, accent, 12);
                row(c, mx, my, left, right, top + 611, "Jump cubes", "Small rotating cubes burst around you on takeoff", XakaVisualsClient.jumpCubes, accent, true);
                sliderRow(c, mx, my, left, right, top + 660, "Cube count", XakaVisualsClient.jumpCubeCount, 1, 10, accent, 14);
                sliderRow(c, mx, my, left, right, top + 709, "Cube size", XakaVisualsClient.jumpCubeSize, 5, 40, accent, 15);
                sliderRow(c, mx, my, left, right, top + 758, "Particle density", XakaVisualsClient.particleDensity, 1, 5, accent, 25);
            }
            case 4 -> {
                infoCard(c, left, top, right, "First-person hand", "Move your hand and held item without moving the camera.");
                sliderRow(c, mx, my, left, right, top + 72, "Hand left / right",
                        Math.round((XakaVisualsClient.handOffsetX + 0.5f) * 20), 0, 20, accent, 5);
                sliderRow(c, mx, my, left, right, top + 121, "Hand up / down",
                        Math.round((XakaVisualsClient.handOffsetY + 0.5f) * 20), 0, 20, accent, 6);
                sliderRow(c, mx, my, left, right, top + 170, "Hand near / far",
                        Math.round((XakaVisualsClient.handOffsetZ + 0.5f) * 20), 0, 20, accent, 7);
                sliderRow(c, mx, my, left, right, top + 219, "Rotate X", Math.round(XakaVisualsClient.handRotationX + 180), 0, 360, accent, 17);
                sliderRow(c, mx, my, left, right, top + 268, "Rotate Y", Math.round(XakaVisualsClient.handRotationY + 180), 0, 360, accent, 18);
                sliderRow(c, mx, my, left, right, top + 317, "Rotate Z", Math.round(XakaVisualsClient.handRotationZ + 180), 0, 360, accent, 19);
                row(c, mx, my, left, right, top + 366, "Reset hand transform", "Restore position and all rotation axes", false, accent, false);
            }
            case 8 -> {
                row(c, mx, my, left, right, top, "Auto Sprint", "Sprint while moving forward", XakaVisualsClient.autoSprint, accent, true);
                row(c, mx, my, left, right, top + 49, "Low armor alert", "Warn when any equipped armor is nearly worn out", XakaVisualsClient.lowArmorNotify, accent, true);
                row(c, mx, my, left, right, top + 98, "Last death coordinates", "Display the last recorded death position", XakaVisualsClient.deathCoords, accent, true);
                row(c, mx, my, left, right, top + 147, "NoFluid overlay", "Hide the underwater screen overlay", XakaVisualsClient.noFluidOverlay, accent, true);
                row(c, mx, my, left, right, top + 196, "No fire overlay", "Hide the first-person fire overlay", XakaVisualsClient.noFireOverlay, accent, true);
                row(c, mx, my, left, right, top + 245, "Set GPS waypoint here", "Save your current block position", XakaVisualsClient.waypointEnabled, accent, false);
                row(c, mx, my, left, right, top + 294, "Clear GPS waypoint", "Remove the saved waypoint", false, accent, false);
                row(c, mx, my, left, right, top + 343, "NameProtect", "Hide names in the Xaka target HUD", XakaVisualsClient.nameProtect, accent, true);
                row(c, mx, my, left, right, top + 392, "No hurt camera", "Disable the camera tilt when your player takes damage", XakaVisualsClient.noHurtCam, accent, true);
                row(c, mx, my, left, right, top + 441, "No weather", "Hide rain, snow and local weather particles", XakaVisualsClient.noWeather, accent, true);
                row(c, mx, my, left, right, top + 490, "Hide boss bar", "Hide the boss health bar on your screen", XakaVisualsClient.hideBossBar, accent, true);
                row(c, mx, my, left, right, top + 539, "No in-wall overlay", "Hide the suffocation texture when inside blocks", XakaVisualsClient.noInWallOverlay, accent, true);
                row(c, mx, my, left, right, top + 588, "Shulker Preview", "Show stored items when hovering over a shulker box", XakaVisualsClient.shulkerPreview, accent, true);
            }
            case 7 -> {
                infoCard(c, left, top, right, "3D character accessories", "Real shaped cloth capes and solid hats attached to your model.");
                row(c, mx, my, left, right, top + 72, "Cape", "Toggle the flowing 3D cape on your back", XakaVisualsClient.wingsEnabled, accent, true);
                row(c, mx, my, left, right, top + 121, "Cape style: " + new String[]{"Royal violet", "Shadow", "Crimson"}[Math.floorMod(XakaVisualsClient.wingStyle, 3)], "Tap to switch cape design", false, accent, false);
                row(c, mx, my, left, right, top + 170, "Hat", "Toggle 3D headwear", XakaVisualsClient.hatEnabled, accent, true);
                row(c, mx, my, left, right, top + 219, "Hat style: " + new String[]{"Top hat", "Witch hat", "Adventurer", "Cap", "Frog", "China hat"}[Math.floorMod(XakaVisualsClient.hatStyle, 6)], "Tap to switch hat design", false, accent, false);
            }
            case 6 -> {
                infoCard(c, left, top, right, "Local MP3 player", "Choose MP3 files from your computer. The mini-player stays on the HUD.");
                row(c, mx, my, left, right, top + 72, "Add MP3 track", "Open a file picker and add a song", false, accent, false);
                row(c, mx, my, left, right, top + 121, "Previous track", "Switch to the previous song", false, accent, false);
                row(c, mx, my, left, right, top + 170, XakaVisualsMusic.isPlaying() ? "Pause music" : "Play music", XakaVisualsMusic.trackName(), XakaVisualsMusic.isPlaying(), accent, true);
                row(c, mx, my, left, right, top + 219, "Next track", "Switch to the next song", false, accent, false);
                sliderRow(c, mx, my, left, right, top + 268, "Volume", Math.round(XakaVisualsMusic.getVolume() * 100), 0, 100, accent, 9);
                contextText(c, left + 4, top + 326, XakaVisualsMusic.status());
            }
            case 5 -> {
                infoCard(c, left, top, right, "Character emotes", "Local-only emotes. Moving cancels the current emote; play it again to restart.");
                row(c, mx, my, left, right, top + 72, "Dance: " + XakaVisualsClient.danceName(), "Tap to choose; HohoDance plays a full short sequence", false, accent, false);
                row(c, mx, my, left, right, top + 121, XakaVisualsClient.dancePlaying ? "Stop dance" : "Play dance",
                        XakaVisualsClient.dancePlaying ? "Your character is currently dancing" : "Start the selected dance", XakaVisualsClient.dancePlaying, accent, true);
                sliderRow(c, mx, my, left, right, top + 170, "Animation speed",
                        Math.round(XakaVisualsClient.danceSpeed * 10), 5, 20, accent, 8);
            }
            case 9 -> {
                infoCard(c, left, top, right, "Ambience", "Change the sky cycle on your client only; the world and server clock are untouched.");
                row(c, mx, my, left, right, top + 72, "Custom time of day", "Override the visible day/night cycle locally", XakaVisualsClient.ambienceTimeEnabled, accent, true);
                int gameHour = Math.floorMod(XakaVisualsClient.ambienceTime / 1000, 24);
                int clockHour = (gameHour + 6) % 24;
                sliderRow(c, mx, my, left, right, top + 121, "Visual time", gameHour, 0, 23, accent, 24);
                String preset = switch (XakaVisualsClient.ambienceTime) {
                    case 0 -> "Sunrise"; case 6000 -> "Noon"; case 12000 -> "Sunset"; case 18000 -> "Midnight"; default -> "Custom";
                };
                row(c, mx, my, left, right, top + 170, "Quick preset: " + preset, "Tap to cycle sunrise, noon, sunset and midnight", false, accent, false);
                contextText(c, left + 4, top + 222, String.format(java.util.Locale.ROOT, "Displayed clock: %02d:00", clockHour));
            }
            case 3 -> {
                infoCard(c, left, top, right, "Accent color", "Choose a signature color for the XakaVisuals interface.");
                String[] names = {"Violet", "Mint", "Rose", "Amber", "Blue"};
                int index = Math.floorMod(XakaVisualsClient.accentIndex, names.length);
                button(c, mx, my, left, top + 78, 118, 31, "‹  Previous", false, accent);
                button(c, mx, my, left + 126, top + 78, 118, 31, names[index] + "  ›", true, accent);
                row(c, mx, my, left, right, top + 126, "Compact HUD", "Use smaller in-game HUD panels", XakaVisualsClient.compactHud, accent, true);
                sliderRow(c, mx, my, left, right, top + 175, "Marker left / right", Math.round((XakaVisualsClient.bodyMarkerOffsetX + 0.5f) * 10), 0, 10, accent, 2);
                sliderRow(c, mx, my, left, right, top + 224, "Marker forward / back", Math.round((XakaVisualsClient.bodyMarkerOffsetZ + 0.5f) * 10), 0, 10, accent, 3);
                sliderRow(c, mx, my, left, right, top + 273, "Marker height", Math.round((XakaVisualsClient.bodyMarkerOffsetY + 0.5f) * 10), 0, 10, accent, 4);
            }
        }
    }


    private void sliderRow(DrawContext c, int mx, int my, int left, int right, int y,
                           String title, int value, int min, int max, int accent, int id) {
        boolean hover = inside(mx, my, left, y, right, y + 42);
        c.fill(left, y, right, y + 42, hover ? CARD_HOVER : CARD);
        c.drawTextWithShadow(textRenderer, title, left + 12, y + 6, TEXT);
        String shown = switch (id) {
            case 2 -> String.format(java.util.Locale.ROOT, "%.1f", XakaVisualsClient.bodyMarkerOffsetX);
            case 3 -> String.format(java.util.Locale.ROOT, "%.1f", XakaVisualsClient.bodyMarkerOffsetZ);
            case 4 -> String.format(java.util.Locale.ROOT, "%.1f", XakaVisualsClient.bodyMarkerOffsetY);
            case 5 -> String.format(java.util.Locale.ROOT, "%.2f", XakaVisualsClient.handOffsetX);
            case 6 -> String.format(java.util.Locale.ROOT, "%.2f", XakaVisualsClient.handOffsetY);
            case 7 -> String.format(java.util.Locale.ROOT, "%.2f", XakaVisualsClient.handOffsetZ);
            case 8 -> String.format(java.util.Locale.ROOT, "%.1fx", XakaVisualsClient.danceSpeed);
            case 13 -> Integer.toString(XakaVisualsClient.crosshairSize);
            case 9 -> Math.round(XakaVisualsMusic.getVolume() * 100) + "%";
            case 10 -> String.format(java.util.Locale.ROOT, "%.1fx", XakaVisualsClient.wingSpeed);
            case 14 -> Integer.toString(XakaVisualsClient.jumpCubeCount);
            case 15 -> String.format(java.util.Locale.ROOT, "%.2f", XakaVisualsClient.jumpCubeSize / 100.0f);
            case 16 -> Integer.toString(XakaVisualsClient.mouseParticleSize);
            case 25 -> XakaVisualsClient.particleDensity + "/5";
            case 17 -> Math.round(XakaVisualsClient.handRotationX) + "°";
            case 18 -> Math.round(XakaVisualsClient.handRotationY) + "°";
            case 19 -> Math.round(XakaVisualsClient.handRotationZ) + "°";
            case 20 -> Integer.toString(XakaVisualsClient.crosshairGap);
            case 21 -> Integer.toString(XakaVisualsClient.crosshairThickness);
            case 22 -> Integer.toString(XakaVisualsClient.crosshairDotSize);
            case 23 -> Integer.toString(XakaVisualsClient.crosshairOutlineWidth);
            case 24 -> String.format(java.util.Locale.ROOT, "%02d:00", (value + 6) % 24);
            default -> Integer.toString(value);
        };
        c.drawTextWithShadow(textRenderer, shown, right - 34, y + 6, accent);
        int sx = left + 12, ex = right - 12, sy = y + 29;
        c.fill(sx, sy, ex, sy + 4, 0xFF414A5D);
        int knob = sx + Math.round((ex - sx) * ((value - min) / (float)(max - min)));
        c.fill(sx, sy, knob, sy + 4, accent);
        c.fill(knob - 3, sy - 3, knob + 4, sy + 7, 0xFFFFFFFF);
    }

    private void setSlider(int id, double mouseX, int left, int right) {
        int sx = left + 12, ex = right - 12;
        float t = (float)Math.max(0, Math.min(1, (mouseX - sx) / (double)(ex - sx)));
        switch (id) {
            case 0 -> XakaVisualsClient.haloSize = 4 + Math.round(t * 14);
            case 1 -> XakaVisualsClient.markerSize = 2 + Math.round(t * 12);
            case 2 -> XakaVisualsClient.bodyMarkerOffsetX = Math.round((-0.5f + t) * 10) / 10.0f;
            case 3 -> XakaVisualsClient.bodyMarkerOffsetZ = Math.round((-0.5f + t) * 10) / 10.0f;
            case 4 -> XakaVisualsClient.bodyMarkerOffsetY = Math.round((-0.5f + t) * 10) / 10.0f;
            case 5 -> XakaVisualsClient.handOffsetX = Math.round((-0.5f + t) * 100) / 100.0f;
            case 6 -> XakaVisualsClient.handOffsetY = Math.round((-0.5f + t) * 100) / 100.0f;
            case 7 -> XakaVisualsClient.handOffsetZ = Math.round((-0.5f + t) * 100) / 100.0f;
            case 8 -> XakaVisualsClient.danceSpeed = 0.5f + t * 1.5f;
            case 13 -> XakaVisualsClient.crosshairSize = 2 + Math.round(t * 10);
            case 9 -> XakaVisualsMusic.setVolume(t);
            case 10 -> XakaVisualsClient.wingSpeed = 0.5f + t * 1.5f;
            case 11 -> XakaVisualsClient.jumpEffectRadius = 5 + Math.round(t * 95);
            case 12 -> XakaVisualsClient.jumpEffectDuration = 300 + Math.round(t * 1700);
            case 14 -> XakaVisualsClient.jumpCubeCount = 1 + Math.round(t * 9);
            case 15 -> XakaVisualsClient.jumpCubeSize = 5 + Math.round(t * 35);
            case 16 -> XakaVisualsClient.mouseParticleSize = 2 + Math.round(t * 8);
            case 17 -> XakaVisualsClient.handRotationX = -180.0f + t * 360.0f;
            case 18 -> XakaVisualsClient.handRotationY = -180.0f + t * 360.0f;
            case 19 -> XakaVisualsClient.handRotationZ = -180.0f + t * 360.0f;
            case 20 -> XakaVisualsClient.crosshairGap = Math.round(t * 12);
            case 21 -> XakaVisualsClient.crosshairThickness = 1 + Math.round(t * 3);
            case 22 -> XakaVisualsClient.crosshairDotSize = 1 + Math.round(t * 5);
            case 23 -> XakaVisualsClient.crosshairOutlineWidth = 1 + Math.round(t * 2);
            case 24 -> XakaVisualsClient.ambienceTime = Math.round(t * 23) * 1000;
            case 25 -> XakaVisualsClient.particleDensity = 1 + Math.round(t * 4);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int panelW = Math.min(620, width - 28);
        int panelH = Math.min(440, height - 28);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        if (mouseX < x || mouseX > x + panelW || mouseY < y || mouseY > y + panelH)
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        int navTop = y + 85, navBottom = y + panelH - 55;
        // Wheel input anywhere over the panel scrolls the relevant pane.
        if (mouseX <= x + 176) {
            int max = Math.max(0, CATEGORIES.length * 64 - (navBottom - navTop));
            navScrollOffset = Math.max(0, Math.min(max,
                    navScrollOffset - (int)Math.round(verticalAmount * 36)));
        } else {
            int viewportHeight = Math.max(1, panelH - 116);
            int max = Math.max(0, categoryContentHeight() - viewportHeight);
            contentScrollOffset = Math.max(0, Math.min(max,
                    contentScrollOffset - (int)Math.round(verticalAmount * 36)));
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int panelW = Math.min(620, width - 28);
        int panelH = Math.min(440, height - 28);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int navViewportTop = y + 85;
        int navViewportBottom = y + panelH - 55;
        int navStep = 64;
        int navRowHeight = Math.min(30, navStep - 2);
        int navY = navViewportTop + 4;
        int navMaxScroll = Math.max(0, CATEGORIES.length * navStep - (navViewportBottom - navViewportTop));
        navScrollOffset = Math.max(0, Math.min(navScrollOffset, navMaxScroll));
        if (mouseY >= navViewportTop && mouseY <= navViewportBottom) {
            navDragging = true;
            for (int i = 0; i < CATEGORIES.length; i++) {
                int rowTop = navY + i * navStep - navScrollOffset;
                if (inside(mouseX, mouseY, x + 20, rowTop, x + 155, rowTop + navRowHeight)) {
                    selectedCategory = i;
                    navDragging = true;
                    return true;
                }
            }
        }

        int left = x + 188;
        int right = x + panelW - 18;
        int top = y + 74 - contentScrollOffset;
        if (mouseY < y + 68 || mouseY > y + panelH - 48) return true;
        if (selectedCategory == 0) {
            if (hitRow(mouseX, mouseY, left, right, top + 49)) XakaVisualsClient.animations = !XakaVisualsClient.animations;
            if (hitRow(mouseX, mouseY, left, right, top + 98)) XakaVisualsClient.compactHud = !XakaVisualsClient.compactHud;
            if (hitRow(mouseX, mouseY, left, right, top + 147)) XakaVisualsClient.bodyMarkerEnabled = !XakaVisualsClient.bodyMarkerEnabled;
            if (hitRow(mouseX, mouseY, left, right, top + 196)) XakaVisualsClient.petEnabled = !XakaVisualsClient.petEnabled;
            if (hitRow(mouseX, mouseY, left, right, top + 245)) {
                if (mouseX > (left + right) / 2.0) XakaVisualsClient.petType = (XakaVisualsClient.petType + 1) % 4;
                else XakaVisualsClient.petSize = XakaVisualsClient.petSize >= 10 ? 3 : XakaVisualsClient.petSize + 2;
            }
            if (hitRow(mouseX, mouseY, left, right, top + 294))
                XakaVisualsClient.targetFrameColorIndex = (XakaVisualsClient.targetFrameColorIndex + 1) % 8;
            if (hitRow(mouseX, mouseY, left, right, top + 343)) XakaVisualsClient.mouseParticles = !XakaVisualsClient.mouseParticles;
            if (hitRow(mouseX, mouseY, left, right, top + 392)) { activeSlider = 16; setSlider(16, mouseX, left, right); }
        } else if (selectedCategory == 1) {
            if (hitRow(mouseX, mouseY, left, right, top)) XakaVisualsClient.watermark = !XakaVisualsClient.watermark;
            if (hitRow(mouseX, mouseY, left, right, top + 49)) XakaVisualsClient.fps = !XakaVisualsClient.fps;
            if (hitRow(mouseX, mouseY, left, right, top + 98)) XakaVisualsClient.coordinates = !XakaVisualsClient.coordinates;
            if (hitRow(mouseX, mouseY, left, right, top + 147)) XakaVisualsClient.markerSquare = !XakaVisualsClient.markerSquare;
            if (hitRow(mouseX, mouseY, left, right, top + 196)) XakaVisualsClient.fpsBoost = !XakaVisualsClient.fpsBoost;
            if (hitRow(mouseX, mouseY, left, right, top + 245)) XakaVisualsClient.enemyHpEnabled = !XakaVisualsClient.enemyHpEnabled;
            if (hitRow(mouseX, mouseY, left, right, top + 882)) XakaVisualsClient.selfHealthHud = !XakaVisualsClient.selfHealthHud;
            if (hitRow(mouseX, mouseY, left, right, top + 294))
                net.minecraft.client.MinecraftClient.getInstance().setScreen(new XakaHudPositionScreen());
            if (hitRow(mouseX, mouseY, left, right, top + 343)) VisualSettings.fullbright = !VisualSettings.fullbright;
            if (hitRow(mouseX, mouseY, left, right, top + 392)) XakaVisualsClient.customCrosshair = !XakaVisualsClient.customCrosshair;
            if (hitRow(mouseX, mouseY, left, right, top + 441)) { activeSlider = 13; setSlider(13, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 490)) XakaVisualsClient.cooldownHud = !XakaVisualsClient.cooldownHud;
            if (hitRow(mouseX, mouseY, left, right, top + 539)) { activeSlider = 20; setSlider(20, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 588)) { activeSlider = 21; setSlider(21, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 637)) XakaVisualsClient.crosshairDot = !XakaVisualsClient.crosshairDot;
            if (hitRow(mouseX, mouseY, left, right, top + 686)) { activeSlider = 22; setSlider(22, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 735)) XakaVisualsClient.crosshairOutline = !XakaVisualsClient.crosshairOutline;
            if (hitRow(mouseX, mouseY, left, right, top + 784)) { activeSlider = 23; setSlider(23, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 833)) XakaVisualsClient.droppedItemTilt = !XakaVisualsClient.droppedItemTilt;
        } else if (selectedCategory == 2) {
            if (hitRow(mouseX, mouseY, left, right, top + 72)) XakaVisualsClient.haloEnabled = !XakaVisualsClient.haloEnabled;
            if (hitRow(mouseX, mouseY, left, right, top + 121)) { activeSlider = 0; setSlider(0, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 170)) XakaVisualsClient.bodyMarkerEnabled = !XakaVisualsClient.bodyMarkerEnabled;
            if (hitRow(mouseX, mouseY, left, right, top + 219)) XakaVisualsClient.markerSquare = !XakaVisualsClient.markerSquare;
            if (hitRow(mouseX, mouseY, left, right, top + 317)) XakaVisualsClient.hitParticles = !XakaVisualsClient.hitParticles;
            if (hitRow(mouseX, mouseY, left, right, top + 366)) XakaVisualsClient.jumpCircle = !XakaVisualsClient.jumpCircle;
            if (hitRow(mouseX, mouseY, left, right, top + 415)) XakaVisualsClient.playerTrails = !XakaVisualsClient.playerTrails;
            if (hitRow(mouseX, mouseY, left, right, top + 464)) XakaVisualsClient.jumpEffect = !XakaVisualsClient.jumpEffect;
            if (hitRow(mouseX, mouseY, left, right, top + 513)) { activeSlider = 11; setSlider(11, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 562)) { activeSlider = 12; setSlider(12, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 611)) XakaVisualsClient.jumpCubes = !XakaVisualsClient.jumpCubes;
            if (hitRow(mouseX, mouseY, left, right, top + 660)) { activeSlider = 14; setSlider(14, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 709)) { activeSlider = 15; setSlider(15, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 758)) { activeSlider = 25; setSlider(25, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 268)) { activeSlider = 1; setSlider(1, mouseX, left, right); }
        } else if (selectedCategory == 4) {
            if (hitRow(mouseX, mouseY, left, right, top + 72)) { activeSlider = 5; setSlider(5, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 121)) { activeSlider = 6; setSlider(6, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 170)) { activeSlider = 7; setSlider(7, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 219)) { activeSlider = 17; setSlider(17, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 268)) { activeSlider = 18; setSlider(18, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 317)) { activeSlider = 19; setSlider(19, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 366)) {
                XakaVisualsClient.handOffsetX = XakaVisualsClient.handOffsetY = XakaVisualsClient.handOffsetZ = 0.0f;
                XakaVisualsClient.handRotationX = XakaVisualsClient.handRotationY = XakaVisualsClient.handRotationZ = 0.0f;
            }
        } else if (selectedCategory == 9) {
            if (hitRow(mouseX, mouseY, left, right, top + 72)) XakaVisualsClient.ambienceTimeEnabled = !XakaVisualsClient.ambienceTimeEnabled;
            if (hitRow(mouseX, mouseY, left, right, top + 121)) { activeSlider = 24; setSlider(24, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 170)) {
                XakaVisualsClient.ambienceTime = switch (XakaVisualsClient.ambienceTime) {
                    case 0 -> 6000; case 6000 -> 12000; case 12000 -> 18000; default -> 0;
                };
            }
        } else if (selectedCategory == 8) {
            if (hitRow(mouseX, mouseY, left, right, top)) XakaVisualsClient.autoSprint = !XakaVisualsClient.autoSprint;
            if (hitRow(mouseX, mouseY, left, right, top + 49)) XakaVisualsClient.lowArmorNotify = !XakaVisualsClient.lowArmorNotify;
            if (hitRow(mouseX, mouseY, left, right, top + 98)) XakaVisualsClient.deathCoords = !XakaVisualsClient.deathCoords;
            if (hitRow(mouseX, mouseY, left, right, top + 147)) XakaVisualsClient.noFluidOverlay = !XakaVisualsClient.noFluidOverlay;
            if (hitRow(mouseX, mouseY, left, right, top + 196)) XakaVisualsClient.noFireOverlay = !XakaVisualsClient.noFireOverlay;
            if (hitRow(mouseX, mouseY, left, right, top + 245)) {
                var player = net.minecraft.client.MinecraftClient.getInstance().player;
                if (player != null) {
                    XakaVisualsClient.waypointX = player.getBlockX();
                    XakaVisualsClient.waypointY = player.getBlockY();
                    XakaVisualsClient.waypointZ = player.getBlockZ();
                    XakaVisualsClient.waypointEnabled = true;
                }
            }
            if (hitRow(mouseX, mouseY, left, right, top + 294)) XakaVisualsClient.waypointEnabled = false;
            if (hitRow(mouseX, mouseY, left, right, top + 343)) XakaVisualsClient.nameProtect = !XakaVisualsClient.nameProtect;
            if (hitRow(mouseX, mouseY, left, right, top + 392)) XakaVisualsClient.noHurtCam = !XakaVisualsClient.noHurtCam;
            if (hitRow(mouseX, mouseY, left, right, top + 441)) XakaVisualsClient.noWeather = !XakaVisualsClient.noWeather;
            if (hitRow(mouseX, mouseY, left, right, top + 490)) XakaVisualsClient.hideBossBar = !XakaVisualsClient.hideBossBar;
            if (hitRow(mouseX, mouseY, left, right, top + 539)) XakaVisualsClient.noInWallOverlay = !XakaVisualsClient.noInWallOverlay;
            if (hitRow(mouseX, mouseY, left, right, top + 588)) XakaVisualsClient.shulkerPreview = !XakaVisualsClient.shulkerPreview;
        } else if (selectedCategory == 7) {
            if (hitRow(mouseX, mouseY, left, right, top + 72)) XakaVisualsClient.wingsEnabled = !XakaVisualsClient.wingsEnabled;
            if (hitRow(mouseX, mouseY, left, right, top + 121)) XakaVisualsClient.wingStyle = (XakaVisualsClient.wingStyle + 1) % 3;
            if (hitRow(mouseX, mouseY, left, right, top + 170)) XakaVisualsClient.hatEnabled = !XakaVisualsClient.hatEnabled;
            if (hitRow(mouseX, mouseY, left, right, top + 219)) XakaVisualsClient.hatStyle = (XakaVisualsClient.hatStyle + 1) % 6;
        } else if (selectedCategory == 6) {
            if (hitRow(mouseX, mouseY, left, right, top + 72)) XakaVisualsMusic.chooseTrack();
            if (hitRow(mouseX, mouseY, left, right, top + 121)) XakaVisualsMusic.previous();
            if (hitRow(mouseX, mouseY, left, right, top + 170)) XakaVisualsMusic.playPause();
            if (hitRow(mouseX, mouseY, left, right, top + 219)) XakaVisualsMusic.next();
            if (hitRow(mouseX, mouseY, left, right, top + 268)) { activeSlider = 9; setSlider(9, mouseX, left, right); }
        } else if (selectedCategory == 5) {
            if (hitRow(mouseX, mouseY, left, right, top + 72)) XakaVisualsClient.activeDance = (XakaVisualsClient.activeDance + 1) % 6;
            if (hitRow(mouseX, mouseY, left, right, top + 121)) XakaVisualsClient.dancePlaying = !XakaVisualsClient.dancePlaying;
            if (hitRow(mouseX, mouseY, left, right, top + 170)) { activeSlider = 8; setSlider(8, mouseX, left, right); }
        } else if (selectedCategory == 3) {
            if (inside(mouseX, mouseY, left, top + 78, left + 118, top + 109))
                XakaVisualsClient.accentIndex = Math.floorMod(XakaVisualsClient.accentIndex - 1, 5);
            if (inside(mouseX, mouseY, left + 126, top + 78, left + 244, top + 109))
                XakaVisualsClient.accentIndex = Math.floorMod(XakaVisualsClient.accentIndex + 1, 5);
            if (hitRow(mouseX, mouseY, left, right, top + 126)) XakaVisualsClient.compactHud = !XakaVisualsClient.compactHud;
            if (hitRow(mouseX, mouseY, left, right, top + 175)) { activeSlider = 2; setSlider(2, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 224)) { activeSlider = 3; setSlider(3, mouseX, left, right); }
            if (hitRow(mouseX, mouseY, left, right, top + 273)) { activeSlider = 4; setSlider(4, mouseX, left, right); }
        }
        VisualSettings.save();
        return true;
    }

    private int categoryContentHeight() {
        return switch (selectedCategory) {
            case 0 -> 450;
            case 1 -> 950;
            case 2 -> 816;
            case 3 -> 333;
            case 4 -> 470;
            case 5 -> 284;
            case 6 -> 370;
            case 7 -> 333;
            case 8 -> 657;
            case 9 -> 270;
            default -> 310;
        };
    }

    private float stepOffset(float value) {
        float next = value + 0.1f;
        return next > 0.5f ? -0.5f : Math.round(next * 10.0f) / 10.0f;
    }

    private boolean hitRow(double mx, double my, int left, int right, int y) {
        return inside(mx, my, left, y, right, y + 42);
    }

    private boolean inside(double mx, double my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }

    private String categoryIcon(int index) {
        return switch (index) { case 0 -> "✦"; case 1 -> "▤"; case 2 -> "◈"; case 5 -> "♪"; case 6 -> "♫"; case 7 -> "✧"; case 9 -> "☼"; default -> "⚙"; };
    }

    private float easeOut(float t) { return 1f - (float) Math.pow(1f - t, 3); }

    @Override
    public void close() {
        VisualSettings.save();
        super.close();
    }

    @Override
    public boolean shouldPause() { return false; }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (navDragging && button == 0) {
            int panelW = Math.min(620, width - 28);
            int panelH = Math.min(440, height - 28);
            int x = (width - panelW) / 2;
            int y = (height - panelH) / 2;
            int top = y + 85;
            int bottom = y + panelH - 55;
            if (mouseY >= top && mouseY <= bottom) {
                int step = 64;
                int max = Math.max(0, CATEGORIES.length * step - (bottom - top));
                navScrollOffset = Math.max(0, Math.min(max, navScrollOffset - (int)Math.round(deltaY)));
            }
            return true;
        }
        if (activeSlider >= 0 && button == 0) {
            int panelW = Math.min(620, width - 28);
            int x = (width - panelW) / 2;
            setSlider(activeSlider, mouseX, x + 188, x + panelW - 18);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) { activeSlider = -1; navDragging = false; VisualSettings.save(); }
        return super.mouseReleased(mouseX, mouseY, button);
    }

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
