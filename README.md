# BambooFPS

**BambooFPS** is a high-performance client-side Fabric mod for **Minecraft Java 1.21.11** designed to eliminate client-side FPS lag near massive automated farms (such as bamboo farms, hoppers, dispensers, crafters, and pistons) without modifying any server-side mechanics.

---

## ⚡ Features

1. **Hide Blocks**:
   - Mixin on `AbstractBlockState.getRenderType()` returning `BlockRenderType.INVISIBLE` for specified blocks.
   - Compatible with Sodium / Embeddium and Vanilla chunk meshers.
   - Default blocks: `minecraft:hopper`, `minecraft:dispenser`, `minecraft:piston`, `minecraft:sticky_piston`, `minecraft:crafter`, `minecraft:bamboo`.

2. **Skip Block Entity Renderers**:
   - Skips client-side rendering of listed block entities and moving piston animations (`PistonBlockEntity`).

3. **Hide Dropped Item Entities**:
   - Skips entity rendering for specified items (e.g., dropped bamboo, bones, bone meal, scaffolding, bamboo blocks).

4. **Cancel Farm Particles & World Events**:
   - Cancels block breaking/breaking particles for hidden blocks.
   - Cancels particle and sound world events for bone meal usage (`1505`), crafter operations (`1049`, `1050`), and dispenser triggers (`1000`, `1001`, `1002`).

5. **Master Toggle Keybind**:
   - Default key: `B` (Category: `BambooFPS`).
   - Actionbar notification (`BambooFPS: ON` / `BambooFPS: OFF`).
   - Triggers instantaneous chunk re-render on toggle.

---

## ⚙️ Configuration (`config/bamboofps.json`)

The mod automatically generates `config/bamboofps.json` on first launch and watches the file on disk using `WatchService` for instant hot-reloads without needing to restart the game.

```json
{
  "enabled": true,
  "hideBlocks": true,
  "hideBlockEntities": true,
  "hideItems": true,
  "cancelParticles": true,
  "hiddenBlocks": [
    "minecraft:hopper",
    "minecraft:dispenser",
    "minecraft:piston",
    "minecraft:sticky_piston",
    "minecraft:crafter",
    "minecraft:bamboo"
  ],
  "hiddenItems": [
    "minecraft:bamboo",
    "minecraft:bone",
    "minecraft:bone_meal",
    "minecraft:scaffolding",
    "minecraft:bamboo_block"
  ]
}
```

Supports block/item tags using the `#` prefix (e.g. `"#minecraft:beds"`).

---

## 💬 Client Commands

All commands are client-side only via Fabric API:

- `/bamboofps toggle` — Toggles the mod on or off.
- `/bamboofps reload` — Reloads configuration from disk.
- `/bamboofps list` — Lists enabled features and all hidden blocks/items.
- `/bamboofps add block <id>` — Adds a block ID or tag (`#tag:id`) to the hidden blocks list.
- `/bamboofps remove block <id>` — Removes a block ID or tag from the hidden blocks list.
- `/bamboofps add item <id>` — Adds an item ID or tag to the hidden items list.
- `/bamboofps remove item <id>` — Removes an item ID or tag from the hidden items list.

---

## 🔍 Verified Mixin Target Map (Yarn 1.21.11+build.4)

| Mixin Class | Target Class | Target Method & Signature | Action |
|-------------|--------------|---------------------------|--------|
| `AbstractBlockStateMixin` | `net.minecraft.block.AbstractBlock$AbstractBlockState` | `getRenderType()Lnet/minecraft/block/BlockRenderType;` | Returns `BlockRenderType.INVISIBLE` if block is hidden |
| `BlockEntityRenderDispatcherMixin` | `net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher` | `render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V` | Cancels rendering of matching block entities & moving pistons |
| `ItemEntityRendererMixin` | `net.minecraft.client.render.entity.ItemEntityRenderer` | `render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V` | Cancels rendering of matching dropped item entities |
| `ParticleManagerMixin` | `net.minecraft.client.particle.ParticleManager` | `addBlockBreakParticles(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V`<br>`addBlockBreakingParticles(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)V` | Cancels break/hit particles for hidden blocks |
| `WorldRendererMixin` | `net.minecraft.client.render.WorldRenderer` | `processWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V` | Cancels bone meal, dispenser, crafter, and block break event particles |

---

## 🛠️ CI / CD Workflows

- **`.github/workflows/build.yml`**: Compiles the project with JDK 21 on all pushes and pull requests.
- **`.github/workflows/release.yml`**: Triggers on push to `main`, bumps version in `gradle.properties` (patch by default, `#minor`, `#major`), compiles, creates a GitHub Release with the mod `.jar`, and sends the `.jar` + release notes to Discord via Webhook.

---

## 📜 License

MIT License © 2026 itz0cat
