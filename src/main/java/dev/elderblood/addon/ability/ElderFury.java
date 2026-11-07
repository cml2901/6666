package dev.elderblood.addon.ability;

import dev.elderblood.addon.fx.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/** Ярость Старшей крови: урон и скорость атаки на минуту. */
public class ElderFury extends AddonAbility {
    public static final ElderFury INSTANCE = new ElderFury();

    private ElderFury() {
        super("elder_fury", 5f, 40);
    }

    @Override
    protected void cast(ServerLevel level, Player p) {
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1));
        p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 2));
        level.playSound(null, p.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6f, 1.6f);
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, p.getX(), p.getY() + 1.8, p.getZ(), 12, 0.4, 0.3, 0.4, 0.0);
        net.minecraft.world.phys.Vec3 c = p.position().add(0, 1.0, 0);
        Fx.sound(level, c, Fx.bigExplosion(), 0.7f, 0.6f);
        Fx.sound(level, c, SoundEvents.WITHER_AMBIENT, 1.0f, 0.8f);
        Fx.ring(level, Fx.flame(1.6f, 24), p.position().add(0, 0.1, 0), 0.8, 24, 0.3);
        Fx.sphere(level, Fx.cloud(1.4f, 20), c, 1.0, 16, 0.12);
        for (int i = 0; i < 20; i++) {
            level.sendParticles(Fx.flame(1.2f, 30), p.getX() + (level.random.nextDouble() - 0.5), p.getY() + level.random.nextDouble() * 1.8, p.getZ() + (level.random.nextDouble() - 0.5), 0, 0, 1, 0, 0.12);
        }
    }
}
