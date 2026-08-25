package net.njw.compassbar;

import com.mojang.logging.LogUtils;
import net.njw.compassbar.network.NetworkHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CompassBar.MODID)
public class CompassBar {

    public static final String MODID = "njw_compass_bar";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CompassBar(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::registerPayloads);
    }
}