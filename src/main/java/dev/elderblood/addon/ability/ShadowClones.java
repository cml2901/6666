package dev.elderblood.addon.ability;

import dev.elderblood.addon.ModEntities;
import dev.elderblood.addon.entity.CloneEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Боевые клоны: два двойника атакуют врагов 5 секунд. Перезарядка 5 секунд. */
public class ShadowClones extends AddonAbility {
    public static final ShadowClones INSTANCE = new ShadowClones();

    private ShadowClones() {
        super("shadow_clones", 20f, 100);
    }

    @Override
    protected void cast(ServerLevel level, Player p) {
        Vec3 right = Vec3.directionFromRotation(0, p.getYRot() + 90).scale(1.6);
        for (int i = 0; i < 2; i++) {
            CloneEntity clone = ModEntities.CLONE.get().create(level);
            if (clone == null) {
                continue;
            }
            Vec3 pos = p.position().add(i == 0 ? right : right.scale(-1));
            clone.moveTo(pos.x, pos.y, pos.z, p.getYRot(), 0);
            clone.setupFrom(p);
            level.addFreshEntity(clone);
            level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 1, pos.z, 25, 0.3, 0.8, 0.3, 0.2);
        }
        level.playSound(null, p.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.5f, 1.0f);
    }
}
