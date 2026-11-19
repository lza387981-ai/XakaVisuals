package dev.xaka.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class XakaVisualsClient implements ClientModInitializer {
    public static boolean animations = true;
    public static boolean compactHud = false;
    public static boolean watermark = true;
    public static boolean fps = true;
    public static boolean coordinates = false;
    public static boolean enemyHpEnabled = true;
    public static int enemyHpX = -1;
    public static int enemyHpY = -1;
    public static boolean markerEnabled = false;
    public static boolean markerSquare = false;
    public static int markerSize = 5;
    public static int targetFrameColorIndex = 0;
    public static boolean haloEnabled = false;
    public static int haloSize = 8;
    public static boolean bodyMarkerEnabled = false;
    public static boolean bodyMarkerSquare = false;
    public static int bodyMarkerSize = 5;
    public static float bodyMarkerOffsetX = 0.0f;
    public static float bodyMarkerOffsetY = 0.0f;
    public static float bodyMarkerOffsetZ = 0.0f;
    public static int accentIndex = 0;
    public static boolean petEnabled = false;
    // 0=Creeper, 1=Wither, 2=Warden, 3=Allay.
    public static int petType = 0;
    public static int petSize = 5;
    public static boolean wingsEnabled = false;
    public static int wingStyle = 0;
    public static float wingSpeed = 1.0f; // retained for config compatibility
    public static boolean hatEnabled = false;
    public static int hatStyle = 0;

    public static String petName() {
        return switch (Math.floorMod(petType, 4)) {
            case 0 -> "Creeper";
            case 1 -> "Wither";
            case 2 -> "Mini Warden";
            default -> "Mini Allay";
        };
    }
    // First-person hand/item offsets, in camera-local blocks.
    public static float handOffsetX = 0.0f;
    public static float handOffsetY = 0.0f;
    public static float handOffsetZ = 0.0f;
    public static boolean fpsBoost = false;
    public static boolean customCrosshair = false;
    public static int crosshairSize = 4;
    public static boolean autoSprint = false;
    public static boolean lowArmorNotify = false;
    public static boolean deathCoords = false;
    private static boolean hasDeathLocation = false;
    private static int lastDeathX, lastDeathY, lastDeathZ;
    public static boolean hitParticles = false;
    public static boolean jumpCircle = false;
    public static boolean jumpEffect = false;
    public static boolean jumpCubes = false;
    public static int jumpCubeCount = 5;
    public static int jumpCubeSize = 18;
    public static int jumpEffectRadius = 5;
    public static int jumpEffectDuration = 800;
    public static boolean playerTrails = false;
    public static boolean noFluidOverlay = false;
    public static boolean noFireOverlay = false;
    public static boolean noHurtCam = false;
    public static boolean noWeather = false;
    public static boolean hideBossBar = false;
    public static boolean noInWallOverlay = false;
    public static boolean cooldownHud = false;
    public static boolean waypointEnabled = false;
    public static boolean nameProtect = false;
    public static int waypointX, waypointY, waypointZ;
    private static boolean wasOnGround = true;
    private static int trailTicks = 0;
    private static int previousHitTargetId = -1;
    private static float previousHitTargetHealth = -1.0f;
    // Local emote playback; the renderer mixin applies it only to this player's model.
    public static boolean dancePlaying = false;
    public static int activeDance = 0;
    public static float danceSpeed = 1.0f;
    public static int danceEntityId = -1;
    private static boolean wasDancePlaying = false;
    private static double lastDanceX, lastDanceY, lastDanceZ;
    public static float danceStartAge = 0.0f;
    public static String danceName() {
        return switch (Math.floorMod(activeDance, 6)) {
            case 0 -> "Robot";
            case 1 -> "Shuffle";
            case 2 -> "Floss";
            case 3 -> "Moonwalk";
            case 4 -> "XakaDance";
            default -> "HohoDance";
        };
    }
    private static Object previousParticles = null;
    private static double previousGamma = Double.NaN;
    private static KeyBinding zoomKey;
    private static boolean zoomActive;
    private static int previousFov = -1;

    private static final int[] ACCENTS = {
            0xFF9B78FF, 0xFF69D6B0, 0xFFFF7EAA, 0xFFFFB45E, 0xFF69A9FF
    };

    private static final int[] TARGET_FRAME_COLORS = {
            0xFFFFFFFF, 0xFF9B78FF, 0xFF69D6B0, 0xFFFF7EAA,
            0xFFFFB45E, 0xFF69A9FF, 0xFFFFE36E, 0xFFFF5C5C
    };
    private static final String[] TARGET_FRAME_COLOR_NAMES = {
            "White", "Violet", "Mint", "Rose", "Amber", "Blue", "Gold", "Red"
    };

    public static int accent() {
        return ACCENTS[Math.floorMod(accentIndex, ACCENTS.length)];
    }

    public static int targetFrameColor() {
        return TARGET_FRAME_COLORS[Math.floorMod(targetFrameColorIndex, TARGET_FRAME_COLORS.length)];
    }

    public static String targetFrameColorName() {
        return TARGET_FRAME_COLOR_NAMES[Math.floorMod(targetFrameColorIndex, TARGET_FRAME_COLOR_NAMES.length)];
    }

    @Override
    public void onInitializeClient() {
        VisualSettings.load();
        KeyBinding openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xakavisuals.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.xakavisuals.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                if (client.player.getHealth() <= 0.0f) {
                    lastDeathX = client.player.getBlockX();
                    lastDeathY = client.player.getBlockY();
                    lastDeathZ = client.player.getBlockZ();
                    hasDeathLocation = true;
                }
                if (autoSprint && client.player.input.movementForward > 0.0f && !client.player.isSneaking()) {
                    client.player.setSprinting(true);
                }
                if (VisualSettings.fullbright) {
                    if (Double.isNaN(previousGamma)) previousGamma = client.options.getGamma().getValue();
                    client.options.getGamma().setValue(16.0);
                } else if (!Double.isNaN(previousGamma)) {
                    client.options.getGamma().setValue(previousGamma);
                    previousGamma = Double.NaN;
                }
                if (zoomKey.isPressed()) {
                    if (!zoomActive) {
                        previousFov = client.options.getFov().getValue();
                        zoomActive = true;
                    }
                    client.options.getFov().setValue(30);
                } else if (zoomActive) {
                    if (previousFov >= 0) client.options.getFov().setValue(previousFov);
                    previousFov = -1;
                    zoomActive = false;
                }
            } else {
                if (!Double.isNaN(previousGamma)) {
                    client.options.getGamma().setValue(previousGamma);
                    previousGamma = Double.NaN;
                }
                if (zoomActive) {
                    if (previousFov >= 0) client.options.getFov().setValue(previousFov);
                    previousFov = -1;
                    zoomActive = false;
                }
            }
            // Right Shift toggles only the XakaVisuals menu; other screens are left alone.
            while (openMenu.wasPressed()) {
                if (client.currentScreen instanceof XakaVisualsScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new XakaVisualsScreen());
                }
            }
        });

        XakaVisualsRenderer.register();
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xakavisuals.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.xakavisuals.main"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(XakaVisualsMusic::tickMouse);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                wasOnGround = true;
                trailTicks = 0;
                return;
            }
            boolean onGround = client.player.isOnGround();
            if (wasOnGround && !onGround && client.player.getVelocity().y > 0.0) {
                if (jumpEffect) XakaVisualsRenderer.spawnJumpEffect(client.player.getX(), client.player.getY() + 0.04, client.player.getZ());
                if (jumpCubes) XakaVisualsRenderer.spawnJumpCubes(client.player.getX(), client.player.getY() + 0.04, client.player.getZ());
            }
            if (jumpCircle && !wasOnGround && onGround) {
                double x = client.player.getX();
                double y = client.player.getY() + 0.08;
                double z = client.player.getZ();
                for (int i = 0; i < 28; i++) {
                    double angle = Math.PI * 2.0 * i / 28.0;
                    double vx = Math.cos(angle) * 0.035;
                    double vz = Math.sin(angle) * 0.035;
                    client.world.addParticle(ParticleTypes.END_ROD,
                            x + Math.cos(angle) * 0.55, y, z + Math.sin(angle) * 0.55,
                            vx, 0.015, vz);
                }
            }
            wasOnGround = onGround;
            if (playerTrails && !client.player.isOnGround()
                    && client.player.getVelocity().horizontalLengthSquared() > 0.012) {
                if (++trailTicks % 3 == 0) {
                    var velocity = client.player.getVelocity();
                    client.world.addParticle(ParticleTypes.END_ROD,
                            client.player.getX() - velocity.x * 1.5,
                            client.player.getY() + 0.08,
                            client.player.getZ() - velocity.z * 1.5,
                            0.0, 0.01, 0.0);
                }
            } else {
                trailTicks = 0;
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!hitParticles || client.player == null || client.world == null
                    || !(client.crosshairTarget instanceof EntityHitResult hit)
                    || !(hit.getEntity() instanceof LivingEntity target) || target == client.player) {
                previousHitTargetId = -1;
                previousHitTargetHealth = -1.0f;
                return;
            }
            float health = target.getHealth();
            if (target.getId() == previousHitTargetId && previousHitTargetHealth >= 0.0f
                    && health < previousHitTargetHealth) {
                for (int i = 0; i < 8; i++) {
                    double vx = (Math.random() - 0.5) * 0.18;
                    double vy = 0.04 + Math.random() * 0.16;
                    double vz = (Math.random() - 0.5) * 0.18;
                    client.world.addParticle(ParticleTypes.CRIT,
                            target.getX() + (Math.random() - 0.5) * target.getWidth(),
                            target.getY() + target.getHeight() * 0.55,
                            target.getZ() + (Math.random() - 0.5) * target.getWidth(),
                            vx, vy, vz);
                }
            }
            previousHitTargetId = target.getId();
            previousHitTargetHealth = health;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                dancePlaying = false;
                danceEntityId = -1;
                wasDancePlaying = false;
                return;
            }

            var pos = client.player.getPos();
            if (dancePlaying && !wasDancePlaying) {
                lastDanceX = pos.x;
                lastDanceY = pos.y;
                lastDanceZ = pos.z;
                danceStartAge = client.player.age;
            } else if (dancePlaying) {
                double dx = pos.x - lastDanceX;
                double dy = pos.y - lastDanceY;
                double dz = pos.z - lastDanceZ;
                // Stop the emote after actual player movement; tiny floating-point jitter is ignored.
                if (dx * dx + dy * dy + dz * dz > 0.0025
                        || (activeDance == 5 && client.player.age - danceStartAge >= 100.0f)) {
                    dancePlaying = false;
                }
                lastDanceX = pos.x;
                lastDanceY = pos.y;
                lastDanceZ = pos.z;
            }

            wasDancePlaying = dancePlaying;
            danceEntityId = dancePlaying ? client.player.getId() : -1;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                Object particleOption = client.options.getParticles();
                java.lang.reflect.Method getter = particleOption.getClass().getMethod("getValue");
                java.lang.reflect.Method setter = particleOption.getClass().getMethod("setValue", getter.getReturnType());
                if (fpsBoost && previousParticles == null) {
                    previousParticles = getter.invoke(particleOption);
                    Object minimal = java.lang.Enum.valueOf((Class) getter.getReturnType(), "MINIMAL");
                    setter.invoke(particleOption, minimal);
                } else if (!fpsBoost && previousParticles != null) {
                    setter.invoke(particleOption, previousParticles);
                    previousParticles = null;
                }
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                // If this Minecraft version exposes a different option type, leave it untouched.
            }
        });

        // Decorative HUD marker only: it does not target, track, or select entities.
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.hudHidden) return;

            XakaVisualsMusic.renderHud(drawContext, client);

            // Draw the watermark with vanilla text primitives: no external texture/resource can go missing.
            if (watermark) {
                drawContext.fill(6, 6, 92, 29, 0xCC101520);
                drawContext.fill(6, 6, 8, 29, accent());
                drawContext.drawTextWithShadow(client.textRenderer, "Xaka", 13, 8, 0xFFC4B5FD);
                drawContext.drawTextWithShadow(client.textRenderer, "Visuals", 45, 8, 0xFFFFFFFF);
            }

            int hudY = watermark ? 34 : 8;
            if (enemyHpEnabled && client.player != null
                    && client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit
                    && hit.getEntity() instanceof net.minecraft.entity.LivingEntity target
                    && target != client.player && target.isAlive()) {
                float health = Math.max(0.0f, target.getHealth());
                float maxHealth = Math.max(1.0f, target.getMaxHealth());
                int sw = client.getWindow().getScaledWidth();
                int cardW = 136;
                int cardH = 32;
                int sh = client.getWindow().getScaledHeight();
                int cardX = enemyHpX < 0 ? (sw - cardW) / 2 : Math.max(0, Math.min(sw - cardW, enemyHpX));
                int cardY = enemyHpY < 0 ? Math.max(8, sh / 2 - 112) : Math.max(0, Math.min(sh - cardH, enemyHpY));
                int hpColor = health / maxHealth > 0.5f ? 0xFF55D68B
                        : health / maxHealth > 0.25f ? 0xFFFFC15A : 0xFFFF626F;
                drawContext.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xD9101420);
                drawContext.fill(cardX, cardY, cardX + 2, cardY + cardH, accent());
                String targetName = nameProtect ? "Protected Player" : target.getDisplayName().getString();
                if (targetName.length() > 16) targetName = targetName.substring(0, 16);
                drawContext.drawTextWithShadow(client.textRenderer, targetName,
                        cardX + 7, cardY + 4, 0xFFF2F3F8);
                String hpText = String.format(java.util.Locale.ROOT, "%.1f / %.1f HP", health, maxHealth);
                drawContext.drawTextWithShadow(client.textRenderer, hpText,
                        cardX + 7, cardY + 17, 0xFFE2E7F0);
                int barX = cardX + 83;
                int barY = cardY + 23;
                int barW = 45;
                drawContext.fill(barX, barY, barX + barW, barY + 4, 0xFF343B49);
                drawContext.fill(barX, barY, barX + Math.round(barW * Math.min(1.0f, health / maxHealth)),
                        barY + 4, hpColor);
            }
            if (fps) {
                String fpsText = client.getCurrentFps() + " FPS";
                drawContext.drawTextWithShadow(client.textRenderer, fpsText, 8, hudY, accent());
                hudY += 12;
            }
            if (lowArmorNotify && client.player != null) {
                boolean low = false;
                for (int slot = 0; slot < 4; slot++) {
                    var armor = client.player.getInventory().getArmorStack(slot);
                    if (!armor.isEmpty() && armor.getMaxDamage() > 0
                            && armor.getMaxDamage() - armor.getDamage() <= Math.max(1, armor.getMaxDamage() / 5)) {
                        low = true;
                        break;
                    }
                }
                if (low) drawContext.drawTextWithShadow(client.textRenderer, "LOW ARMOR", 8, hudY, 0xFFFFB45E);
                if (low) hudY += 12;
            }
            if (deathCoords && hasDeathLocation) {
                String death = "Last death: " + lastDeathX + " / " + lastDeathY + " / " + lastDeathZ;
                drawContext.drawTextWithShadow(client.textRenderer, death, 8, hudY, 0xFFFF8D8D);
                hudY += 12;
            }
            if (waypointEnabled && client.player != null) {
                double distance = Math.sqrt(client.player.squaredDistanceTo(waypointX + 0.5, waypointY + 0.5, waypointZ + 0.5));
                String waypoint = String.format(java.util.Locale.ROOT, "GPS: %d / %d / %d  (%.0fm)",
                        waypointX, waypointY, waypointZ, distance);
                drawContext.drawTextWithShadow(client.textRenderer, waypoint, 8, hudY, 0xFF8FD8FF);
                hudY += 12;
            }
            if (cooldownHud && client.player != null) {
                int cooldown = Math.round(client.player.getAttackCooldownProgress(0.0f) * 100.0f);
                drawContext.drawTextWithShadow(client.textRenderer, "Attack cooldown: " + cooldown + "%",
                        8, hudY, accent());
                hudY += 12;
            }
            if (coordinates && client.player != null) {
                var pos = client.player.getPos();
                String coordsText = String.format(java.util.Locale.ROOT, "XYZ %.1f / %.1f / %.1f", pos.x, pos.y, pos.z);
                drawContext.drawTextWithShadow(client.textRenderer, coordsText, 8, hudY, 0xFFE7EAF2);
            }

            if (customCrosshair) {
                int centerX = client.getWindow().getScaledWidth() / 2;
                int centerY = client.getWindow().getScaledHeight() / 2;
                int size = Math.max(2, Math.min(12, crosshairSize));
                int gap = 3;
                int thickness = 1;
                int color = accent();
                drawContext.fill(centerX - gap - size, centerY, centerX - gap, centerY + thickness, color);
                drawContext.fill(centerX + gap + 1, centerY, centerX + gap + size + 1, centerY + thickness, color);
                drawContext.fill(centerX, centerY - gap - size, centerX + thickness, centerY - gap, color);
                drawContext.fill(centerX, centerY + gap + 1, centerX + thickness, centerY + gap + size + 1, color);
            }

            if (!markerEnabled) return;
            int cx = client.getWindow().getScaledWidth() / 2;
            int cy = client.getWindow().getScaledHeight() / 2 + 38;
            int size = Math.max(2, Math.min(14, markerSize));
            int color = accent();
            if (markerSquare) {
                drawContext.fill(cx - size, cy - size, cx + size + 1, cy + size + 1, color);
            } else {
                for (int y = -size; y <= size; y++) {
                    int half = (int) Math.floor(Math.sqrt(size * size - y * y));
                    drawContext.fill(cx - half, cy + y, cx + half + 1, cy + y + 1, color);
                }
            }
        });
    }
}
