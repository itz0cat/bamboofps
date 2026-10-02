Build a Fabric client-side mod called BambooFPS for Minecraft Java 1.21.11 (Java 21, Fabric Loader + Fabric API, Loom). Mod ID: bamboofps. Package: dev.itz0cat.bamboofps. Repo: github.com/itz0cat/bamboofps. Client-only (environment: client). Do NOT guess class, method or event names: look them up in the mappings for 1.21.11 and verify before using them.

PURPOSE
Fixes client-side FPS lag near big farms (bamboo, hoppers, crafters, pistons) by not rendering things. Server-side behavior is untouched.

FEATURES (each one toggleable)
1. Hide blocks: default list = hopper, dispenser, piston, sticky_piston, crafter, bamboo. Mixin on the block state render type returning INVISIBLE for blocks in the list. Must work with Sodium.
2. Block entity renderers: skip rendering for listed blocks (piston moving part, hopper, dispenser, crafter, chest). Include the piston slide animation.
3. Hide item entities whose item is in an item list (default: bamboo, bone, bone_meal, scaffolding, bamboo_block).
4. Cancel particles: bamboo/block break particles for listed blocks, and world events for bone meal, crafter and dispenser (verify IDs in WorldEvents).
5. Master toggle keybind (default: B, category "BambooFPS") with a chat/actionbar message "BambooFPS: ON/OFF".

NO REBUILD NEEDED FOR NEW BLOCKS (important)
- Config file: config/bamboofps.json (Gson). Contains: enabled, hiddenBlocks[], hiddenItems[], and a boolean for each feature. Create defaults on first launch.
- Keep the live lists in thread-safe sets (volatile / concurrent), read by the mixins.
- Client commands (Fabric API client commands): /bamboofps add block <id>, remove block <id>, add item <id>, remove item <id>, list, reload, toggle. Validate IDs against the registry, and give tab completion from the registry.
- Auto-reload when the JSON file changes on disk (WatchService), and on /bamboofps reload.
- After any change to the block list, trigger a chunk re-render so blocks hide or show immediately.
- Optional: support block tags (e.g. "#minecraft:beds").
- Optional: ModMenu integration if it's available. Not required.

GITHUB ACTIONS
- Do not rely on building locally (it runs on a phone via Termux). CI must do all building.
- .github/workflows/build.yml: on push and pull_request, set up JDK 21, Gradle cache, run ./gradlew build, upload jar artifacts.
- .github/workflows/release.yml: on push to main (ignore commits whose message contains [skip ci]):
  a. Auto-bump the version in gradle.properties (mod_version): patch by default; minor if the commit message contains "#minor", major if it contains "#major".
  b. Build the jar with the new version. The jar name and fabric.mod.json must use ${version} from gradle.properties.
  c. Commit the bump back as github-actions[bot] with the message "chore: bump version to X.Y.Z [skip ci]".
  d. Create the tag vX.Y.Z and a GitHub Release with the jar attached and auto-generated notes.
  e. Use permissions: contents: write and avoid infinite loops.

PROJECT FILES
build.gradle, gradle.properties (mod_version=1.0.0, minecraft_version, loader_version, fabric_version), settings.gradle, gradle wrapper, fabric.mod.json, bamboofps.mixins.json, src/client/... if using split sources, README (features, commands, config example, install), .gitignore, LICENSE (MIT).

RULES
- Keep mixins small and null-safe, and never crash the game if the config is bad (fall back to defaults and log a warning).
- Before finishing, list every mixin target class and method you used and the mapping names, so I can check them.
- Commit in small steps with clear messages and push to the repo.
