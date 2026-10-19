package com.cmcy.offense_armer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 战斗判定：减伤、免落地、被打耗氮、Lv5 反射、Lv2 挡远程。
 */
@EventBusSubscriber(modid = Offense_armer.MODID)
public class CombatHandler {
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        NitrogenAbility ability = player.getData(ModAttachments.NITROGEN_ABILITY.get());

        // 耗尽后的落地免伤宽限（不依赖 active）
        if (ability.getFallGraceTicks() > 0 && event.getSource().is(DamageTypes.FALL)) {
            event.setCanceled(true);
            return;
        }
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
        float nitrogenCost = event.getOriginalAmount() * (float) Config.damageNitrogenCost;
        if (nitrogenCost > 0f) {
            ability.consumeNitrogen(nitrogenCost);
        }
        // Lv5 绝对装甲·反射
        if (ability.getLevel() >= 5
                && event.getSource().getEntity() instanceof LivingEntity attacker
                && attacker != player && attacker.isAlive()) {
            float reflected = event.getOriginalAmount() * (float) Config.reflectRatio;
            if (reflected > 0f) {
                attacker.hurt(attacker.damageSources().generic(), reflected);
            }
        }
    }

    /** Lv2 起：抵挡远程投射物。 */
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
        player.serverLevel().sendParticles(ModParticles.NITRO_GLOW.get(),
                pos.x, pos.y, pos.z, 16, 0.15, 0.15, 0.15, 0.02);
        projectile.discard();
    }
}
