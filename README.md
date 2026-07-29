# Gravestone Carryables Compat

Local fork/expansion of [Corpse-Gravestone-Curios-Compat](https://github.com/Leclowndu93150/Corpse-Gravestone-Curios-Compat) (Leclowndu93150).

Restores equipped **carryables** into the slots they came from when you break a Max Henkel **Gravestone**:

- **Curios** (rings, hands, back, etc.)
- **Accessories** (Aether gloves, rings, capes, …)
- **Vanilla** main inventory, armor, offhand (same indices)
- **Full `ItemStack` data** — backpack contents (e.g. Sophisticated Backpacks components), enchants, custom NBT/components, modules, etc. are kept because stacks are copied, not rebuilt

## Status

- Local only under `git/infernos/minecraft-mods/Gravestone-Carryables-Compat`
- **No GitHub fork/push** (upstream remote is fetch-only)
- Not published; not in the Infernos pack yet

## Build

```bat
gradlew.bat build
```

Jar: `build/libs/gravestonecarryables-1.21.1-NeoForge-1.0.0.jar`

## Config (`gravestonecarryables-common.toml`)

| Key | Default | Meaning |
|-----|---------|---------|
| `blacklisted_items` | `[]` | Item ids never auto-restored to equipment |
| `transfer_cursed_items` | `false` | Allow Curse of Binding gear back into slots |
| `restore_curios` | `true` | Curios re-equip |
| `restore_accessories` | `true` | Accessories re-equip |
| `restore_vanilla_slots` | `true` | Same-slot inventory/armor/offhand |

## Credits

- Original Curios + Gravestone approach: **Leclowndu93150**
- Carryables rename + Accessories / multi-system expansion: **Infernos**
