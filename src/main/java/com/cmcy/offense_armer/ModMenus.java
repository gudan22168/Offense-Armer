package com.cmcy.offense_armer;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Offense_armer.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<NitrogenGeneratorMenu>> NITROGEN_GENERATOR =
            MENUS.register("nitrogen_generator", () -> IMenuTypeExtension.create(NitrogenGeneratorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<GeothermalPlantMenu>> GEOTHERMAL_PLANT =
            MENUS.register("geothermal_plant", () -> IMenuTypeExtension.create(GeothermalPlantMenu::new));
}
