package com.cmcy.offense_armer;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 服务端逻辑：氮气消耗/回复、能力关闭、禁穿甲、减伤、挡远程、免落地、
 * 残影尾迹、氮气飞行（Lv4）、爆气飞行冲毁方块（Lv5）。
 */
@SuppressWarnings("deprecation") // 飞行实现读取/写入 Abilities.mayfly（软弃用，仍被 mayFly() 回退读取，配合 onUpdateAbilities 同步最可靠）
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
        boolean changed = false;

        if (ability.isActive()) {
            ability.consumeNitrogen((float) Config.drainPerTick);

            double regen = Config.regenForLevel(ability.getLevel());
            if (regen > 0.0) {
                ability.addNitrogen((float) regen);
            }

            if (ability.getNitrogen() <= 0f) {
                ability.setActive(false);
                player.displayClientMessage(Component.translatable("message.offense_armer.depleted"), true);
            }

            if (stripArmor(player)) {
                player.displayClientMessage(Component.translatable("message.offense_armer.armor_blocked"), true);
            }

            if (ability.getLevel() >= 3) {
                spawnTrail(player);
            }

            if (ability.getLevel() >= 4) {
                handleFlight(player, ability);
            }

            if (ability.getLevel() >= 5 && player.tickCount % 10 == 0) {
                applyCryoField(player);
            }

            if (ability.getLevel() >= 5 && ability.getNitrogen() >= ability.getMaxNitrogen()
                    && player.tickCount % 10 == 0) {
                MekanismCompat.reverseCharge(player);
            }

            changed = true;
        } else {
            cleanupFlight(player, ability);

            // 兼容 Mekanism 氮气获取：未开启时从附近储罐/容器补充氮气
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

    /** Lv4+ 飞行；Lv5 爆冲（按住爆冲键超高速 + 冲毁方块 + 马赫环）。 */
    private static void handleFlight(ServerPlayer player, NitrogenAbility ability) {
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
        if (player.getAbilities().flying) {
            ability.consumeNitrogen((float) Config.flightExtraDrain);
            if (ability.getLevel() >= 5 && ability.isBursting()) {
                float burstSpeed = (float) Config.burstFlightSpeed;
                if (player.getAbilities().getFlyingSpeed() != burstSpeed) {
                    player.getAbilities().setFlyingSpeed(burstSpeed);
                    player.onUpdateAbilities();
                }
                if (player.isShiftKeyDown()) {
                    // R + Shift：下冲，破坏脚下方块钻下去
                    digDownward(player);
                } else {
                    // R 单独：前冲，破坏前方方块
                    breakBlocksInPath(player);
                }
                spawnMachRing(player);
            } else {
                float baseSpeed = (float) Config.baseFlightSpeed;
                if (player.getAbilities().getFlyingSpeed() != baseSpeed) {
                    player.getAbilities().setFlyingSpeed(baseSpeed);
                    player.onUpdateAbilities();
                }
            }
        }
    }

    /** 爆冲下潜（R + Shift）：破坏脚下 3×3 柱钻下去，宽度与横向一致，避免卡在两格边缘。 */
    private static void digDownward(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        int r = Config.burstBreakRadius;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = 0; dy >= -4; dy--) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos bp = center.offset(dx, dy, dz);
                    BlockState state = player.level().getBlockState(bp);
                    if (state.isAir()) {
                        continue;
                    }
                    if (state.getDestroySpeed(player.level(), bp) < 0f) {
                        continue;
                    }
                    player.level().destroyBlock(bp, Config.burstDropItems, player);
                }
            }
        }
        if (player.tickCount % 10 == 0) {
            damageNearby(player, Config.burstBreakRadius + 0.5, (float) Config.impactDamage);
        }
    }

    /** 能力关闭或降级时收回飞行权限（非创造/旁观）。 */
    private static void cleanupFlight(ServerPlayer player, NitrogenAbility ability) {
        if (ability.getLevel() < 4) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.getAbilities().mayfly) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.getAbilities().setFlyingSpeed(0.05f);
            player.onUpdateAbilities();
        }
    }

    /** Lv5 爆冲：冲毁沿途方块（按住爆冲键即触发，无速度要求）。 */
    private static void breakBlocksInPath(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        int r = Config.burstBreakRadius;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos bp = center.offset(dx, dy, dz);
                    BlockState state = player.level().getBlockState(bp);
                    if (state.isAir()) {
                        continue;
                    }
                    if (state.getDestroySpeed(player.level(), bp) < 0f) {
                        continue;
                    }
                    player.level().destroyBlock(bp, Config.burstDropItems, player);
                }
            }
        }
        if (player.tickCount % 10 == 0) {
            damageNearby(player, Config.burstBreakRadius + 0.5, (float) Config.impactDamage);
        }
    }

    /** 对附近实体造成冲撞伤害（爆冲/下砸命中）。 */
    private static void damageNearby(ServerPlayer player, double radius, float damage) {
        AABB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && !(e instanceof Player));
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), damage);
        }
    }

    /** Lv5 爆冲时的马赫环（明亮紧致的冲击波粒子环，END_ROD 更清晰）。 */
    private static void spawnMachRing(ServerPlayer player) {
        if (player.tickCount % 2 != 0) {
            return;
        }
        Vec3 pos = player.position();
        int n = 20;
        for (int k = 0; k < n; k++) {
            double angle = k * (Math.PI * 2.0 / n);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            player.serverLevel().sendParticles(ParticleTypes.END_ROD, pos.x + cos * 0.6, pos.y + 1.0, pos.z + sin * 0.6, 1, 0.0, 0.0, 0.0, 0.0);
            player.serverLevel().sendParticles(ParticleTypes.CLOUD, pos.x + cos * 0.45, pos.y + 1.0, pos.z + sin * 0.45, 1, 0.0, 0.0, 0.0, 0.01);
        }
    }

    /** Lv3 起，移动时留下白色氮气气流尾迹（沿移动反方向喷出气雾）。 */
    private static void spawnTrail(ServerPlayer player) {
        Vec3 vel = player.getDeltaMovement();
        if (vel.lengthSqr() < 0.02) {
            return;
        }
        Vec3 pos = player.position();
        Vec3 back = vel.normalize().scale(-0.4);
        double bx = pos.x + back.x;
        double by = pos.y + 1.0 + back.y;
        double bz = pos.z + back.z;
        player.serverLevel().sendParticles(ParticleTypes.CLOUD, bx, by, bz, 3, 0.28, 0.35, 0.28, 0.04);
        player.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE, bx, by, bz, 2, 0.28, 0.35, 0.28, 0.02);
    }

    /** Lv5 低温领域：冻结附近的水、减速并冰冻生物。 */
    private static void applyCryoField(ServerPlayer player) {
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
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, true), player);
            mob.hurt(mob.damageSources().freeze(), (float) Config.cryoDamage);
        }

        Vec3 pos = player.position();
        player.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y + 1.0, pos.z, 6, r * 0.5, 2.0, r * 0.5, 0.02);
    }

    /** 能力开启时脱下所有护甲，并喷出白色氮气粒子。 */
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
            Vec3 pos = player.position();
            player.serverLevel().sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 1.0, pos.z, 30, 0.4, 0.6, 0.4, 0.02);
            player.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y + 1.0, pos.z, 20, 0.4, 0.6, 0.4, 0.05);
        }
        return removed;
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        NitrogenAbility ability = player.getData(ModAttachments.NITROGEN_ABILITY.get());
        if (!ability.isActive()) {
            return;
        }

        // Lv3 起：免疫落地伤害
        if (ability.getLevel() >= 3 && event.getSource().is(DamageTypes.FALL)) {
            event.setCanceled(true);
            return;
        }

        double reduction = Config.reductionForLevel(ability.getLevel());
        if (reduction > 0.0) {
            event.setAmount(event.getAmount() * (float) (1.0 - reduction));
        }

        // 被打消耗氮气（按原始伤害比例）
        float nitrogenCost = event.getOriginalAmount() * (float) Config.damageNitrogenCost;
        if (nitrogenCost > 0f) {
            ability.consumeNitrogen(nitrogenCost);
        }

        // Lv5 绝对装甲·反射：把部分近战伤害反弹给攻击者
        if (ability.getLevel() >= 5) {
            if (event.getSource().getEntity() instanceof LivingEntity attacker
                    && attacker != player && attacker.isAlive()) {
                float reflected = event.getOriginalAmount() * (float) Config.reflectRatio;
                if (reflected > 0f) {
                    attacker.hurt(attacker.damageSources().generic(), reflected);
                }
            }
        }
    }

    /** Lv2 起：抵挡远程投射物（箭/三叉戟/火球等），命中前将其氮气化消散。 */
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) {
            return;
        }
        if (!(hit.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        NitrogenAbility ability = player.getData(ModAttachments.NITROGEN_ABILITY.get());
        if (!ability.isActive() || ability.getLevel() < 2) {
            return;
        }
        event.setCanceled(true);
        Projectile projectile = event.getProjectile();
        Vec3 pos = projectile.position();
        player.serverLevel().sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 12, 0.15, 0.15, 0.15, 0.02);
        player.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y, pos.z, 8, 0.15, 0.15, 0.15, 0.05);
        projectile.discard();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModNetwork.syncToClient(player, player.getData(ModAttachments.NITROGEN_ABILITY.get()));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModNetwork.syncToClient(player, player.getData(ModAttachments.NITROGEN_ABILITY.get()));
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModNetwork.syncToClient(player, player.getData(ModAttachments.NITROGEN_ABILITY.get()));
        }
    }
}
