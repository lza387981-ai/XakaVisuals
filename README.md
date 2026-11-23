# XakaVisuals V3

A new client-side Fabric mod project for Minecraft 1.21.4.

## Build
- Java 21
- Gradle 8.12
- Fabric Loom 1.10
- Fabric Loader 0.16.10
- Fabric API 0.119.4+1.21.4

Run:

```bash
gradle build
```

The JAR is written to `build/libs/`.

## Current development status
- New V3 project branch and Fabric client entrypoint.
- Right Shift menu key.
- Screenshot-inspired tabbed menu shell: Render, HUD, Player, Misc.
- Feature registry contains the requested module names.

The current commit is an initial UI scaffold. Individual render/HUD/player modules, saved configuration, draggable HUD elements, keybind capture, and the final generated Target ESP texture still need implementation and in-game verification.
