package com.cmcy.offense_armer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 马赫环粒子：环形贴图，随时间扩张并淡出。
 */
public class MachRingParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected MachRingParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = sprites;
        this.setSpriteFromAge(sprites);
        this.lifetime = 12;
        this.quadSize = 0.9f;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.quadSize += 0.12f;
        this.alpha = 1.0f - (float) this.age / (float) this.lifetime;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xs, double ys, double zs) {
            return new MachRingParticle(level, x, y, z, sprites);
        }
    }
}
