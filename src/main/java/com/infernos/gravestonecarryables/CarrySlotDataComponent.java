package com.infernos.gravestonecarryables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Marks an item stack with where it was equipped at death so grave recovery can put it back.
 * Full ItemStack (components / backpack contents / enchants / modules) is preserved — we only
 * add and later remove this component around transfer.
 */
public final class CarrySlotDataComponent {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Main.MODID);

    public enum System {
        CURIOS,
        ACCESSORIES,
        VANILLA;

        public static final Codec<System> CODEC = Codec.STRING.xmap(
                s -> System.valueOf(s.toUpperCase()),
                Enum::name
        );
    }

    /**
     * @param system       equipment system
     * @param slotType     curios/accessories slot id, or vanilla: main/armor/offhand
     * @param slotIndex    index within that inventory
     * @param wasEquipped  true if actively equipped at death
     * @param cosmetic     cosmetic variant of curios/accessories slot when applicable
     */
    public record CarrySlotData(
            System system,
            String slotType,
            int slotIndex,
            boolean wasEquipped,
            boolean cosmetic
    ) {}

    public static final Codec<CarrySlotData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    System.CODEC.fieldOf("system").forGetter(CarrySlotData::system),
                    Codec.STRING.fieldOf("slotType").forGetter(CarrySlotData::slotType),
                    Codec.INT.fieldOf("slotIndex").forGetter(CarrySlotData::slotIndex),
                    Codec.BOOL.fieldOf("wasEquipped").forGetter(CarrySlotData::wasEquipped),
                    Codec.BOOL.optionalFieldOf("cosmetic", false).forGetter(CarrySlotData::cosmetic)
            ).apply(instance, CarrySlotData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CarrySlotData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, d -> d.system().name(),
            ByteBufCodecs.STRING_UTF8, CarrySlotData::slotType,
            ByteBufCodecs.VAR_INT, CarrySlotData::slotIndex,
            ByteBufCodecs.BOOL, CarrySlotData::wasEquipped,
            ByteBufCodecs.BOOL, CarrySlotData::cosmetic,
            (system, slotType, slotIndex, wasEquipped, cosmetic) ->
                    new CarrySlotData(System.valueOf(system), slotType, slotIndex, wasEquipped, cosmetic)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CarrySlotData>> CARRY_SLOT_DATA =
            DATA_COMPONENTS.register("carry_slot_data", () ->
                    DataComponentType.<CarrySlotData>builder()
                            .persistent(CODEC)
                            .networkSynchronized(STREAM_CODEC)
                            .build());

    private CarrySlotDataComponent() {}

    public static void tag(net.minecraft.world.item.ItemStack stack, System system, String slotType, int slotIndex, boolean cosmetic) {
        stack.set(CARRY_SLOT_DATA.get(), new CarrySlotData(system, slotType, slotIndex, true, cosmetic));
    }

    public static CarrySlotData get(net.minecraft.world.item.ItemStack stack) {
        return stack.get(CARRY_SLOT_DATA.get());
    }

    public static void clear(net.minecraft.world.item.ItemStack stack) {
        stack.remove(CARRY_SLOT_DATA.get());
    }
}
