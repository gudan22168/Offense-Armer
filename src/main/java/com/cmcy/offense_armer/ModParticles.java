package com.cmcy.offense_armer;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 自定义粒子类型（贴图来自 shuimo0413/Iron-Elden-Ring-Particle-library，MIT）。
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Offense_armer.MODID);

    /** 马赫环：环形冲击波贴图，扩张 + 淡出。 */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MACH_RING =
            PARTICLE_TYPES.register("mach_ring", () -> new SimpleParticleType(false));

    /** 氮气柔光：用于气流尾迹。 */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NITRO_GLOW =
            PARTICLE_TYPES.register("nitro_glow", () -> new SimpleParticleType(false));
}
