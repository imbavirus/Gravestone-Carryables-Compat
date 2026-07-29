package com.infernos.gravestonecarryables.handler;

import com.infernos.gravestonecarryables.CarrySlotDataComponent;
import com.infernos.gravestonecarryables.Config;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * Soft-dispatches to optional equipment APIs so missing mods do not hard-crash class load.
 * Handler classes still need those mods on the compile/runtime classpath when present.
 */
public final class CarrySystems {
    private CarrySystems() {}

    public static void tagAll(Player player) {
        if (Config.RESTORE_CURIOS.get() && ModList.get().isLoaded("curios")) {
            CuriosCarryHandler.tagEquipped(player);
        }
        if (Config.RESTORE_ACCESSORIES.get() && ModList.get().isLoaded("accessories")) {
            AccessoriesCarryHandler.tagEquipped(player);
        }
    }

    @SafeVarargs
    public static void restoreEquipmentFromLists(Player player, NonNullList<ItemStack>... inventories) {
        if (Config.RESTORE_CURIOS.get() && ModList.get().isLoaded("curios")) {
            CuriosCarryHandler.restoreFromLists(player, inventories);
        }
        if (Config.RESTORE_ACCESSORIES.get() && ModList.get().isLoaded("accessories")) {
            AccessoriesCarryHandler.restoreFromLists(player, inventories);
        }
    }

    public static boolean tryRestoreTagged(ItemStack stack, Player player) {
        CarrySlotDataComponent.CarrySlotData data = CarrySlotDataComponent.get(stack);
        if (data == null || !data.wasEquipped()) {
            return false;
        }
        return switch (data.system()) {
            case CURIOS -> Config.RESTORE_CURIOS.get()
                    && ModList.get().isLoaded("curios")
                    && CuriosCarryHandler.tryRestore(stack, player);
            case ACCESSORIES -> Config.RESTORE_ACCESSORIES.get()
                    && ModList.get().isLoaded("accessories")
                    && AccessoriesCarryHandler.tryRestore(stack, player);
            case VANILLA -> false;
        };
    }
}
