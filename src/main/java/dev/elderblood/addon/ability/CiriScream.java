package dev.elderblood.addon.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Крик Цири: волна звука — урон, оглушение и отбрасывание. */
public class CiriScream extends AddonAbility {
    public static final CiriScream INSTANCE = new CiriScream();

    private CiriScream() {
        super("ciri_scream", 20f, 400);
    }

    @Override
    protected void cast(ServerLevel level, Player p) {
        Vec3 c = p.position();
        level.playSound(null, p.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0f, 1.2f);
        level.sendParticles(ParticleTypes.SONIC_BOOM, c.x, c.y + 1.0, c.z, 1, 0, 0, 0, 0);
        for (LivingEntity t : enemiesAround(level, p, c, 9.0)) {
            t.hurt(level.damageSources().sonicBoom(p), 10.0f);
            Vec3 dir = t.position().subtract(c).multiply(1, 0, 1).normalize();
            t.push(dir.x * 1.6, 0.45, dir.z * 1.6);
            t.hurtMarked = true;
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
            t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
        }
    }
}
