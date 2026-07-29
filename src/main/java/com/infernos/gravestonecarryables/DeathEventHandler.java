package com.infernos.gravestonecarryables;

import com.infernos.gravestonecarryables.handler.CarrySystems;
import com.leclowndu93150.baguettelib.event.entity.death.PlayerDeathEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Before death inventories are collected into the grave, tag every equipped carryable
 * so recovery can put it back in the same slot. Item components (backpack loot, enchants,
 * modules, custom data) stay on the stack.
 */
@EventBusSubscriber(modid = Main.MODID)
public final class DeathEventHandler {

    private DeathEventHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeathPre(PlayerDeathEvent.Pre event) {
        Player player = event.getPlayer();
        CarrySystems.tagAll(player);

        if (Config.RESTORE_VANILLA_SLOTS.get()) {
            tagVanilla(player);
        }
    }

    private static void tagVanilla(Player player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.items.size(); i++) {
            ItemStack stack = inv.items.get(i);
            if (!stack.isEmpty()) {
                CarrySlotDataComponent.tag(stack, CarrySlotDataComponent.System.VANILLA, "main", i, false);
            }
        }
        for (int i = 0; i < inv.armor.size(); i++) {
            ItemStack stack = inv.armor.get(i);
            if (!stack.isEmpty()) {
                CarrySlotDataComponent.tag(stack, CarrySlotDataComponent.System.VANILLA, "armor", i, false);
            }
        }
        for (int i = 0; i < inv.offhand.size(); i++) {
            ItemStack stack = inv.offhand.get(i);
            if (!stack.isEmpty()) {
                CarrySlotDataComponent.tag(stack, CarrySlotDataComponent.System.VANILLA, "offhand", i, false);
            }
        }
    }
}
