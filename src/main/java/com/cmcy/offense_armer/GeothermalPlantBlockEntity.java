package com.cmcy.offense_armer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * 地热实验电站方块实体：插入送热棒后向下抽取熔岩发电，按连接列表定向供电。
 */
public class GeothermalPlantBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_ENERGY = 100_000;

    private int energy = 0;
    private int lavaMined = 0;
    private int lastScanFound = 0;
    private final SimpleContainer inventory = new SimpleContainer(1);
    /** 已连接的用电设备坐标。 */
    private final List<BlockPos> links = new ArrayList<>();

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

    private final ContainerData data = new ContainerData() {
        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy;
                case 1 -> links.size();
                case 2 -> lavaMined;
                case 3 -> {
                    ItemStack rod = inventory.getItem(0);
                    yield rod.isEmpty() ? -1 : rod.getMaxDamage() - rod.getDamageValue();
                }
                case 4 -> lastScanFound;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }
    };

    public GeothermalPlantBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GEOTHERMAL_PLANT_BE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.offense_armer.geothermal_plant");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            ModNetwork.syncPlantLinks(sp, this);
        }
        return new GeothermalPlantMenu(containerId, playerInventory, this);
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    public List<BlockPos> getLinks() {
        return links;
    }

    public boolean addLink(BlockPos pos) {
        if (links.contains(pos)) {
            return false;
        }
        links.add(pos.immutable());
        setChanged();
        return true;
    }

    public boolean removeLink(BlockPos pos) {
        boolean removed = links.remove(pos);
        if (removed) {
            setChanged();
        }
        return removed;
    }

    public void clearLinks() {
        if (!links.isEmpty()) {
            links.clear();
            setChanged();
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeothermalPlantBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }
        boolean hasRod = !be.inventory.getItem(0).isEmpty();
        if (be.getBlockState().getValue(GeothermalPlantBlock.HAS_ROD) != hasRod) {
            GeothermalPlantBlock.setRodState(level, pos, hasRod);
        }
        if (hasRod && level.getGameTime() % 20 == 0) {
            be.scanAndExtract(level, pos);
        }
        // 按连接列表定向供电
        if (be.energy > 0 && !be.links.isEmpty()) {
            int perLink = Math.max(1, Config.geoPushPerTick / be.links.size());
            boolean dirty = false;
            for (BlockPos target : be.links) {
                if (be.energy <= 0) {
                    break;
                }
                IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, target, null);
                if (storage == null || !storage.canReceive()) {
                    continue;
                }
                int accepted = storage.receiveEnergy(Math.min(be.energy, perLink), false);
                if (accepted > 0) {
                    be.energy -= accepted;
                    dirty = true;
                }
            }
            if (dirty) {
                be.setChanged();
            }
        }
    }

    /** 随机采样若干列，每列从机器下方一直找到基岩层，挖掉第一块熔岩。 */
    private void scanAndExtract(Level level, BlockPos pos) {
        // 储能已满：停止抽取，避免白白消耗送热棒
        if (this.energy >= MAX_ENERGY) {
            this.lastScanFound = -1;
            return;
        }
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
                    this.lavaMined++;
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
        this.lastScanFound = mined;
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
        tag.putInt("LavaMined", lavaMined);
        tag.putLongArray("Links", links.stream().mapToLong(BlockPos::asLong).toArray());
        ContainerHelper.saveAllItems(tag, inventory.getItems(), provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Energy")) {
            energy = tag.getInt("Energy");
        }
        lavaMined = tag.getInt("LavaMined");
        links.clear();
        for (long l : tag.getLongArray("Links")) {
            links.add(BlockPos.of(l));
        }
        ContainerHelper.loadAllItems(tag, inventory.getItems(), provider);
    }
}
