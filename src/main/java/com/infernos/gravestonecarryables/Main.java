package com.infernos.gravestonecarryables;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "gravestonecarryables";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Main(IEventBus modEventBus, ModContainer modContainer) {
        Config.register(modContainer);
        CarrySlotDataComponent.DATA_COMPONENTS.register(modEventBus);

        LOGGER.info("Gravestone Carryables Compat loaded (curios={}, accessories={})",
                ModList.get().isLoaded("curios"),
                ModList.get().isLoaded("accessories"));
    }
}
