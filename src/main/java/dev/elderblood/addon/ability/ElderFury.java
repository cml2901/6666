package dev.elderblood.addon.ability;

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
        super("elder_fury", 40f, 2400);
    }

    @Override
    protected void cast(ServerLevel level, Player p) {
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1));
        p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 2));
        level.playSound(null, p.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6f, 1.6f);
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, p.getX(), p.getY() + 1.8, p.getZ(), 12, 0.4, 0.3, 0.4, 0.0);
    }
}
