package dev.elderblood.addon.fx;

import com.furiusmax.bjornlib.client.particle.GenericParticleData;
import com.furiusmax.bjornlib.registry.ParticleRegistry;
import com.furiusmax.witcherworld.core.registry.SoundRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/** Общие эффекты: чёрно-фиолетовые частицы Witcher World/BjornLib и эндеритовые всплески (как у эндеритовой рапиры). */
public final class Fx {
    private Fx() {
    }

    /** Чёрно-фиолетовая частица заданного типа. */
    public static GenericParticleData violet(ParticleType<?> type, float scale, int life) {
        GenericParticleData d = new GenericParticleData(type);
        d.r1 = 0.62f; d.g1 = 0.18f; d.b1 = 1.0f; d.a1 = 0.95f;
        d.r2 = 0.10f; d.g2 = 0.0f; d.b2 = 0.18f; d.a2 = 0.0f;
        d.scale1 = scale;
        d.scale2 = scale * 0.2f;
        d.lifetime = life;
        return d;
    }

    public static GenericParticleData dark(ParticleType<?> type, float scale, int life) {
        GenericParticleData d = violet(type, scale, life);
        d.r1 = 0.20f; d.g1 = 0.0f; d.b1 = 0.32f; d.a1 = 0.9f;
        d.r2 = 0.0f; d.g2 = 0.0f; d.b2 = 0.0f;
        return d;
    }

    public static ParticleOptions wisp(float s, int life) {
        return violet(ParticleRegistry.WISP_PARTICLE, s, life);
    }

    public static ParticleOptions spark(float s, int life) {
        return violet(ParticleRegistry.SPARK_PARTICLE, s, life);
    }

    public static ParticleOptions cloud(float s, int life) {
        return dark(ParticleRegistry.CLOUD_PARTICLE, s, life);
    }

    public static ParticleOptions flame(float s, int life) {
        return violet(ParticleRegistry.FLAME_PARTICLE, s, life);
    }

    public static ParticleOptions scream(float s, int life) {
        return violet(com.furiusmax.witcherworld.core.registry.ParticleRegistry.SCREAM.get(), s, life);
    }

    /** Частицы, разлетающиеся наружу по сфере (как ENDER_PARTICLES у рапиры). */
    public static void sphere(ServerLevel l, ParticleOptions o, Vec3 c, double radius, int n, double speed) {
        RandomSource r = l.getRandom();
        for (int i = 0; i < n; i++) {
            double theta = r.nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * r.nextDouble() - 1);
            double dx = Math.sin(phi) * Math.cos(theta);
            double dy = Math.cos(phi);
            double dz = Math.sin(phi) * Math.sin(theta);
            l.sendParticles(o, c.x + dx * radius, c.y + dy * radius, c.z + dz * radius, 0, dx, dy, dz, speed);
        }
    }

    /** Кольцо частиц, расширяющееся по горизонтали. */
    public static void ring(ServerLevel l, ParticleOptions o, Vec3 c, double radius, int n, double speed) {
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            double dx = Math.cos(a);
            double dz = Math.sin(a);
            l.sendParticles(o, c.x + dx * radius, c.y, c.z + dz * radius, 0, dx, 0, dz, speed);
        }
    }

    /** Эндеритовый всплеск: портал + дым + фиолетовые искры. */
    public static void enderBurst(ServerLevel l, Vec3 c) {
        sphere(l, ParticleTypes.PORTAL, c, 1.0, 28, 0.9);
        sphere(l, ParticleTypes.LARGE_SMOKE, c, 0.8, 10, 0.05);
        sphere(l, spark(0.5f, 24), c, 0.6, 14, 0.12);
        sphere(l, cloud(0.9f, 20), c, 0.4, 6, 0.04);
    }

    public static void sound(ServerLevel l, Vec3 c, SoundEvent s, float vol, float pitch) {
        l.playSound(null, c.x, c.y, c.z, s, SoundSource.PLAYERS, vol, pitch);
    }

    public static SoundEvent bansheeHit() {
        return SoundRegistry.BANSHEE_HIT.get();
    }

    public static SoundEvent bansheeAmbient() {
        return SoundRegistry.BANSHEE_AMBIENT.get();
    }

    public static SoundEvent cloneDead() {
        return SoundRegistry.BANSHEE_CLON_DEAD.get();
    }

    public static SoundEvent teleport() {
        return SoundRegistry.LESHEN_TP.get();
    }

    public static SoundEvent bigExplosion() {
        return SoundRegistry.BIG_EXPLOSION.get();
    }

    public static SoundEvent wraith() {
        return SoundRegistry.NOONWRAITH_AMBIENT.get();
    }
}
