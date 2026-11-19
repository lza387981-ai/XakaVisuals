# XakaVisuals.V2

Standalone client-side Fabric mod for Minecraft 1.21.4 / Java 21.

## Current V2 foundation
- Independent mod id: `xakavisuals`
- Right Shift opens the new V2 settings screen.
- Separate Visual, Cosmetics, HUD and CheatV2 tabs.
- Local watermark and FPS HUD options.
- Independent settings model; no dependency on EnoughVisuals classes or bundled executable libraries.

## Build
Run with Java 21 and Gradle 8.12:

```sh
gradle build
```

The distributable JAR is created in `build/libs/`.

## Resource migration
The supplied EnoughVisuals archive contains compiled classes and resources, not the original preferred Java source form. V2 is being built as an independent implementation. Only assets that are cleared for reuse will be migrated; bundled executable JARs are intentionally not copied into the new mod.

## License
The V2 implementation is released under GPL-3.0-or-later. Third-party assets retain their own notices and licenses.
