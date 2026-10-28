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
}
