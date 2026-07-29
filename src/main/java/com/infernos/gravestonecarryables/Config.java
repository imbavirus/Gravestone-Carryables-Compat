package com.infernos.gravestonecarryables;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class Config {
    public static final ModConfigSpec COMMON;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEMS;
    public static final ModConfigSpec.BooleanValue TRANSFER_CURSED_ITEMS;
    public static final ModConfigSpec.BooleanValue RESTORE_CURIOS;
    public static final ModConfigSpec.BooleanValue RESTORE_ACCESSORIES;
    public static final ModConfigSpec.BooleanValue RESTORE_VANILLA_SLOTS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("General settings").push("general");

        BLACKLISTED_ITEMS = b
                .comment("Items that should not be auto-restored into equipment slots (format: 'modid:item')")
                .defineList("blacklisted_items", List.of(), entry -> entry instanceof String);

        TRANSFER_CURSED_ITEMS = b
                .comment("Whether items with Curse of Binding should be restored into equipment slots")
                .define("transfer_cursed_items", false);

        RESTORE_CURIOS = b
                .comment("Restore Curios slots when Curios is present")
                .define("restore_curios", true);

        RESTORE_ACCESSORIES = b
                .comment("Restore Accessories slots when Accessories is present (Aether gloves, etc.)")
                .define("restore_accessories", true);

        RESTORE_VANILLA_SLOTS = b
                .comment("Restore main inventory / armor / offhand into the same slots they came from")
                .define("restore_vanilla_slots", true);

        b.pop();
        COMMON = b.build();
    }

    private Config() {}

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON);
    }

    public static boolean isItemBlacklisted(Item item) {
        return BLACKLISTED_ITEMS.get().contains(BuiltInRegistries.ITEM.getKey(item).toString());
    }
}
