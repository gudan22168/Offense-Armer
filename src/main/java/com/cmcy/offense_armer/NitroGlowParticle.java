package com.cmcy.offense_armer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 氮气柔光粒子：小而柔的发光点，随时间收缩淡出，用于气流尾迹。
 */
public class NitroGlowParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected NitroGlowParticle(ClientLevel level, double x, double y, double z,
                                double xs, double ys, double zs, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = sprites;
        this.setSpriteFromAge(sprites);
        this.lifetime = 16;
        this.quadSize = 0.22f + level.random.nextFloat() * 0.12f;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.xd = xs;
        this.yd = ys;
        this.zd = zs;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.quadSize *= 0.94f;
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
            return new NitroGlowParticle(level, x, y, z, xs, ys, zs, sprites);
        }
    }
}
