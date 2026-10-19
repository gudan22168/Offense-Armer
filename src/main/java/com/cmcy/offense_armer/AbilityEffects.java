package com.cmcy.offense_armer;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 能力特效：马赫环、氮气尾迹、低温领域。
 */
public final class AbilityEffects {
    private AbilityEffects() {
    }

    /** Lv5 爆冲时的马赫环（环形贴图粒子，扩张淡出）。 */
    public static void spawnMachRing(ServerPlayer player) {
        if (player.tickCount % 2 != 0) {
            return;
        }
        Vec3 pos = player.position();
        player.serverLevel().sendParticles(ModParticles.MACH_RING.get(),
                pos.x, pos.y + 1.0, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    /** Lv3 起，移动时留下氮气柔光尾迹。 */
    public static void spawnTrail(ServerPlayer player) {
        Vec3 vel = player.getDeltaMovement();
        if (vel.lengthSqr() < 0.02) {
            return;
        }
        Vec3 pos = player.position();
        Vec3 back = vel.normalize().scale(-0.4);
        player.serverLevel().sendParticles(ModParticles.NITRO_GLOW.get(),
                pos.x + back.x, pos.y + 1.0 + back.y, pos.z + back.z, 3, 0.2, 0.3, 0.2, 0.0);
    }

    /** Lv5 低温领域：冻结附近的水、减速并冰冻生物。 */
    public static void applyCryoField(ServerPlayer player) {
        int r = Config.cryoRadius;
        BlockPos center = player.blockPosition();
        Level level = player.level();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos bp = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(bp);
                    FluidState fluid = state.getFluidState();
                    if (fluid.is(FluidTags.WATER) && fluid.isSource()) {
                        level.setBlock(bp, Blocks.FROSTED_ICE.defaultBlockState(), 3);
                    }
                }
            }
        }

        AABB area = player.getBoundingBox().inflate(r);
        List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && !(e instanceof Player));
        for (LivingEntity mob : mobs) {
            if (Config.cryoHostileOnly && !(mob instanceof Monster)) {
                continue;
            }
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, true), player);
            mob.hurt(player.damageSources().playerAttack(player), (float) Config.cryoDamage);
        }

        Vec3 pos = player.position();
        player.serverLevel().sendParticles(ModParticles.NITRO_GLOW.get(),
                pos.x, pos.y + 1.0, pos.z, 6, r * 0.5, 2.0, r * 0.5, 0.0);
    }

    /** 脱甲时喷出氮气粒子。 */
    public static void stripArmorBurst(ServerPlayer player) {
        Vec3 pos = player.position();
        player.serverLevel().sendParticles(ModParticles.NITRO_GLOW.get(),
                pos.x, pos.y + 1.0, pos.z, 40, 0.4, 0.6, 0.4, 0.02);
    }
}
