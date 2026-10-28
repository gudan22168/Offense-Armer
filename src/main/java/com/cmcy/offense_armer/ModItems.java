package com.cmcy.offense_armer;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Offense_armer.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Offense_armer.MODID);

    public static final DeferredItem<Item> LIQUID_NITROGEN_BOTTLE = ITEMS.register("liquid_nitrogen_bottle",
            () -> new LiquidNitrogenBottle(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> ABILITY_POTION_1 = ITEMS.register("ability_potion_1",
            () -> new AbilityPotionItem(new Item.Properties().stacksTo(1), 1));
    public static final DeferredItem<Item> ABILITY_POTION_2 = ITEMS.register("ability_potion_2",
            () -> new AbilityPotionItem(new Item.Properties().stacksTo(1), 2));
    public static final DeferredItem<Item> ABILITY_POTION_3 = ITEMS.register("ability_potion_3",
            () -> new AbilityPotionItem(new Item.Properties().stacksTo(1), 3));
    public static final DeferredItem<Item> ABILITY_POTION_4 = ITEMS.register("ability_potion_4",
            () -> new AbilityPotionItem(new Item.Properties().stacksTo(1), 4));
    public static final DeferredItem<Item> ABILITY_POTION_5 = ITEMS.register("ability_potion_5",
            () -> new AbilityPotionItem(new Item.Properties().stacksTo(1), 5));

    public static final DeferredItem<Item> CARBON_SIEVE = ITEMS.register("carbon_sieve",
            () -> new CarbonSieveItem(new Item.Properties().stacksTo(1).durability(512)));

    public static final DeferredItem<BlockItem> NITROGEN_GENERATOR = ITEMS.register("nitrogen_generator",
            () -> new BlockItem(ModBlocks.NITROGEN_GENERATOR.get(), new Item.Properties()));

    public static final DeferredItem<Item> HEAT_ROD = ITEMS.register("heat_rod",
            () -> new HeatRodItem(new Item.Properties().stacksTo(1).durability(2031)));

    public static final DeferredItem<BlockItem> GEOTHERMAL_PLANT = ITEMS.register("geothermal_plant",
            () -> new BlockItem(ModBlocks.GEOTHERMAL_PLANT.get(), new Item.Properties()));

    public static final DeferredItem<Item> POWER_LINKER = ITEMS.register("power_linker",
            () -> new PowerLinkerItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register(
            "offense_armer",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.offense_armer"))
                    .icon(() -> new ItemStack(LIQUID_NITROGEN_BOTTLE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(LIQUID_NITROGEN_BOTTLE.get());
                        output.accept(ABILITY_POTION_1.get());
                        output.accept(ABILITY_POTION_2.get());
                        output.accept(ABILITY_POTION_3.get());
                        output.accept(ABILITY_POTION_4.get());
                        output.accept(ABILITY_POTION_5.get());
                        output.accept(NITROGEN_GENERATOR.get());
                        output.accept(CARBON_SIEVE.get());
                        output.accept(GEOTHERMAL_PLANT.get());
                        output.accept(HEAT_ROD.get());
                        output.accept(POWER_LINKER.get());
                    })
                    .build());
}
