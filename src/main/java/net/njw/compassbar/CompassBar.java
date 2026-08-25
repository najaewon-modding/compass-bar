package net.njw.compassbar;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(CompassBar.MODID)
public class CompassBar {
    public static final String MODID = "njw_compass_bar";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CompassBar(IEventBus modEventBus) {
    }
}