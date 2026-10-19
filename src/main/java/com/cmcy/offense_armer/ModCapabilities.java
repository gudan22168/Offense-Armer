package com.cmcy.offense_armer;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * 方块能力注册：给制氮机加能量输入能力（发电机/无线供电/其他模组电缆可充电）。
 */
@EventBusSubscriber(modid = Offense_armer.MODID)
public class ModCapabilities {
    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlocks.NITROGEN_GENERATOR_BE.get(),
                (be, side) -> be.getEnergyStorage()
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlocks.GEOTHERMAL_PLANT_BE.get(),
                (be, side) -> be.getEnergyStorage()
        );
    }
}
