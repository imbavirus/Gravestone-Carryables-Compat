# Gravestone Carryables

**NeoForge 1.21.1** addon for [GraveStone Mod](https://modrinth.com/mod/gravestone-mod) (Max Henkel).

When you break your grave, equipped **carryables** go back into the **same slots** they came from — not only into your main inventory.

| System | Examples |
|--------|----------|
| **Curios** | rings, belt, charm, hands, back, cosmetics |
| **Accessories** | Aether gloves, rings, capes, charms |
| **Vanilla** | hotbar, armor, offhand (same indices) |

Full **ItemStack** data is preserved: enchants, custom components, **backpack contents** (e.g. Sophisticated Backpacks), Mekanism modules on armor, etc. Stacks are copied and re-inserted; only a temporary slot marker is added and removed.

---

## Credits & origin

This project is an **expansion** of:

- **[Corpse-Gravestone-Curios-Compat](https://github.com/Leclowndu93150/Corpse-Gravestone-Curios-Compat)** by **[Leclowndu93150](https://github.com/Leclowndu93150)**  
- Published as **[Gravestone x Curios API Compat](https://modrinth.com/mod/gravestone-x-curios-api-compat)** (Modrinth / CurseForge)

Original work: death tagging + Gravestone `fillPlayerInventory` restore for **Curios**.

**Infernos** (this fork): rebranded to **Gravestone Carryables**, added **Accessories**, shared carry-slot metadata, vanilla slot restore, config toggles, and full-stack preservation focus.

Please support the original author if you use Curios-only setups — their mod remains the right choice for a minimal Curios-only patch.

---

## Requirements

| Mod | Role |
|-----|------|
| **NeoForge** 1.21.1 | loader |
| **[GraveStone](https://modrinth.com/mod/gravestone-mod)** | graves |
| **[BaguetteLib](https://modrinth.com/mod/baguettelib)** | reliable pre-death events |
| **[Curios](https://modrinth.com/mod/curios)** | *optional* — Curios restore |
| **[Accessories](https://modrinth.com/mod/accessories)** | *optional* — Accessories restore |

At least one of Curios / Accessories is recommended. Vanilla inventory restore works without either.

**Server config tip:** set Gravestone `break_pickup = true` so items are sorted back into inventory/equipment instead of only dumped as loose loot.

---

## Configuration

File: `config/gravestonecarryables-common.toml`

| Option | Default | Description |
|--------|---------|-------------|
| `blacklisted_items` | `[]` | Item ids (`modid:item`) that never auto-restore into equipment |
| `transfer_cursed_items` | `false` | Allow Curse of Binding gear back into equipment slots |
| `restore_curios` | `true` | Re-equip Curios when the mod is present |
| `restore_accessories` | `true` | Re-equip Accessories when the mod is present |
| `restore_vanilla_slots` | `true` | Restore main / armor / offhand to original indices |

---

## How it works

1. **On death** (high priority, via BaguetteLib): tag each equipped stack with system + slot type + index (+ cosmetic flag).  
2. Gravestone stores those stacks as usual.  
3. **On grave break** (`fillPlayerInventory` overwrite): read tags and put stacks back into Curios / Accessories / vanilla slots; clean the marker; leftover items go to inventory or overflow.

**Note:** Curios/Accessories **slot types** must be registered on the player by the pack (other mods’ entity datapacks, or a small slots datapack). This mod restores into existing slots; it does not invent belt/charm slots by itself.

---

## Build

```bat
gradlew.bat build
```

Output: `build/libs/gravestonecarryables-1.21.1-NeoForge-<version>.jar`

---

## License

**MIT** — see [LICENSE](LICENSE).

Includes copyright for **Leclowndu93150** (original) and **Infernos** (expansion).

---

## Links

- Source: https://github.com/imbavirus/Gravestone-Carryables-Compat  
- Upstream (original Curios compat): https://github.com/Leclowndu93150/Corpse-Gravestone-Curios-Compat  
- Gravestone: https://modrinth.com/mod/gravestone-mod  
