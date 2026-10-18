package com.cmcy.offense_armer;

import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

/**
 * Mekanism 氮气联动：
 * <ul>
 *   <li>反向充能：Lv5 满氮气溢出时，把「氮气」（配置的气体）充入附近 Mekanism 储罐/设备与背包气体容器。</li>
 *   <li>抽取氮气：从附近 Mekanism 储罐/容器抽取气体，补充玩家氮气（兼容模组氮气获取）。</li>
 * </ul>
 * 编译期引用 Mekanism 化学 API，运行时用 {@link #isLoaded()} 判断 Mekanism 是否存在，避免硬依赖。
 * 注意：Mekanism 10.x 已无「氮气」气体，目标气体由配置 mekanismGasId 决定（默认氢气）。
 */
public class MekanismCompat {
    /** 1.0 氮气点数 = 10 mB 气体（100 点满 = 1000 mB = 1 桶）。 */
    public static final float MB_PER_NITROGEN = 10f;

    private static Holder<Chemical> nitrogen;
    private static String resolvedId = "";

    private MekanismCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("mekanism");
    }

    private static Holder<Chemical> getNitrogen() {
        String id = Config.mekanismGasId;
        if (!id.equals(resolvedId)) {
            Chemical chemical = MekanismAPI.CHEMICAL_REGISTRY.getOptional(ResourceLocation.parse(id)).orElse(null);
            nitrogen = chemical == null ? null : MekanismAPI.CHEMICAL_REGISTRY.wrapAsHolder(chemical);
            resolvedId = id;
        }
        return nitrogen;
    }

    /** Lv5 反向充能：把氮气溢出充入附近 Mekanism 储罐/设备与背包气体容器。 */
    public static void reverseCharge(ServerPlayer player) {
        if (!isLoaded()) {
            return;
        }
        Holder<Chemical> gas = getNitrogen();
        if (gas == null) {
            return;
        }
        long amount = Config.mekanismChargeMb;

        BlockCapability<IChemicalHandler, Direction> blockCap = Capabilities.CHEMICAL.block();
        int r = Config.cryoRadius;
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            IChemicalHandler handler = player.level().getCapability(blockCap, pos, null);
            if (handler != null) {
                handler.insertChemical(new ChemicalStack(gas, amount), Action.EXECUTE);
            }
        }

        ItemCapability<IChemicalHandler, Void> itemCap = Capabilities.CHEMICAL.item();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) {
                continue;
            }
            IChemicalHandler handler = stack.getCapability(itemCap);
            if (handler != null) {
                handler.insertChemical(new ChemicalStack(gas, amount), Action.EXECUTE);
            }
        }
    }

    /** 从附近 Mekanism 储罐/容器抽取气体，返回抽到的 mB 数（用于兼容模组氮气获取）。 */
    public static long drainNitrogen(ServerPlayer player) {
        if (!isLoaded()) {
            return 0;
        }
        Holder<Chemical> gas = getNitrogen();
        if (gas == null) {
            return 0;
        }
        long amount = Config.mekanismDrainMb;
        long drained = 0;

        BlockCapability<IChemicalHandler, Direction> blockCap = Capabilities.CHEMICAL.block();
        int r = Config.cryoRadius;
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            IChemicalHandler handler = player.level().getCapability(blockCap, pos, null);
            if (handler == null) {
                continue;
            }
            ChemicalStack extracted = handler.extractChemical(new ChemicalStack(gas, amount), Action.EXECUTE);
            if (extracted != null && !extracted.isEmpty()) {
                drained += extracted.getAmount();
            }
            if (drained >= amount) {
                return drained;
            }
        }

        ItemCapability<IChemicalHandler, Void> itemCap = Capabilities.CHEMICAL.item();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) {
                continue;
            }
            IChemicalHandler handler = stack.getCapability(itemCap);
            if (handler == null) {
                continue;
            }
            ChemicalStack extracted = handler.extractChemical(new ChemicalStack(gas, amount), Action.EXECUTE);
            if (extracted != null && !extracted.isEmpty()) {
                drained += extracted.getAmount();
            }
            if (drained >= amount) {
                return drained;
            }
        }
        return drained;
    }
}
