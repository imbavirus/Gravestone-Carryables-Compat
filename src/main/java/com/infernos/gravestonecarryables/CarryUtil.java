package com.infernos.gravestonecarryables;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class CarryUtil {
    private CarryUtil() {}

    public static boolean shouldSkipRestore(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (Config.isItemBlacklisted(stack.getItem())) {
            return true;
        }
        return !Config.TRANSFER_CURSED_ITEMS.get() && isBindingCursed(stack);
    }

    public static boolean isBindingCursed(ItemStack stack) {
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var entry : enchants.entrySet()) {
            if (entry.getKey().is(Enchantments.BINDING_CURSE)) {
                return true;
            }
        }
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var entry : stored.entrySet()) {
            if (entry.getKey().is(Enchantments.BINDING_CURSE)) {
                return true;
            }
        }
        return false;
    }

    /** Copy stack and strip our temporary marker so the restored item is clean. */
    public static ItemStack cleanCopy(ItemStack stack) {
        ItemStack copy = stack.copy();
        CarrySlotDataComponent.clear(copy);
        return copy;
    }
}
