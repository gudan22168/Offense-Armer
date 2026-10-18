package com.cmcy.offense_armer;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Offense_armer.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Offense_armer.MODID);

    public static final DeferredBlock<NitrogenGeneratorBlock> NITROGEN_GENERATOR = BLOCKS.register("nitrogen_generator",
            () -> new NitrogenGeneratorBlock(BlockBehaviour.Properties.of().strength(3.0f).noOcclusion()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NitrogenGeneratorBlockEntity>> NITROGEN_GENERATOR_BE =
            BLOCK_ENTITIES.register("nitrogen_generator",
                    () -> BlockEntityType.Builder.of(NitrogenGeneratorBlockEntity::new, NITROGEN_GENERATOR.get()).build(null));
}
