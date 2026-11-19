# XakaVisuals.V2

Standalone Fabric client mod for Minecraft 1.21.4. Development happens on the `xakavisuals-v2` branch; the `main` branch is left untouched.

## Current V2 foundation
- Right Shift opens and closes the new click GUI.
- Separate Visual, Cosmetics, HUD and Client sections (no CheatV2 tab).
- Purple/charcoal XAKA interface.
- XAKA watermark and FPS counter HUD.
- Settings saved to `config/xakavisuals.properties`.
- GitHub Actions workflow builds the branch with Java 21 and Gradle 8.12.

## EnoughVisuals 2.6 inventory
The supplied JAR contains 604 application class files, 321 PNG files and 84 JSON files. The feature-class inventory is tracked in [PORTING_STATUS.md](PORTING_STATUS.md).

The V2 menu is a foundation, not a completed port. Features are being reimplemented and tested one by one; a menu switch is not considered complete until it changes an in-game behavior.

## Build
Use JDK 21 and Gradle 8.12:

```sh
gradle build
```

The resulting mod JAR is in `build/libs/`. GitHub Actions uploads the JAR as a workflow artifact when a push or pull request targets `xakavisuals-v2`.

## Third-party notices
The supplied EnoughVisuals archive includes a GPL-3.0 license text, while its embedded Fabric metadata reports a conflicting `All rights reserved` value. The exact scope of those notices and the licenses of nested dependencies/assets must be checked before redistributing copied material. Preserve all applicable notices and source-code obligations.
