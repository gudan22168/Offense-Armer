package com.cmcy.offense_armer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * 地热电站管理界面：显示储能、连接数（详细连接列表通过网络包同步）。
 */
public class GeothermalPlantMenu extends AbstractContainerMenu {
    private final BlockPos plantPos;
    private final ContainerData data;

    // 客户端
    public GeothermalPlantMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos(), new SimpleContainerData(2));
    }

    // 服务端
    public GeothermalPlantMenu(int containerId, Inventory playerInventory, GeothermalPlantBlockEntity be) {
        this(containerId, playerInventory, be.getBlockPos(), be.getData());
    }

    private GeothermalPlantMenu(int containerId, Inventory playerInventory, BlockPos pos, ContainerData data) {
        super(ModMenus.GEOTHERMAL_PLANT.get(), containerId);
        this.plantPos = pos;
        this.data = data;
        this.addDataSlots(data);
    }

    public BlockPos getPlantPos() {
        return plantPos;
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getLinkCount() {
        return data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(plantPos) instanceof GeothermalPlantBlockEntity
                && player.distanceToSqr(plantPos.getX() + 0.5, plantPos.getY() + 0.5, plantPos.getZ() + 0.5) < 64.0;
    }
}
