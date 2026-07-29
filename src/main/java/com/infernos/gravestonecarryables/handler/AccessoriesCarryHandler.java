package com.infernos.gravestonecarryables.handler;

import com.infernos.gravestonecarryables.CarrySlotDataComponent;
import com.infernos.gravestonecarryables.CarryUtil;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import io.wispforest.accessories.impl.ExpandedSimpleContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tags and restores Accessories equipment (Aether gloves, rings, etc.).
 * Full stacks preserve all components / nested inventory data.
 */
public final class AccessoriesCarryHandler {
    private AccessoriesCarryHandler() {}

    public static void tagEquipped(Player player) {
        Optional<AccessoriesCapability> opt = AccessoriesCapability.getOptionally(player);
        if (opt.isEmpty()) {
            return;
        }
        AccessoriesCapability cap = opt.get();

        // Prefer getAllEquipped if available — covers normal + cosmetic depending on API
        List<SlotEntryReference> equipped = cap.getAllEquipped();
        for (SlotEntryReference entry : equipped) {
            ItemStack stack = entry.stack();
            if (stack.isEmpty()) {
                continue;
            }
            String slotName = entry.reference().slotName();
            int index = entry.reference().slot();
            CarrySlotDataComponent.tag(stack, CarrySlotDataComponent.System.ACCESSORIES, slotName, index, false);
        }

        // Also walk containers for cosmetic slots and anything getAllEquipped missed
        Map<String, AccessoriesContainer> containers = cap.getContainers();
        for (Map.Entry<String, AccessoriesContainer> e : containers.entrySet()) {
            String slotName = e.getKey();
            AccessoriesContainer container = e.getValue();

            ExpandedSimpleContainer accessories = container.getAccessories();
            for (int i = 0; i < accessories.getContainerSize(); i++) {
                ItemStack stack = accessories.getItem(i);
                if (!stack.isEmpty() && CarrySlotDataComponent.get(stack) == null) {
                    CarrySlotDataComponent.tag(stack, CarrySlotDataComponent.System.ACCESSORIES, slotName, i, false);
                }
            }

            ExpandedSimpleContainer cosmetics = container.getCosmeticAccessories();
            for (int i = 0; i < cosmetics.getContainerSize(); i++) {
                ItemStack stack = cosmetics.getItem(i);
                if (!stack.isEmpty()) {
                    CarrySlotDataComponent.tag(stack, CarrySlotDataComponent.System.ACCESSORIES, slotName, i, true);
                }
            }
        }
    }

    public static boolean tryRestore(ItemStack stack, Player player) {
        CarrySlotDataComponent.CarrySlotData data = CarrySlotDataComponent.get(stack);
        if (data == null || data.system() != CarrySlotDataComponent.System.ACCESSORIES || !data.wasEquipped()) {
            return false;
        }
        if (CarryUtil.shouldSkipRestore(stack)) {
            return false;
        }

        Optional<AccessoriesCapability> opt = AccessoriesCapability.getOptionally(player);
        if (opt.isEmpty()) {
            return false;
        }

        AccessoriesCapability cap = opt.get();
        Map<String, AccessoriesContainer> containers = cap.getContainers();
        AccessoriesContainer container = containers.get(data.slotType());
        if (container != null && tryPlaceInContainer(container, stack, data.slotIndex(), data.cosmetic())) {
            container.markChanged();
            return true;
        }

        // Fallback: first empty compatible slot in any container
        for (AccessoriesContainer c : containers.values()) {
            ExpandedSimpleContainer inv = data.cosmetic() ? c.getCosmeticAccessories() : c.getAccessories();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).isEmpty()) {
                    // Prefer original slot type; only use other containers as last resort for same name already tried
                    if (c == container || container == null) {
                        inv.setItem(i, CarryUtil.cleanCopy(stack));
                        c.markChanged();
                        return true;
                    }
                }
            }
        }

        // Last resort: Accessories auto-equip
        ItemStack clean = CarryUtil.cleanCopy(stack);
        var result = cap.attemptToEquipAccessory(clean, false);
        return result != null;
    }

    private static boolean tryPlaceInContainer(AccessoriesContainer container, ItemStack stack, int index, boolean cosmetic) {
        ExpandedSimpleContainer inv = cosmetic ? container.getCosmeticAccessories() : container.getAccessories();
        if (index < 0 || index >= inv.getContainerSize()) {
            return false;
        }
        if (!inv.getItem(index).isEmpty()) {
            return false;
        }
        inv.setItem(index, CarryUtil.cleanCopy(stack));
        return true;
    }

    @SafeVarargs
    public static void restoreFromLists(Player player, NonNullList<ItemStack>... inventories) {
        for (NonNullList<ItemStack> inv : inventories) {
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.get(i);
                if (stack.isEmpty()) {
                    continue;
                }
                CarrySlotDataComponent.CarrySlotData data = CarrySlotDataComponent.get(stack);
                if (data == null || data.system() != CarrySlotDataComponent.System.ACCESSORIES) {
                    continue;
                }
                if (tryRestore(stack, player)) {
                    inv.set(i, ItemStack.EMPTY);
                }
            }
        }
    }
}
