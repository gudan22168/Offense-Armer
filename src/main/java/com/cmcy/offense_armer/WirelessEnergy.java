package com.cmcy.offense_armer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 无线能量传输（不做电缆）：把能量推送给半径内所有可接收能量的方块。
 * 发电机会在自己的 tick 里调用 {@link #push}。
 */
public final class WirelessEnergy {
    private WirelessEnergy() {
    }

    /**
     * 从 origin 向半径 radius 内的 IEnergyStorage 方块推送能量。
     *
     * @param self 发射方自身（跳过自己），可为 null
     * @return 实际送出的能量
     */
    public static int push(Level level, BlockPos origin, int radius, int amount, BlockEntity self) {
        if (amount <= 0 || level.isClientSide()) {
            return 0;
        }
        int remaining = amount;
        BlockPos min = origin.offset(-radius, -radius, -radius);
        BlockPos max = origin.offset(radius, radius, radius);
        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            if (remaining <= 0) {
                break;
            }
            if (self != null && p.equals(self.getBlockPos())) {
                continue;
            }
            IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, p, null);
            if (target == null || !target.canReceive()) {
                continue;
            }
            int accepted = target.receiveEnergy(remaining, false);
            if (accepted > 0) {
                remaining -= accepted;
            }
        }
        return amount - remaining;
    }
}
