package com.cmcy.offense_armer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 制氮机容器菜单：1 个碳分子筛槽 + 3 个数据（电量/空气/氮气）+ 玩家背包。
 */
public class NitrogenGeneratorMenu extends AbstractContainerMenu {
    public static final int SLOT_SIEVE = 0;
    public static final int DATA_ENERGY = 0;
    public static final int DATA_AIR = 1;
    public static final int DATA_NITROGEN = 2;

    private final Container container;
    private final ContainerData data;

    // 客户端从网络创建
    public NitrogenGeneratorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory);
    }

    // 客户端本地创建
    public NitrogenGeneratorMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(1), new SimpleContainerData(3));
    }

    // 服务端创建
    public NitrogenGeneratorMenu(int containerId, Inventory playerInventory, NitrogenGeneratorBlockEntity be) {
        this(containerId, playerInventory, be.getInventory(), be.getData());
    }

    private NitrogenGeneratorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenus.NITROGEN_GENERATOR.get(), containerId);
        this.container = container;
        this.data = data;

        // 分子筛槽（GUI 背景在 89,33，物品在 90,34）
        this.addSlot(new Slot(container, 0, 90, 34) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof CarbonSieveItem;
            }
        });

        // 玩家背包 3x9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 9 + col * 18, 85 + row * 18));
            }
        }
        // 快捷栏
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 9 + col * 18, 143));
        }

        this.addDataSlots(data);
    }

    public int getEnergy() {
        return data.get(DATA_ENERGY);
    }

    public int getAir() {
        return data.get(DATA_AIR);
    }

    public int getNitrogen() {
        return data.get(DATA_NITROGEN);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return result;
        }
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index == SLOT_SIEVE) {
            if (!this.moveItemStackTo(stack, 1, 37, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof CarbonSieveItem) {
            if (!this.moveItemStackTo(stack, SLOT_SIEVE, SLOT_SIEVE + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 28) {
            if (!this.moveItemStackTo(stack, 28, 37, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 1, 28, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return result;
    }
}
