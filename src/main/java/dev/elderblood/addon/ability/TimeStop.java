package dev.elderblood.addon.ability;

import dev.elderblood.addon.fx.Fx;
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
        super("time_stop", 5f, 40);
    }

    @Override
    protected void cast(ServerLevel level, Player p) {
        Vec3 c = p.position();
        level.playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 2.0f, 0.5f);
        level.sendParticles(ParticleTypes.END_ROD, c.x, c.y + 1.0, c.z, 60, 4, 1.5, 4, 0.02);
        Fx.sound(level, c, Fx.wraith(), 1.5f, 0.6f);
        Vec3 mid = c.add(0, 0.3, 0);
        for (int i = 0; i < 4; i++) {
            Fx.ring(level, Fx.wisp(1.0f, 30), mid.add(0, i * 0.7, 0), 1.0 + i, 24, 0.35);
        }
        Fx.sphere(level, Fx.cloud(1.6f, 24), c.add(0, 1.0, 0), 1.5, 14, 0.15);
        for (LivingEntity t : enemiesAround(level, p, c, 14.0)) {
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 9));
            t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 9));
            t.addEffect(new MobEffectInstance(MobEffects.JUMP, 100, 128));
            level.sendParticles(Fx.wisp(0.9f, 40), t.getX(), t.getY() + 1.0, t.getZ(), 4, 0.3, 0.6, 0.3, 0.02);
            if (t instanceof Mob m) {
                m.setTarget(null);
            }
        }
    }
}
