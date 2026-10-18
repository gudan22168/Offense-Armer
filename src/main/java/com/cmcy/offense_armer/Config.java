package com.cmcy.offense_armer;

import java.util.List;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 氮气装甲的可配置数值，全部集中在 COMMON 配置里，方便后续调平衡。
 */
@EventBusSubscriber(modid = Offense_armer.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue MAX_NITROGEN = BUILDER
            .comment("氮气上限（最大储量）。")
            .defineInRange("maxNitrogen", 100.0, 1.0, 100000.0);

    public static final ModConfigSpec.DoubleValue DRAIN_PER_TICK = BUILDER
            .comment("能力开启时每 tick 消耗的氮气量。")
            .defineInRange("drainPerTick", 0.1, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue REGEN_LV3 = BUILDER
            .comment("Lv3 起每 tick 自动回复的氮气量。")
            .defineInRange("regenPerTickLv3", 0.02, 0.0, 100.0);
    public static final ModConfigSpec.DoubleValue REGEN_LV4 = BUILDER
            .comment("Lv4 每 tick 自动回复的氮气量。")
            .defineInRange("regenPerTickLv4", 0.05, 0.0, 100.0);
    public static final ModConfigSpec.DoubleValue REGEN_LV5 = BUILDER
            .comment("Lv5 每 tick 自动回复的氮气量。")
            .defineInRange("regenPerTickLv5", 0.12, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue REDUCTION_LV1 = BUILDER
            .comment("Lv1 减伤比例（0~1）。")
            .defineInRange("damageReductionLv1", 0.25, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue REDUCTION_LV2 = BUILDER
            .comment("Lv2 减伤比例（0~1）。")
            .defineInRange("damageReductionLv2", 0.35, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue REDUCTION_LV3 = BUILDER
            .comment("Lv3 减伤比例（0~1）。")
            .defineInRange("damageReductionLv3", 0.45, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue REDUCTION_LV4 = BUILDER
            .comment("Lv4 减伤比例（0~1）。")
            .defineInRange("damageReductionLv4", 0.55, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue REDUCTION_LV5 = BUILDER
            .comment("Lv5 减伤比例（0~1），近免疫。")
            .defineInRange("damageReductionLv5", 0.85, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue REFLECT_RATIO = BUILDER
            .comment("Lv5 将受到近战伤害按此比例反弹给攻击者（预留）。")
            .defineInRange("reflectRatio", 0.5, 0.0, 10.0);

    public static final ModConfigSpec.IntValue CRYO_RADIUS = BUILDER
            .comment("Lv5 低温领域半径（格）。")
            .defineInRange("cryoRadius", 5, 1, 16);

    public static final ModConfigSpec.DoubleValue CRYO_DAMAGE = BUILDER
            .comment("Lv5 低温领域每脉冲对生物的冰冻伤害。")
            .defineInRange("cryoDamage", 1.0, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue FLIGHT_EXTRA_DRAIN = BUILDER
            .comment("Lv4+ 飞行时每 tick 额外消耗的氮气量。")
            .defineInRange("flightExtraDrainPerTick", 0.05, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue BASE_FLIGHT_SPEED = BUILDER
            .comment("Lv4+ 基础飞行速度（原版默认 0.05，疾跑 0.1）。")
            .defineInRange("baseFlightSpeed", 0.1, 0.05, 2.0);

    public static final ModConfigSpec.DoubleValue BURST_FLIGHT_SPEED = BUILDER
            .comment("Lv5 爆冲飞行速度（按住爆冲键时）。")
            .defineInRange("burstFlightSpeed", 0.5, 0.05, 2.0);

    public static final ModConfigSpec.IntValue BURST_BREAK_RADIUS = BUILDER
            .comment("Lv5 爆冲冲毁方块的半径（格）。")
            .defineInRange("burstBreakRadius", 1, 1, 4);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> NITROGEN_FLUID_IDS = BUILDER
            .comment("被识别为「氮气」的流体 ID 列表（格式 modid:fluid，用于兼容其他模组的氮气）。")
            .defineListAllowEmpty("nitrogenFluids", List.of(), () -> "", NitrogenCompat::validateFluidName);

    public static final ModConfigSpec.ConfigValue<String> NITROGEN_FLUID_TAG = BUILDER
            .comment("额外的氮气流体标签（可选，例如 c:nitrogen）。留空则不启用。")
            .define("nitrogenFluidTag", "");

    public static final ModConfigSpec.ConfigValue<String> MEKANISM_GAS_ID = BUILDER
            .comment("联动 Mekanism 时视为「氮气」的气体 ID（Mekanism 10.x 无氮气气体，默认用氢气，可改成任意气体）。")
            .define("mekanismGasId", "mekanism:hydrogen");

    public static final ModConfigSpec.IntValue MEKANISM_CHARGE_MB = BUILDER
            .comment("Lv5 反向充能每脉冲向每个储罐注入的气体量（mB）。")
            .defineInRange("mekanismChargeMb", 20, 1, 100000);

    public static final ModConfigSpec.IntValue MEKANISM_DRAIN_MB = BUILDER
            .comment("每脉冲从 Mekanism 储罐抽取的气体量（mB，用于兼容获取氮气）。")
            .defineInRange("mekanismDrainMb", 50, 1, 100000);

    public static final ModConfigSpec.DoubleValue DAMAGE_NITROGEN_COST = BUILDER
            .comment("能力开启时被打，每 1 点伤害消耗的氮气量。")
            .defineInRange("damageNitrogenCost", 0.5, 0.0, 100.0);

    public static final ModConfigSpec.BooleanValue BURST_DROP_ITEMS = BUILDER
            .comment("爆冲/下潜破坏方块时是否掉落物品（true 掉落，false 不掉）。")
            .define("burstDropItems", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static double maxNitrogen;
    public static double drainPerTick;
    public static double regenLv3, regenLv4, regenLv5;
    public static double reductionLv1, reductionLv2, reductionLv3, reductionLv4, reductionLv5;
    public static double reflectRatio;
    public static int cryoRadius;
    public static double cryoDamage;
    public static double flightExtraDrain;
    public static double baseFlightSpeed;
    public static double burstFlightSpeed;
    public static int burstBreakRadius;
    public static List<String> nitrogenFluidIds;
    public static String nitrogenFluidTag;
    public static String mekanismGasId;
    public static int mekanismChargeMb;
    public static int mekanismDrainMb;
    public static double damageNitrogenCost;
    public static boolean burstDropItems;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        maxNitrogen = MAX_NITROGEN.get();
        drainPerTick = DRAIN_PER_TICK.get();
        regenLv3 = REGEN_LV3.get();
        regenLv4 = REGEN_LV4.get();
        regenLv5 = REGEN_LV5.get();
        reductionLv1 = REDUCTION_LV1.get();
        reductionLv2 = REDUCTION_LV2.get();
        reductionLv3 = REDUCTION_LV3.get();
        reductionLv4 = REDUCTION_LV4.get();
        reductionLv5 = REDUCTION_LV5.get();
        reflectRatio = REFLECT_RATIO.get();
        cryoRadius = CRYO_RADIUS.get();
        cryoDamage = CRYO_DAMAGE.get();
        flightExtraDrain = FLIGHT_EXTRA_DRAIN.get();
        baseFlightSpeed = BASE_FLIGHT_SPEED.get();
        burstFlightSpeed = BURST_FLIGHT_SPEED.get();
        burstBreakRadius = BURST_BREAK_RADIUS.get();
        nitrogenFluidIds = List.copyOf(NITROGEN_FLUID_IDS.get());
        nitrogenFluidTag = NITROGEN_FLUID_TAG.get();
        mekanismGasId = MEKANISM_GAS_ID.get();
        mekanismChargeMb = MEKANISM_CHARGE_MB.get();
        mekanismDrainMb = MEKANISM_DRAIN_MB.get();
        damageNitrogenCost = DAMAGE_NITROGEN_COST.get();
        burstDropItems = BURST_DROP_ITEMS.get();
        NitrogenCompat.reload();
    }

    public static double reductionForLevel(int level) {
        return switch (level) {
            case 1 -> reductionLv1;
            case 2 -> reductionLv2;
            case 3 -> reductionLv3;
            case 4 -> reductionLv4;
            case 5 -> reductionLv5;
            default -> 0.0;
        };
    }

    public static double regenForLevel(int level) {
        return switch (level) {
            case 3 -> regenLv3;
            case 4 -> regenLv4;
            case 5 -> regenLv5;
            default -> 0.0;
        };
    }
}
