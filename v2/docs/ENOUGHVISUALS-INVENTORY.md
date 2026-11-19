# EnoughVisuals 2.6 — static feature/resource inventory

Source examined: user-supplied `enoughvisuals-2.6.jar` (SHA-256: `e3598cb6b2ee938bf4890495269cc5a7da6e825d97898874cc618beb3b3089d1`).
This is an inventory only; the JAR was not executed.

## Container and compatibility
- Fabric client mod, Minecraft 1.21.4, Java 21.
- Mod id: `enoughvisuals`, version `2.6`.
- Main client entrypoint: `fun.enoughvisuals.Enough`.
- Mixin configuration: `enoughvisuals.mixins.json`.
- 604 top-level class files and 5 nested JARs.

## Module names found in bytecode

### Visual modules
Ambience, Animations, AspectRatio, AtmoDawnFog, AttackEffect, BlockOverlay, ChinaHat, ColorPlus, Cooldowns, Crosshair, CustomHitbox, FreeLook, FriendMarker, Fullbright, HealthIndicator, HitColor, HitParticles, HitRange, ItemLight, ItemPhysics, JumpCircle, JumpCube, JumpEffect, KillEffect, MaceIndicator, MotionBlur, MouseParticles, NoFluid, NoRender, Particles, Prediction, ShaderHands, ShaderPlayer, ShulkerPreview, SkyBox, SwingAnimation, Svetoch, TargetESP, TotemTracker, Trails, ViewModel, WebPanels, WorldCubes, WorldTweaks, Zoom.

### Utility modules
AutoAccept, AutoNear, AutoReconnect, AutoRespawn, AutoSprint, ChatHelper, ClickFriend, ClientGuiModule, CustomSphereSkin, DeathCords, FTHelper, FakePlayer, FastSearch, FastSwap, HealingHelper, HitSound, ItemPickupLogger, ItemScroller, ItemSwap, KillSound, LockSlot, LowArmorNotify, Macros, NameProtect, Optimization, PvPSave, SendCord, SpecBind, StreamerMode, TrapTimer.

### Client modules
Configs, CustomTheme, Emotions, Friends, GPS, Globals, Graffiti, GuiSize, HUD.

### Other systems found
- HUD rendering, layout, drag-and-drop, viewport/grid, animation, notifications and speed meter.
- Emotion/animation model parsing and rendering.
- Cosmetics and pet renderers.
- Graffiti catalog/texture manager.
- Inventory layout, accounts, GPS, network/event feed and web panels.
- Config/settings framework and custom rendering utilities.

## Asset counts
- 321 PNG files
- 84 JSON files
- 23 OGG sound files
- 5 GLB models
- 58 shader source files (.fsh/.vsh/.glsl)
- 5 nested JAR libraries
- 2 DLL files

Notable asset groups include fonts/MSDF atlases, HUD/category icons, logos, textures, shaders, sound effects, emotion models/rigs, cosmetic catalog fragments and custom 3D models.

## Security review notes
- Static strings indicate network/API integration and UUID/token-related code paths; that alone does not establish malicious behavior.
- The archive contains browser-related components and native DLLs. Do not run the JAR as part of static review.
- The metadata says `All rights reserved`, while a separate `LICENSE_enoughvisuals` file contains GPL-3.0 text. These notices conflict; obtain clarification from the author before copying compiled code or artwork.
- XakaVisual.V2 will use the feature inventory as a design reference and use independently written implementations and original/appropriately licensed assets until permissions are clear.
