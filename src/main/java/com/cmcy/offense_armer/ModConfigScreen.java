package com.cmcy.offense_armer;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 模组配置界面（基于 Cloth Config，可选依赖）。
 * 未安装 Cloth Config 时本类不会被调用（见 Offense_armer 里的条件判断）。
 */
public class ModConfigScreen {
    private ModConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("氮气装甲（Offense Armer）配置"));
        ConfigEntryBuilder e = builder.entryBuilder();

        ConfigCategory nitrogen = builder.getOrCreateCategory(Component.literal("氮气"));
        nitrogen.addEntry(e.startDoubleField(Component.literal("氮气上限"), Config.maxNitrogen)
                .setDefaultValue(100.0).setMin(1.0).setMax(100000.0)
                .setSaveConsumer(v -> { Config.MAX_NITROGEN.set(v); Config.maxNitrogen = v; }).build());
        nitrogen.addEntry(e.startDoubleField(Component.literal("每 tick 耗氮"), Config.drainPerTick)
                .setDefaultValue(0.1).setMin(0.0).setMax(100.0)
                .setSaveConsumer(v -> { Config.DRAIN_PER_TICK.set(v); Config.drainPerTick = v; }).build());
        nitrogen.addEntry(e.startDoubleField(Component.literal("Lv3 回氮"), Config.regenLv3)
                .setDefaultValue(0.02).setMin(0.0).setMax(100.0)
                .setSaveConsumer(v -> { Config.REGEN_LV3.set(v); Config.regenLv3 = v; }).build());
        nitrogen.addEntry(e.startDoubleField(Component.literal("Lv4 回氮"), Config.regenLv4)
                .setDefaultValue(0.05).setMin(0.0).setMax(100.0)
                .setSaveConsumer(v -> { Config.REGEN_LV4.set(v); Config.regenLv4 = v; }).build());
        nitrogen.addEntry(e.startDoubleField(Component.literal("Lv5 回氮"), Config.regenLv5)
                .setDefaultValue(0.12).setMin(0.0).setMax(100.0)
                .setSaveConsumer(v -> { Config.REGEN_LV5.set(v); Config.regenLv5 = v; }).build());
        nitrogen.addEntry(e.startDoubleField(Component.literal("过载后重开门槛（比例）"), Config.reenableThreshold)
                .setDefaultValue(0.2).setMin(0.0).setMax(1.0)
                .setSaveConsumer(v -> { Config.REENABLE_THRESHOLD.set(v); Config.reenableThreshold = v; }).build());

        ConfigCategory reduction = builder.getOrCreateCategory(Component.literal("减伤"));
        reduction.addEntry(e.startDoubleField(Component.literal("Lv1 减伤"), Config.reductionLv1)
                .setDefaultValue(0.25).setMin(0.0).setMax(1.0)
                .setSaveConsumer(v -> { Config.REDUCTION_LV1.set(v); Config.reductionLv1 = v; }).build());
        reduction.addEntry(e.startDoubleField(Component.literal("Lv2 减伤"), Config.reductionLv2)
                .setDefaultValue(0.35).setMin(0.0).setMax(1.0)
                .setSaveConsumer(v -> { Config.REDUCTION_LV2.set(v); Config.reductionLv2 = v; }).build());
        reduction.addEntry(e.startDoubleField(Component.literal("Lv3 减伤"), Config.reductionLv3)
                .setDefaultValue(0.45).setMin(0.0).setMax(1.0)
                .setSaveConsumer(v -> { Config.REDUCTION_LV3.set(v); Config.reductionLv3 = v; }).build());
        reduction.addEntry(e.startDoubleField(Component.literal("Lv4 减伤"), Config.reductionLv4)
                .setDefaultValue(0.55).setMin(0.0).setMax(1.0)
                .setSaveConsumer(v -> { Config.REDUCTION_LV4.set(v); Config.reductionLv4 = v; }).build());
        reduction.addEntry(e.startDoubleField(Component.literal("Lv5 减伤"), Config.reductionLv5)
                .setDefaultValue(0.85).setMin(0.0).setMax(1.0)
                .setSaveConsumer(v -> { Config.REDUCTION_LV5.set(v); Config.reductionLv5 = v; }).build());
        reduction.addEntry(e.startDoubleField(Component.literal("反射比例"), Config.reflectRatio)
                .setDefaultValue(0.5).setMin(0.0).setMax(10.0)
                .setSaveConsumer(v -> { Config.REFLECT_RATIO.set(v); Config.reflectRatio = v; }).build());

        ConfigCategory cryo = builder.getOrCreateCategory(Component.literal("低温领域"));
        cryo.addEntry(e.startIntField(Component.literal("领域半径（格）"), Config.cryoRadius)
                .setDefaultValue(5).setMin(1).setMax(16)
                .setSaveConsumer(v -> { Config.CRYO_RADIUS.set(v); Config.cryoRadius = v; }).build());
        cryo.addEntry(e.startDoubleField(Component.literal("冰冻伤害"), Config.cryoDamage)
                .setDefaultValue(1.0).setMin(0.0).setMax(100.0)
                .setSaveConsumer(v -> { Config.CRYO_DAMAGE.set(v); Config.cryoDamage = v; }).build());
        cryo.addEntry(e.startBooleanToggle(Component.literal("只攻击敌对生物（不攻击动物）"), Config.cryoHostileOnly)
                .setDefaultValue(true)
                .setSaveConsumer(v -> { Config.CRYO_HOSTILE_ONLY.set(v); Config.cryoHostileOnly = v; }).build());

        ConfigCategory flight = builder.getOrCreateCategory(Component.literal("飞行"));
        flight.addEntry(e.startDoubleField(Component.literal("飞行额外耗氮/tick"), Config.flightExtraDrain)
                .setDefaultValue(0.05).setMin(0.0).setMax(100.0)
                .setSaveConsumer(v -> { Config.FLIGHT_EXTRA_DRAIN.set(v); Config.flightExtraDrain = v; }).build());
        flight.addEntry(e.startDoubleField(Component.literal("基础飞行速度"), Config.baseFlightSpeed)
                .setDefaultValue(0.1).setMin(0.05).setMax(2.0)
                .setSaveConsumer(v -> { Config.BASE_FLIGHT_SPEED.set(v); Config.baseFlightSpeed = v; }).build());
        flight.addEntry(e.startDoubleField(Component.literal("爆冲飞行速度"), Config.burstFlightSpeed)
                .setDefaultValue(0.5).setMin(0.05).setMax(2.0)
                .setSaveConsumer(v -> { Config.BURST_FLIGHT_SPEED.set(v); Config.burstFlightSpeed = v; }).build());
        flight.addEntry(e.startDoubleField(Component.literal("爆冲额外耗氮/tick"), Config.burstExtraDrain)
                .setDefaultValue(0.15).setMin(0.0).setMax(100.0)
                .setSaveConsumer(v -> { Config.BURST_EXTRA_DRAIN.set(v); Config.burstExtraDrain = v; }).build());
        flight.addEntry(e.startIntField(Component.literal("爆冲破块半径（格）"), Config.burstBreakRadius)
                .setDefaultValue(1).setMin(1).setMax(4)
                .setSaveConsumer(v -> { Config.BURST_BREAK_RADIUS.set(v); Config.burstBreakRadius = v; }).build());

        ConfigCategory combat = builder.getOrCreateCategory(Component.literal("战斗"));
        combat.addEntry(e.startDoubleField(Component.literal("冲撞/下砸额外伤害"), Config.impactDamage)
                .setDefaultValue(6.0).setMin(0.0).setMax(1000.0)
                .setSaveConsumer(v -> { Config.IMPACT_DAMAGE.set(v); Config.impactDamage = v; }).build());
        combat.addEntry(e.startDoubleField(Component.literal("每点伤害耗氮"), Config.damageNitrogenCost)
                .setDefaultValue(0.5).setMin(0.0).setMax(100.0)
                .setSaveConsumer(v -> { Config.DAMAGE_NITROGEN_COST.set(v); Config.damageNitrogenCost = v; }).build());
        combat.addEntry(e.startBooleanToggle(Component.literal("爆冲破块掉落物品"), Config.burstDropItems)
                .setDefaultValue(false)
                .setSaveConsumer(v -> { Config.BURST_DROP_ITEMS.set(v); Config.burstDropItems = v; }).build());

        ConfigCategory compat = builder.getOrCreateCategory(Component.literal("兼容"));
        compat.addEntry(e.startStrField(Component.literal("Mekanism 气体 ID"), Config.mekanismGasId)
                .setDefaultValue("mekanism:hydrogen")
                .setSaveConsumer(v -> { Config.MEKANISM_GAS_ID.set(v); Config.mekanismGasId = v; }).build());
        compat.addEntry(e.startIntField(Component.literal("反向充能 mB/脉冲"), Config.mekanismChargeMb)
                .setDefaultValue(20).setMin(1).setMax(100000)
                .setSaveConsumer(v -> { Config.MEKANISM_CHARGE_MB.set(v); Config.mekanismChargeMb = v; }).build());
        compat.addEntry(e.startIntField(Component.literal("抽取 mB/脉冲"), Config.mekanismDrainMb)
                .setDefaultValue(50).setMin(1).setMax(100000)
                .setSaveConsumer(v -> { Config.MEKANISM_DRAIN_MB.set(v); Config.mekanismDrainMb = v; }).build());

        return builder.build();
    }
}
