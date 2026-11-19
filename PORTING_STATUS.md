# EnoughVisuals 2.6 → XakaVisuals.V2 port status

This inventory was extracted from the uploaded `enoughvisuals-2.6(2).jar`. It records class names, not a claim that every class has already been ported.

## Archive inventory
- 604 application `.class` files
- 321 PNG images
- 84 JSON files
- 37 fragment shaders, 24 fragment shader variants, 20 vertex shaders and 5 GLB models
- 5 nested dependency JARs under `META-INF/jars/`
- 2 Windows DLL files

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
- [x] Attack cooldown percentage HUD
- [x] GPS waypoint capture, distance and clear controls
- [x] NameProtect for target-health HUD names
- [x] Custom crosshair with adjustable length, gap, thickness, center dot and outline
- [x] Target marker with adjustable size, offsets and color palette
- [x] Player halo and body marker with shape/size/position controls
- [x] Local floating Creeper, Wither, Warden and Allay companions
- [x] Original procedural cape and hat cosmetics
- [x] ChinaHat: local procedural 3D conical hat style inspired by the source visual module
- [x] First-person hand/item position controls
- [x] Six local player emote presets with speed control
- [x] Reversible FPS Boost (restores the prior particle setting)
- [x] Hit particles when the crosshair-target's health decreases
- [x] Jump landing particle circles
- [x] Airborne movement particle trails
- [x] NoFluid underwater overlay toggle
- [x] No-fire first-person overlay toggle
- [x] NoHurtCam: disable first-person damage camera tilt
- [x] JumpCircle and Trails controls
- [x] JumpEffect: local expanding double-ring effect with radius and duration settings
- [x] JumpCube: local takeoff-triggered rotating cube particles with count and size settings
- [x] MouseParticles: local cursor-following sparkle trail in the menu with size control
- [x] CI compile/build checks for the feature batches above
- [x] NoWeather: hide local rain/snow rendering, particles and sound
- [x] HideBossBar: optional local boss-bar HUD suppression
- [x] NoInWallOverlay: optional local suffocation-overlay suppression
- [x] ViewModel: first-person hand/item translation and rotation across all three axes
- [x] Crosshair: custom reticle with adjustable gap, length, thickness, center dot and outline
- [x] Ambience: client-only visual time override with slider and sunrise/noon/sunset/midnight presets; server time is not modified
- [ ] Reimplement remaining visual modules and their settings
- [x] AutoSprint
- [x] Low-armor warning
- [x] Last-death coordinates HUD
- [ ] Reimplement remaining client and utility modules
- [ ] Port/replace approved textures, PNGs, fonts, sounds and JSON assets
- [ ] Audit nested dependencies and their licenses
- [ ] Launch-test the mod in Minecraft 1.21.4 and verify rendering/input in-game

## Security review gate
- [x] Static inventory of embedded classes, assets, nested JARs and native DLLs
- [x] Inspect the remote configuration and device-identification flow
- [ ] Review remaining networked systems and nested dependencies individually
- [ ] Decide which optional network features can be redesigned as opt-in, transparent Xaka services

## Implemented but awaiting in-game verification
- NoWeather: local rain/snow rendering and weather particles/sound toggle; implementation is CI-compiled, awaiting in-game verification.
- HideBossBar: optional local boss-bar HUD suppression; implementation is CI-compiled, awaiting in-game verification.
- NoInWallOverlay: local suffocation-overlay suppression; implementation is CI-compiled, awaiting in-game verification.
- JumpEffect: local takeoff-triggered double ring with adjustable radius and duration; implementation is CI-compiled, awaiting in-game verification.
- JumpCube: local takeoff-triggered wireframe cubes with adjustable count and size; implementation is CI-compiled, awaiting in-game verification.
- MouseParticles: local menu cursor sparkle trail with size control; implementation is CI-compiled, awaiting in-game verification.
- ViewModel: local first-person item transform with XYZ position and rotation sliders; implementation is CI-compiled, awaiting in-game verification.
- Crosshair: local custom reticle with gap, thickness, dot and outline controls; implementation is CI-compiled, awaiting in-game verification.
- Ambience: client-only time-of-day override with saved local setting and quick presets; implementation is CI-compiled, awaiting in-game verification.

See [SECURITY_REVIEW.md](SECURITY_REVIEW.md) for the findings and the porting exclusions.

A feature is marked complete only after its implementation and in-game behavior have been tested. Decompiled bytecode is not treated as original source code.
