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

/** Разлом пространства: втягивает врагов в точку, куда смотришь. */
public class SpaceRift extends AddonAbility {
    public static final SpaceRift INSTANCE = new SpaceRift();

    private SpaceRift() {
        super("space_rift", 25f, 300);
    }

    @Override
    protected void cast(ServerLevel level, Player p) {
        Vec3 c = p.pick(24.0, 1.0f, false).getLocation();
        level.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 2.0f, 0.4f);
        level.sendParticles(ParticleTypes.PORTAL, c.x, c.y + 0.5, c.z, 120, 1.0, 1.0, 1.0, 0.8);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y + 0.5, c.z, 60, 2.0, 1.0, 2.0, 0.1);
        for (LivingEntity t : enemiesAround(level, p, c, 11.0)) {
            Vec3 v = c.subtract(t.position()).scale(0.3);
            if (v.length() > 1.8) {
                v = v.normalize().scale(1.8);
            }
            t.setDeltaMovement(v.x, Math.max(v.y, 0.25), v.z);
            t.hurtMarked = true;
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        }
    }
}
