# XakaVisual.V2

A new, independent Fabric client mod for Minecraft 1.21.4. This project is developed separately from the original XakaVisuals codebase.

## Current foundation
- Fabric client entrypoint
- Right Shift menu keybind
- New dark click-GUI shell with Visual / HUD / Utility / Cosmetics categories
- Original module registry placeholders to be wired to real implementations

## Development plan
1. Finish a safe static inventory of the EnoughVisuals reference JAR.
2. Reimplement selected functionality in original XakaVisual.V2 code.
3. Add original artwork and only reuse third-party assets when their license or permission allows it.
4. Implement settings, persistence, HUD editor, pets/emotes and visual effects in stages.
5. Build and test on Minecraft 1.21.4 / Fabric.

The supplied EnoughVisuals 2.6 JAR identifies its mod license as `All rights reserved`; its bundled `LICENSE_enoughvisuals` file contains GPL-3.0 text. These notices are inconsistent, so this project does not copy its compiled classes or artwork. Its feature set is used as a technical reference while licensing is clarified.
