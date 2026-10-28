package com.cmcy.offense_armer;

import java.util.List;

import net.minecraft.core.BlockPos;

/** 客户端保存的电站连接数据（只被客户端界面读取）。 */
public final class ClientPlantLinks {
    public static BlockPos plant = null;
    public static List<ModNetwork.PlantLink> links = List.of();

    private ClientPlantLinks() {
    }

    public static void set(BlockPos pos, List<ModNetwork.PlantLink> list) {
        plant = pos;
        links = list;
    }
}
