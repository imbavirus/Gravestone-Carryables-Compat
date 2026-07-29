package com.infernos.gravestonecarryables.handler;

import com.infernos.gravestonecarryables.CarrySlotDataComponent;
import com.infernos.gravestonecarryables.CarryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.Optional;

/**
 * Tags and restores Curios equipment. Full ItemStacks keep backpack contents / modules / enchants.
 */
public final class CuriosCarryHandler {
    private CuriosCarryHandler() {}

    public static void tagEquipped(Player player) {
        Optional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(player);
        if (opt.isEmpty()) {
            return;
        }
        ICuriosItemHandler handler = opt.get();
        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            String slotType = entry.getKey();
            ICurioStacksHandler stacksHandler = entry.getValue();

            IDynamicStackHandler stacks = stacksHandler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack stack = stacks.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    CarrySlotDataComponent.tag(stack, CarrySlotDataComponent.System.CURIOS, slotType, i, false);
                }
            }

            IDynamicStackHandler cosmetics = stacksHandler.getCosmeticStacks();
            for (int i = 0; i < cosmetics.getSlots(); i++) {
                ItemStack stack = cosmetics.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    CarrySlotDataComponent.tag(stack, CarrySlotDataComponent.System.CURIOS, slotType, i, true);
                }
            }
        }
    }

    public static boolean tryRestore(ItemStack stack, Player player) {
        CarrySlotDataComponent.CarrySlotData data = CarrySlotDataComponent.get(stack);
        if (data == null || data.system() != CarrySlotDataComponent.System.CURIOS || !data.wasEquipped()) {
            return false;
        }
        if (CarryUtil.shouldSkipRestore(stack)) {
            return false;
        }

        Optional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(player);
        if (opt.isEmpty()) {
            return false;
        }

        Map<String, ICurioStacksHandler> curios = opt.get().getCurios();
        if (tryPlace(stack, curios, data.slotType(), data.slotIndex(), data.cosmetic())) {
            return true;
        }
        return tryAnyEmptyMatching(stack, curios, data.cosmetic());
    }

    private static boolean tryPlace(ItemStack stack, Map<String, ICurioStacksHandler> curios, String slotType, int slotIndex, boolean cosmetic) {
        ICurioStacksHandler handler = curios.get(slotType);
        if (handler == null) {
            return false;
        }
        IDynamicStackHandler dyn = cosmetic ? handler.getCosmeticStacks() : handler.getStacks();
        if (slotIndex < 0 || slotIndex >= dyn.getSlots()) {
            return false;
        }
        ItemStack existing = dyn.getStackInSlot(slotIndex);
        if (existing.isEmpty()) {
            dyn.setStackInSlot(slotIndex, CarryUtil.cleanCopy(stack));
            return true;
        }
        return false;
    }

    private static boolean tryAnyEmptyMatching(ItemStack stack, Map<String, ICurioStacksHandler> curios, boolean cosmetic) {
        var tags = CuriosApi.getCuriosHelper().getCurioTags(stack.getItem());
        for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
            if (!tags.contains(entry.getKey())) {
                continue;
            }
            IDynamicStackHandler dyn = cosmetic ? entry.getValue().getCosmeticStacks() : entry.getValue().getStacks();
            for (int i = 0; i < dyn.getSlots(); i++) {
                if (dyn.getStackInSlot(i).isEmpty()) {
                    dyn.setStackInSlot(i, CarryUtil.cleanCopy(stack));
                    return true;
                }
            }
        }
        return false;
    }

    /** Pull tagged curios out of grave inventory lists into equipment. */
    @SafeVarargs
    public static void restoreFromLists(Player player, NonNullList<ItemStack>... inventories) {
        for (NonNullList<ItemStack> inv : inventories) {
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.get(i);
                if (stack.isEmpty()) {
                    continue;
                }
                CarrySlotDataComponent.CarrySlotData data = CarrySlotDataComponent.get(stack);
                if (data == null || data.system() != CarrySlotDataComponent.System.CURIOS) {
                    continue;
                }
                if (tryRestore(stack, player)) {
                    inv.set(i, ItemStack.EMPTY);
                }
            }
        }
    }
}
