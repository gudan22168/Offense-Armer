package com.cmcy.offense_armer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * 能量能力查找工具。
 * 有些模组（如 Mekanism 的机器）只在配置过的方向暴露 IEnergyStorage，
 * 所以无方向查找失败时再逐个方向尝试。
 */
public final class EnergyHelper {
    private EnergyHelper() {
    }

    /** 找一个能接收能量的能力，找不到返回 null。 */
    @Nullable
    public static IEnergyStorage findReceiver(Level level, BlockPos pos) {
        IEnergyStorage s = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        if (s != null && s.canReceive()) {
            return s;
        }
        for (Direction d : Direction.values()) {
            s = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, d);
            if (s != null && s.canReceive()) {
                return s;
            }
        }
        return null;
    }

    /** 找任意能量能力（不管能否接收），用于读取电量显示。 */
    @Nullable
    public static IEnergyStorage findAny(Level level, BlockPos pos) {
        IEnergyStorage s = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        if (s != null) {
            return s;
        }
        for (Direction d : Direction.values()) {
            s = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, d);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    /**
     * 把能量推给目标：先试无方向，再逐个方向试，累加实际接收量。
     * 不依赖 canReceive()，因为部分模组（Mekanism 等）的实现与面配置不完全一致。
     *
     * @return 实际被接收的能量
     */
    public static int push(Level level, BlockPos pos, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int accepted = 0;
        IEnergyStorage s = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        if (s != null) {
            accepted += s.receiveEnergy(amount, false);
        }
        if (accepted < amount) {
            for (Direction d : Direction.values()) {
                s = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, d);
                if (s != null) {
                    accepted += s.receiveEnergy(amount - accepted, false);
                    if (accepted >= amount) {
                        break;
                    }
                }
            }
        }
        return Math.min(accepted, amount);
    }
}
