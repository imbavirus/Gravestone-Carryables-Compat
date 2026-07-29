package com.infernos.gravestonecarryables.mixin;

import com.infernos.gravestonecarryables.CarrySlotDataComponent;
import com.infernos.gravestonecarryables.CarryUtil;
import com.infernos.gravestonecarryables.Config;
import com.infernos.gravestonecarryables.handler.CarrySystems;
import de.maxhenkel.gravestone.blocks.GraveStoneBlock;
import de.maxhenkel.gravestone.corelib.death.Death;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(GraveStoneBlock.class)
public abstract class GraveStoneBlockMixin {

    /**
     * @author Infernos / Leclowndu93150
     * @reason Restore Curios, Accessories, and vanilla slots with full stack data (backpacks, enchants, modules).
     */
    @Overwrite(remap = false)
    public NonNullList<ItemStack> fillPlayerInventory(Player player, Death death) {
        NonNullList<ItemStack> overflow = NonNullList.create();

        CarrySystems.restoreEquipmentFromLists(
                player,
                death.getMainInventory(),
                death.getArmorInventory(),
                death.getOffHandInventory(),
                death.getAdditionalItems()
        );

        if (Config.RESTORE_VANILLA_SLOTS.get()) {
            gravestonecarryables$transferVanilla(death.getMainInventory(), player.getInventory().items, overflow, "main");
            gravestonecarryables$transferVanilla(death.getArmorInventory(), player.getInventory().armor, overflow, "armor");
            gravestonecarryables$transferVanilla(death.getOffHandInventory(), player.getInventory().offhand, overflow, "offhand");
        }

        List<NonNullList<ItemStack>> remaining = new ArrayList<>();
        remaining.add(death.getMainInventory());
        remaining.add(death.getArmorInventory());
        remaining.add(death.getOffHandInventory());
        remaining.add(death.getAdditionalItems());

        for (NonNullList<ItemStack> list : remaining) {
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = list.get(i);
                if (stack.isEmpty()) {
                    continue;
                }
                if (CarrySystems.tryRestoreTagged(stack, player)) {
                    list.set(i, ItemStack.EMPTY);
                    continue;
                }
                ItemStack clean = stack.copy();
                CarrySlotDataComponent.clear(clean);
                if (!player.getInventory().add(clean)) {
                    overflow.add(clean);
                }
                list.set(i, ItemStack.EMPTY);
            }
        }

        death.getAdditionalItems().clear();
        return overflow;
    }

    @Unique
    private void gravestonecarryables$transferVanilla(
            NonNullList<ItemStack> source,
            NonNullList<ItemStack> destination,
            NonNullList<ItemStack> overflow,
            String expectedSlotType
    ) {
        for (int i = 0; i < source.size() && i < destination.size(); i++) {
            ItemStack stack = source.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            CarrySlotDataComponent.CarrySlotData data = CarrySlotDataComponent.get(stack);
            if (data != null && data.system() != CarrySlotDataComponent.System.VANILLA && data.wasEquipped()) {
                continue;
            }

            int destIndex = i;
            if (data != null
                    && data.system() == CarrySlotDataComponent.System.VANILLA
                    && expectedSlotType.equals(data.slotType())
                    && data.slotIndex() >= 0
                    && data.slotIndex() < destination.size()) {
                destIndex = data.slotIndex();
            }

            ItemStack current = destination.get(destIndex);
            if (!current.isEmpty()) {
                overflow.add(current);
            }
            ItemStack clean = CarryUtil.cleanCopy(stack);
            destination.set(destIndex, clean);
            source.set(i, ItemStack.EMPTY);
        }
    }
}
