package com.cmcy.offense_armer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 地热实验电站方块实体：插入送热棒后，向下抽取熔岩转为电能，无线推送给附近机器。
 */
public class GeothermalPlantBlockEntity extends BlockEntity {
    public static final int MAX_ENERGY = 100_000;

    private int energy = 0;
    private final SimpleContainer inventory = new SimpleContainer(1);

    /** 能量输出能力（无线推送和目标机器都通过它取电）。 */
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = Math.min(energy, Math.max(0, maxExtract));
            if (extracted > 0 && !simulate) {
                energy -= extracted;
                setChanged();
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX_ENERGY;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public GeothermalPlantBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GEOTHERMAL_PLANT_BE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeothermalPlantBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }
        boolean hasRod = !be.inventory.getItem(0).isEmpty();
        // 与方块状态同步（棒坏掉/插入），四格一起切换
        if (be.getBlockState().getValue(GeothermalPlantBlock.HAS_ROD) != hasRod) {
            GeothermalPlantBlock.setRodState(level, pos, hasRod);
        }
        if (hasRod && level.getGameTime() % 20 == 0) {
            be.scanAndExtract(level, pos);
        }
        if (be.energy > 0) {
            int pushed = WirelessEnergy.push(level, pos, Config.geoWirelessRadius,
                    Math.min(be.energy, Config.geoPushPerTick), be);
            if (pushed > 0) {
                be.energy -= pushed;
                be.setChanged();
            }
        }
    }

    /** 随机采样若干列，每列从机器下方一直找到基岩层，挖掉第一块熔岩。 */
    private void scanAndExtract(Level level, BlockPos pos) {
        int r = Config.geoRadius;
        int target = Config.geoLavaPerScan;
        int mined = 0;
        int attempts = 0;
        while (mined < target && attempts < 160) {
            attempts++;
            int dx = level.random.nextInt(r * 2 + 1) - r;
            int dz = level.random.nextInt(r * 2 + 1) - r;
            if (dx * dx + dz * dz > r * r) {
                continue;
            }
            BlockPos column = pos.offset(dx, 0, dz);
            for (int y = pos.getY() - 1; y >= level.getMinBuildHeight(); y--) {
                BlockPos p = new BlockPos(column.getX(), y, column.getZ());
                if (level.getBlockState(p).is(Blocks.LAVA)) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    this.energy = Math.min(MAX_ENERGY, this.energy + Config.geoEnergyPerLava);
                    mined++;
                    // 送热棒损耗
                    ItemStack rod = this.inventory.getItem(0);
                    rod.setDamageValue(rod.getDamageValue() + 1);
                    if (rod.getDamageValue() >= rod.getMaxDamage()) {
                        this.inventory.setItem(0, ItemStack.EMPTY);
                    }
                    this.setChanged();
                    break;
                }
            }
        }
    }

    public boolean tryInsertRod(ItemStack stack) {
        if (!this.inventory.getItem(0).isEmpty() || !(stack.getItem() instanceof HeatRodItem)) {
            return false;
        }
        this.inventory.setItem(0, stack.copyWithCount(1));
        stack.shrink(1);
        this.setChanged();
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("Energy", energy);
        ContainerHelper.saveAllItems(tag, inventory.getItems(), provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Energy")) {
            energy = tag.getInt("Energy");
        }
        ContainerHelper.loadAllItems(tag, inventory.getItems(), provider);
    }
}
