package dev.elderblood.addon.ability;

import dev.elderblood.addon.fx.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/** Хаотическая регенерация: мгновенное лечение и быстрая регенерация. */
public class ChaosRegen extends AddonAbility {
    public static final ChaosRegen INSTANCE = new ChaosRegen();

    private ChaosRegen() {
        super("chaos_regen", 30f, 600);
    }

    @Override
    protected void cast(ServerLevel level, Player p) {
        p.heal(6.0f);
        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 3));
        level.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2f, 1.4f);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1.0, p.getZ(), 20, 0.5, 0.8, 0.5, 0.0);
        for (int i = 0; i < 24; i++) {
            double a = i * 0.7;
            level.sendParticles(Fx.wisp(0.9f, 30), p.getX() + Math.cos(a) * 0.9, p.getY() + 0.1 + i * 0.07, p.getZ() + Math.sin(a) * 0.9, 0, 0, 1, 0, 0.1);
        }
        Fx.sphere(level, Fx.spark(0.5f, 20), p.position().add(0, 1.0, 0), 0.8, 12, 0.1);
    }
}
