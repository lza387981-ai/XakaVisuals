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
- [ ] Reimplement visual modules and their settings
- [ ] Reimplement client and utility modules
- [ ] Port/replace approved textures, PNGs, fonts, sounds and JSON assets
- [ ] Audit nested dependencies and their licenses
- [ ] Build and launch-test on Minecraft 1.21.4

A feature is marked complete only after its implementation and in-game behavior have been tested. Decompiled bytecode is not treated as original source code.
