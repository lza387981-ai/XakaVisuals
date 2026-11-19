# XakaVisuals.V2

Standalone client-side Fabric mod for Minecraft 1.21.4 / Java 21.

## Current V2 foundation
- Independent mod id: `xakavisuals`
- Right Shift opens and closes the V2 settings screen.
- Separate Visual, Cosmetics, HUD and Client sections. There is no CheatV2 tab.
- XAKA watermark and FPS HUD options.
- Settings are stored locally in `config/xakavisuals.properties`.
- V2 does not load EnoughVisuals classes or its bundled executable libraries.

## Build
Run with Java 21 and Gradle 8.12:

```sh
gradle build
```

The distributable JAR is created in `build/libs/`. GitHub Actions builds this branch and uploads the JAR as an artifact.

## Resource migration
The supplied EnoughVisuals archive contains compiled classes and resources, not the original preferred Java source form. V2 is being implemented independently. The uploaded archive contains 604 application class files, 321 PNG files and 84 JSON files; see [PORTING_STATUS.md](PORTING_STATUS.md) for the feature inventory and current status.

## License and notices
The supplied archive contains a GPL-3.0 license text, while its Fabric metadata reports `All rights reserved`. The scope of those notices and the licenses of nested dependencies/assets must be checked before redistributing copied material. Preserve all applicable notices and source-code obligations.
