package com.cmcy.offense_armer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 能力主循环：氮气收支、耗尽/过载、脱甲、调度飞行与特效、登录/重生同步。
 * 具体逻辑拆分在 {@link FlightController} / {@link CombatHandler} / {@link AbilityEffects}。
 */
@EventBusSubscriber(modid = Offense_armer.MODID)
public class AbilityEvents {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tickAbility(player);
        }
    }

    private static void tickAbility(ServerPlayer player) {
        NitrogenAbility ability = player.getData(ModAttachments.NITROGEN_ABILITY.get());
        ability.tickFallGrace();
        boolean changed = false;

        if (ability.isActive()) {
            ability.consumeNitrogen((float) Config.drainPerTick);
            if (ability.getNitrogen() <= 0f) {
                // 氮气耗尽：关闭能力、收回飞行、开落地免伤宽限
                ability.setActive(false);
                ability.setBursting(false);
                ability.setFallGraceTicks(200);
                player.fallDistance = 0f;
                FlightController.cleanupFlight(player, ability);
                player.displayClientMessage(Component.translatable("message.offense_armer.depleted"), true);
                changed = true;
            } else {
                double regen = Config.regenForLevel(ability.getLevel());
                if (regen > 0.0) {
                    ability.addNitrogen((float) regen);
                }
                // Lv1 起：自动脱甲（非氮气装甲本身）
                if (stripArmor(player)) {
                    player.displayClientMessage(Component.translatable("message.offense_armer.armor_blocked"), true);
                }
                if (ability.getLevel() >= 3) {
                    AbilityEffects.spawnTrail(player);
                }
                if (ability.getLevel() >= 4) {
                    FlightController.handleFlight(player, ability);
                }
                if (ability.getLevel() >= 5 && player.tickCount % 10 == 0) {
                    AbilityEffects.applyCryoField(player);
                }
                if (ability.getLevel() >= 5 && ability.getNitrogen() >= ability.getMaxNitrogen()
                        && player.tickCount % 10 == 0) {
                    MekanismCompat.reverseCharge(player);
                }
                changed = true;
            }
        } else {
            FlightController.cleanupFlight(player, ability);
            if (ability.getFallGraceTicks() > 0) {
                player.fallDistance = 0f;
            }
            // Lv3+ 暗中回氮
            double regen = Config.regenForLevel(ability.getLevel());
            if (regen > 0.0 && ability.getNitrogen() < ability.getMaxNitrogen()) {
                ability.addNitrogen((float) regen);
                changed = true;
            }
            // Mekanism 兼容取氮
            if (ability.getNitrogen() < ability.getMaxNitrogen() && player.tickCount % 10 == 0) {
                long drained = MekanismCompat.drainNitrogen(player);
                if (drained > 0) {
                    ability.addNitrogen(drained / MekanismCompat.MB_PER_NITROGEN);
                    changed = true;
                }
            }
        }

        if (changed) {
            ModNetwork.syncToClient(player, ability);
        }
    }

    /** 能力开启时脱下所有护甲。 */
    private static boolean stripArmor(ServerPlayer player) {
        boolean removed = false;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                player.setItemSlot(slot, ItemStack.EMPTY);
                removed = true;
            }
        }
        if (removed) {
            AbilityEffects.stripArmorBurst(player);
        }
        return removed;
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        syncIfPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncIfPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncIfPlayer(event.getEntity());
    }

    private static void syncIfPlayer(net.minecraft.world.entity.Entity entity) {
        if (entity instanceof ServerPlayer player) {
            ModNetwork.syncToClient(player, player.getData(ModAttachments.NITROGEN_ABILITY.get()));
        }
    }
}
