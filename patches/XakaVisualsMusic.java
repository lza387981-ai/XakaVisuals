package dev.xaka.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import javax.sound.sampled.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class XakaVisualsMusic {
    private static final List<File> TRACKS = new ArrayList<>();
    private static Clip clip;
    private static int current = -1;
    private static float volume = 0.75f;
    private static boolean lastMouseDown, dragging;
    private static int dragX, dragY, hudX = -1, hudY = -1;
    private static String error = "";
    private XakaVisualsMusic() {}

    public static synchronized void addTrack(File file) {
        if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".mp3")) {
            TRACKS.add(file);
            if (current < 0) current = 0;
            error = "";
        }
    }
    public static void chooseTrack() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Add MP3 to XakaVisuals");
            chooser.setFileFilter(new FileNameExtensionFilter("MP3 audio (*.mp3)", "mp3"));
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) addTrack(chooser.getSelectedFile());
        } catch (Throwable t) { error = "Desktop file picker unavailable"; }
    }
    public static synchronized void playPause() {
        if (clip == null) { playIndex(Math.max(0, current)); return; }
        if (clip.isRunning()) clip.stop(); else clip.start();
    }
    public static synchronized void playIndex(int index) {
        if (TRACKS.isEmpty()) { error = "Add an MP3 file first"; return; }
        current = Math.floorMod(index, TRACKS.size());
        try {
            if (clip != null) { clip.stop(); clip.close(); clip = null; }
            AudioInputStream encoded = AudioSystem.getAudioInputStream(TRACKS.get(current));
            AudioFormat source = encoded.getFormat();
            int channels = source.getChannels() > 0 ? source.getChannels() : 2;
            float rate = source.getSampleRate() > 0 ? source.getSampleRate() : 44100f;
            AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, channels, channels * 2, rate, false);
            try (AudioInputStream decoded = AudioSystem.getAudioInputStream(pcm, encoded)) {
                clip = AudioSystem.getClip();
                clip.open(decoded);
            }
            applyVolume();
            clip.start();
            error = "";
        } catch (Exception ex) {
            error = "Could not decode this MP3";
            if (clip != null) { clip.close(); clip = null; }
        }
    }
    public static synchronized void next() { if (!TRACKS.isEmpty()) playIndex(current + 1); }
    public static synchronized void previous() { if (!TRACKS.isEmpty()) playIndex(current - 1); }
    public static synchronized void setVolume(float value) { volume = Math.max(0f, Math.min(1f, value)); applyVolume(); }
    public static float getVolume() { return volume; }
    private static void applyVolume() {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl c = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float db = volume <= 0.001f ? c.getMinimum() : (float)(20.0 * Math.log10(volume));
            c.setValue(Math.max(c.getMinimum(), Math.min(c.getMaximum(), db)));
        }
    }
    public static synchronized void seek(float fraction) {
        if (clip != null && clip.isOpen()) clip.setMicrosecondPosition((long)(clip.getMicrosecondLength() * Math.max(0, Math.min(1, fraction))));
    }
    public static synchronized String trackName() {
        return current >= 0 && current < TRACKS.size() ? TRACKS.get(current).getName() : "No track selected";
    }
    public static synchronized String status() {
        if (!error.isEmpty()) return error;
        return TRACKS.isEmpty() ? "Choose an MP3 file to begin" : (clip != null && clip.isRunning() ? "Playing" : "Paused");
    }
    public static synchronized boolean hasTrack() { return !TRACKS.isEmpty(); }
    public static synchronized boolean isPlaying() { return clip != null && clip.isRunning(); }
    public static synchronized float progress() {
        return clip == null || !clip.isOpen() || clip.getMicrosecondLength() == 0 ? 0f : clip.getMicrosecondPosition() / (float)clip.getMicrosecondLength();
    }
    private static String fmt(long us) {
        long sec = Math.max(0, us / 1000000);
        return String.format(java.util.Locale.ROOT, "%d:%02d", sec / 60, sec % 60);
    }
    public static synchronized String timeText() {
        return clip == null || !clip.isOpen() ? "0:00 / 0:00" : fmt(clip.getMicrosecondPosition()) + " / " + fmt(clip.getMicrosecondLength());
    }
    public static void renderHud(DrawContext ctx, MinecraftClient client) {
        if (!hasTrack() || client.options.hudHidden) return;
        int sw = client.getWindow().getScaledWidth(), sh = client.getWindow().getScaledHeight();
        if (hudX < 0) { hudX = sw - 194; hudY = sh - 62; }
        hudX = Math.max(0, Math.min(sw - 184, hudX)); hudY = Math.max(0, Math.min(sh - 46, hudY));
        int x = hudX, y = hudY;
        ctx.fill(x, y, x + 184, y + 46, 0xE9101420);
        ctx.fill(x, y, x + 184, y + 2, XakaVisualsClient.accent());
        ctx.fill(x, y + 2, x + 184, y + 15, 0xFF20283A);
        String name = trackName(); if (name.length() > 22) name = name.substring(0, 19) + "...";
        ctx.drawTextWithShadow(client.textRenderer, name, x + 7, y + 4, 0xFFF4F1FA);
        ctx.drawTextWithShadow(client.textRenderer, "‹", x + 9, y + 19, 0xFFFFFFFF);
        ctx.drawTextWithShadow(client.textRenderer, isPlaying() ? "Ⅱ" : "▶", x + 31, y + 19, XakaVisualsClient.accent());
        ctx.drawTextWithShadow(client.textRenderer, "›", x + 57, y + 19, 0xFFFFFFFF);
        ctx.drawTextWithShadow(client.textRenderer, timeText(), x + 82, y + 20, 0xFFB9C1D2);
        int bx = x + 8, by = y + 38, bw = 168;
        ctx.fill(bx, by, bx + bw, by + 3, 0xFF414A5D);
        ctx.fill(bx, by, bx + Math.round(bw * progress()), by + 3, XakaVisualsClient.accent());
    }
    public static void tickMouse(MinecraftClient client) {
        if (client == null || !hasTrack()) return;
        long win = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        double[] cx = new double[1], cy = new double[1]; GLFW.glfwGetCursorPos(win, cx, cy);
        int mx = (int)(cx[0] * client.getWindow().getScaledWidth() / client.getWindow().getWidth());
        int my = (int)(cy[0] * client.getWindow().getScaledHeight() / client.getWindow().getHeight());
        if (down && !lastMouseDown && mx >= hudX && mx <= hudX + 184 && my >= hudY && my <= hudY + 46) {
            if (my <= hudY + 15) { dragging = true; dragX = mx - hudX; dragY = my - hudY; }
            else if (my >= hudY + 34) seek((mx - (hudX + 8)) / 168f);
            else if (mx < hudX + 29) previous();
            else if (mx < hudX + 55) playPause();
            else if (mx < hudX + 80) next();
        }
        if (down && dragging) { hudX = mx - dragX; hudY = my - dragY; }
        if (!down) dragging = false;
        lastMouseDown = down;
    }
}
