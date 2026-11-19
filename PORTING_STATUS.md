# EnoughVisuals 2.6 → XakaVisuals.V2 port status

This inventory was extracted from the uploaded `enoughvisuals-2.6.jar`. It records class names, not a claim that every class has already been ported.

## Archive inventory
- 604 application `.class` files
- 321 PNG images
- 84 JSON files
- Several nested dependency JARs under `META-INF/jars/`

## Visual modules
Ambience, Animations, AspectRatio, AtmoDawnFog, AttackEffect, BlockOverlay, ChinaHat, ColorPlus, Cooldowns, Crosshair, CustomHitbox, FreeLook, FriendMarker, Fullbright, HealthIndicator, HitColor, HitParticles, HitRange, ItemLight, ItemPhysics, JumpCircle, JumpCube, JumpEffect, KillEffect, MaceIndicator, MotionBlur, MouseParticles, NoFluid, NoRender, Particles, Prediction, ShaderHands, ShaderPlayer, ShulkerPreview, SkyBox, SwingAnimation, TargetESP, TotemTracker, Trails, ViewModel, WebPanels, WorldCubes, WorldTweaks, Zoom.

## Client modules
Configs, CustomTheme, Emotions, Friends, GPS, Globals, Graffiti, GuiSize, HUD.

## Utility modules
AutoAccept, AutoNear, AutoReconnect, AutoRespawn, AutoSprint, ChatHelper, ClickFriend, ClientGuiModule, CustomSphereSkin, DeathCords, FTHelper, FakePlayer, FastSearch, FastSwap, HealingHelper, HitSound, ItemPickupLogger, ItemScroller, ItemSwap, KillSound, LockSlot, LowArmorNotify, Macros, NameProtect, Optimization, PvPSave, SendCord, SpecBind, StreamerMode, TrapTimer.

## Port status
- [x] New independent V2 branch and Fabric project scaffold
- [x] Right Shift menu key
- [x] New menu shell and section navigation
- [x] XAKA watermark and FPS HUD switches
- [x] Persistent config foundation
- [x] Fullbright gamma toggle with restoration of the previous gamma value, including world exit
- [x] Hold-to-zoom keybind (C) with restoration of the previous FOV, including world exit
- [x] Coordinates, FPS, watermark and target health HUD
- [x] Draggable target-health HUD position editor
- [x] Custom crosshair with size setting
- [x] Target marker with adjustable size, offsets and color palette
- [x] Player halo and body marker with shape/size/position controls
- [x] Local floating Creeper, Wither, Warden and Allay companions
- [x] Original procedural cape and hat cosmetics
- [x] First-person hand/item position controls
- [x] Six local player emote presets with speed control
- [x] Reversible FPS Boost (restores the prior particle setting)
- [x] Hit particles when the crosshair-target's health decreases
- [x] Jump landing particle circles
- [x] Airborne movement particle trails
- [x] NoFluid underwater overlay toggle
- [x] No-fire first-person overlay toggle
- [ ] Reimplement remaining visual modules and their settings
- [x] AutoSprint
- [x] Low-armor warning
- [x] Last-death coordinates HUD
- [ ] Reimplement remaining client and utility modules
- [ ] Port/replace approved textures, PNGs, fonts, sounds and JSON assets
- [ ] Audit nested dependencies and their licenses
- [x] CI compile/build checks for the feature batches above
- [ ] Launch-test the mod in Minecraft 1.21.4 and verify rendering/input in-game

A feature is marked complete only after its implementation and in-game behavior have been tested. Decompiled bytecode is not treated as original source code.
