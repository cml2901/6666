package dev.elderblood.addon.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Остановка времени: враги вокруг замирают на 5 секунд. */
public class TimeStop extends AddonAbility {
    public static final TimeStop INSTANCE = new TimeStop();

    private TimeStop() {
        super("time_stop", 35f, 1200);
    }

    @Override
    protected void cast(ServerLevel level, Player p) {
        Vec3 c = p.position();
        level.playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 2.0f, 0.5f);
        level.sendParticles(ParticleTypes.END_ROD, c.x, c.y + 1.0, c.z, 60, 4, 1.5, 4, 0.02);
        for (LivingEntity t : enemiesAround(level, p, c, 14.0)) {
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 9));
            t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 9));
            t.addEffect(new MobEffectInstance(MobEffects.JUMP, 100, 128));
            if (t instanceof Mob m) {
                m.setTarget(null);
            }
        }
    }
}
