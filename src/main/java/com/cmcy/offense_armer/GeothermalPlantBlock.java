package com.cmcy.offense_armer;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 地热实验电站：2×2 多方块。主方块（NW）承载方块实体，插入送热棒后抽熔岩发电。
 */
public class GeothermalPlantBlock extends Block implements EntityBlock {
    public enum Part implements StringRepresentable {
        NW("nw"), NE("ne"), SW("sw"), SE("se");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final BooleanProperty HAS_ROD = BooleanProperty.create("has_rod");

    public GeothermalPlantBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PART, Part.NW).setValue(HAS_ROD, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, HAS_ROD);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        // 2×2 四格是否都能放
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                if (!level.getBlockState(pos.offset(dx, 0, dz)).canBeReplaced(ctx)) {
                    return null;
                }
            }
        }
        // 单区块限制：2×2 覆盖的 4 个区块里不能已有电站
        Player player = ctx.getPlayer();
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                if (chunkHasPlant(level, pos.offset(dx, 0, dz))) {
                    if (player != null) {
                        player.displayClientMessage(Component.translatable("message.offense_armer.geo_occupied"), true);
                    }
                    return null;
                }
            }
        }
        return this.defaultBlockState().setValue(PART, Part.NW).setValue(HAS_ROD, false);
    }

    private static boolean chunkHasPlant(Level level, BlockPos pos) {
        var chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be instanceof GeothermalPlantBlockEntity) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.east(), state.setValue(PART, Part.NE), 3);
        level.setBlock(pos.south(), state.setValue(PART, Part.SW), 3);
        level.setBlock(pos.south().east(), state.setValue(PART, Part.SE), 3);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos master = getMasterPos(pos, state);
            if (level.getBlockEntity(master) instanceof GeothermalPlantBlockEntity be) {
                Containers.dropContents(level, master, be.getInventory());
            }
            // 移除其余 3 格（被挖这格交给原版掉落）
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    BlockPos p = master.offset(dx, 0, dz);
                    if (p.equals(pos)) {
                        continue;
                    }
                    if (level.getBlockState(p).is(this)) {
                        level.removeBlock(p, false);
                    }
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof HeatRodItem) {
            BlockPos master = getMasterPos(pos, state);
            if (level.getBlockEntity(master) instanceof GeothermalPlantBlockEntity be && be.tryInsertRod(stack)) {
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (stack.getItem() instanceof PowerLinkerItem) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        openPlantMenu(level, pos, state, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        openPlantMenu(level, pos, state, player);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void openPlantMenu(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide()) {
            return;
        }
        BlockPos master = getMasterPos(pos, state);
        if (level.getBlockEntity(master) instanceof GeothermalPlantBlockEntity be) {
            player.openMenu(be);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.NW ? new GeothermalPlantBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof GeothermalPlantBlockEntity plant) {
                GeothermalPlantBlockEntity.tick(lvl, pos, st, plant);
            }
        };
    }

    public static BlockPos getMasterPos(BlockPos pos, BlockState state) {
        return switch (state.getValue(PART)) {
            case NW -> pos;
            case NE -> pos.west();
            case SW -> pos.north();
            case SE -> pos.north().west();
        };
    }

    /** 同步四格的送热棒状态（模型 2×2 拼接，棒跨在中心，四格都要切换）。 */
    public static void setRodState(Level level, BlockPos master, boolean hasRod) {
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                BlockPos p = master.offset(dx, 0, dz);
                BlockState s = level.getBlockState(p);
                if (s.is(ModBlocks.GEOTHERMAL_PLANT.get()) && s.getValue(HAS_ROD) != hasRod) {
                    level.setBlock(p, s.setValue(HAS_ROD, hasRod), 3);
                }
            }
        }
    }
}
