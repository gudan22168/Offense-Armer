package com.cmcy.offense_armer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 制氮机：2 格宽横向方块（左半/右半）。右半是主方块，承载方块实体。
 */
public class NitrogenGeneratorBlock extends Block implements EntityBlock {
    public enum Part implements StringRepresentable {
        LEFT("left"), RIGHT("right");
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
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public NitrogenGeneratorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, Part.LEFT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getHorizontalDirection().getOpposite();
        BlockPos otherPos = ctx.getClickedPos().relative(facing.getClockWise());
        if (!ctx.getLevel().getBlockState(otherPos).canBeReplaced(ctx)) {
            return null; // 右半放不下，取消整台机器的放置
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(PART, Part.LEFT);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(FACING);
        BlockPos otherPos = pos.relative(facing.getClockWise());
        if (level.getBlockState(otherPos).isAir()) {
            level.setBlock(otherPos, state.setValue(PART, Part.RIGHT), 3);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            Part part = state.getValue(PART);
            Direction facing = state.getValue(FACING);
            BlockPos otherPos = part == Part.LEFT
                    ? pos.relative(facing.getClockWise())
                    : pos.relative(facing.getCounterClockWise());
            BlockState other = level.getBlockState(otherPos);
            if (other.is(this) && other.getValue(PART) != part) {
                level.removeBlock(otherPos, false);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.RIGHT ? new NitrogenGeneratorBlockEntity(pos, state) : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockPos bePos = state.getValue(PART) == Part.RIGHT
                    ? pos
                    : pos.relative(state.getValue(FACING).getClockWise());
            if (level.getBlockEntity(bePos) instanceof NitrogenGeneratorBlockEntity generator) {
                player.openMenu(generator);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlocks.NITROGEN_GENERATOR_BE.get()
                ? (lvl, pos, st, be) -> NitrogenGeneratorBlockEntity.tick(lvl, pos, st, (NitrogenGeneratorBlockEntity) be)
                : null;
    }
}
