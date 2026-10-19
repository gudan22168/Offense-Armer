package com.cmcy.offense_armer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 制氮机方块实体：电量 / 压缩空气 / 氮气（默认满），分子筛槽，站在附近自动回氮。
 */
public class NitrogenGeneratorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_ENERGY = 10000;
    public static final int MAX_AIR = 10000;
    public static final int MAX_NITROGEN = 10000;
    /** 每轮产氮消耗的能量。 */
    public static final int ENERGY_PER_CYCLE = 10;

    private int energy = MAX_ENERGY;
    private int air = MAX_AIR;
    private int nitrogen = 0;

    private final SimpleContainer inventory = new SimpleContainer(1);

    /** 能量输入能力（发电机 / 无线供电 / 其他模组电缆都可通过它充电）。 */
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int accepted = Math.min(MAX_ENERGY - energy, Math.max(0, toReceive));
            if (accepted > 0 && !simulate) {
                energy += accepted;
                setChanged();
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
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
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy;
                case 1 -> air;
                case 2 -> nitrogen;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energy = value;
                case 1 -> air = value;
                case 2 -> nitrogen = value;
                default -> {
                }
            }
        }
    };

    public NitrogenGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.NITROGEN_GENERATOR_BE.get(), pos, state);
    }

    public int getEnergy() {
        return energy;
    }

    public int getAir() {
        return air;
    }

    public int getNitrogen() {
        return nitrogen;
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.offense_armer.nitrogen_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new NitrogenGeneratorMenu(containerId, playerInventory, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NitrogenGeneratorBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }
        if (level.getGameTime() % 10 != 0) {
            return;
        }
        // 生产：有分子筛 + 氮气未满 + 电够，才产氮、扣耐久
        ItemStack sieve = be.inventory.getItem(0);
        if (sieve.getItem() instanceof CarbonSieveItem
                && sieve.getDamageValue() < sieve.getMaxDamage()
                && be.nitrogen < MAX_NITROGEN
                && be.energy >= ENERGY_PER_CYCLE) {
            be.energy -= ENERGY_PER_CYCLE;
            be.nitrogen = Math.min(MAX_NITROGEN, be.nitrogen + 50);
            sieve.setDamageValue(sieve.getDamageValue() + 1);
            if (sieve.getDamageValue() >= sieve.getMaxDamage()) {
                be.inventory.setItem(0, ItemStack.EMPTY);
            }
            be.setChanged();
        }
        // 站在机器附近（右侧一格 / 前方）自动回氮
        AABB area = new AABB(pos).inflate(2.0);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, area);
        for (ServerPlayer player : players) {
            NitrogenAbility ability = player.getData(ModAttachments.NITROGEN_ABILITY.get());
            float missing = ability.getMaxNitrogen() - ability.getNitrogen();
            float add = Math.min(5.0f, missing);
            if (add > 0 && be.nitrogen >= (int) (add * 20)) {
                ability.addNitrogen(add);
                be.nitrogen -= (int) (add * 20);
                be.setChanged();
                ModNetwork.syncToClient(player, ability);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("Energy", energy);
        tag.putInt("Air", air);
        tag.putInt("Nitrogen", nitrogen);
        ContainerHelper.saveAllItems(tag, inventory.getItems(), provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Energy")) {
            energy = tag.getInt("Energy");
        }
        if (tag.contains("Air")) {
            air = tag.getInt("Air");
        }
        if (tag.contains("Nitrogen")) {
            nitrogen = tag.getInt("Nitrogen");
        }
        ContainerHelper.loadAllItems(tag, inventory.getItems(), provider);
    }
}
