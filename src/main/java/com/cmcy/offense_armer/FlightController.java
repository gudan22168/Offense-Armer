package com.cmcy.offense_armer;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 飞行控制：Lv4+ 飞行权限、Lv5 爆冲（破块/下钻/马赫环）、关闭时收回飞行。
 */
@SuppressWarnings("deprecation")
public final class FlightController {
    private FlightController() {
    }

    /** Lv4+ 飞行；Lv5 爆冲（按住爆冲键超高速 + 冲毁方块 + 马赫环）。 */
    public static void handleFlight(ServerPlayer player, NitrogenAbility ability) {
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
        if (!player.getAbilities().flying) {
            return;
        }
        ability.consumeNitrogen((float) Config.flightExtraDrain);
        if (ability.getLevel() >= 5 && ability.isBursting()) {
            ability.consumeNitrogen((float) Config.burstExtraDrain);
            float burstSpeed = (float) Config.burstFlightSpeed;
            if (player.getAbilities().getFlyingSpeed() != burstSpeed) {
                player.getAbilities().setFlyingSpeed(burstSpeed);
                player.onUpdateAbilities();
            }
            if (player.isShiftKeyDown()) {
                digDownward(player);
            } else {
                breakBlocksInPath(player);
            }
            AbilityEffects.spawnMachRing(player);
        } else {
            float baseSpeed = (float) Config.baseFlightSpeed;
            if (player.getAbilities().getFlyingSpeed() != baseSpeed) {
                player.getAbilities().setFlyingSpeed(baseSpeed);
                player.onUpdateAbilities();
            }
        }
    }

    /** 爆冲下潜（R + Shift）：破坏脚下 3×3 柱钻下去。 */
    public static void digDownward(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        int r = Config.burstBreakRadius;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = 0; dy >= -4; dy--) {
                for (int dz = -r; dz <= r; dz++) {
                    tryBreak(player, center.offset(dx, dy, dz));
                }
            }
        }
        if (player.tickCount % 10 == 0) {
            damageNearby(player, Config.burstBreakRadius + 0.5, (float) Config.impactDamage);
        }
    }

    /** 能力关闭或降级时收回飞行权限（非创造/旁观）。 */
    public static void cleanupFlight(ServerPlayer player, NitrogenAbility ability) {
        if (ability.getLevel() < 4 || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.getAbilities().mayfly) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.getAbilities().setFlyingSpeed(0.05f);
            player.onUpdateAbilities();
        }
    }

    /** Lv5 爆冲：冲毁沿途方块。 */
    public static void breakBlocksInPath(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        int r = Config.burstBreakRadius;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    tryBreak(player, center.offset(dx, dy, dz));
                }
            }
        }
        if (player.tickCount % 10 == 0) {
            damageNearby(player, Config.burstBreakRadius + 0.5, (float) Config.impactDamage);
        }
    }

    private static void tryBreak(ServerPlayer player, BlockPos bp) {
        BlockState state = player.level().getBlockState(bp);
        if (state.isAir()) {
            return;
        }
        if (state.getDestroySpeed(player.level(), bp) < 0f) {
            return;
        }
        player.level().destroyBlock(bp, Config.burstDropItems, player);
    }

    /** 对附近实体造成冲撞伤害（爆冲/下砸命中）。 */
    public static void damageNearby(ServerPlayer player, double radius, float damage) {
        AABB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && !(e instanceof Player));
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), damage);
        }
    }
}
