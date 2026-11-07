package dev.elderblood.addon;

import com.furiusmax.witcherworld.common.classes.OldBloodClass;
import com.furiusmax.witcherworld.core.registry.AttachmentsRegistry;
import dev.elderblood.addon.fx.Fx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Эффекты эндеритовой рапиры (REFM) для Старшей крови:
 * удар окутывает цель эндер-частицами, а попадание снаряда телепортирует владельца в безопасное место (раз в 3.6 с).
 */
public class EnderPassive {
    private static final long COOLDOWN = 72;

    private static boolean isElder(ServerPlayer p) {
        return p.getData(AttachmentsRegistry.PLAYER_CLASS).getActiveClass() instanceof OldBloodClass;
    }

    @SubscribeEvent
    public void onHit(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof ServerPlayer p && event.getEntity() != p && isElder(p)
                && p.level() instanceof ServerLevel level) {
            Fx.enderBurst(level, event.getEntity().position().add(0, event.getEntity().getBbHeight() * 0.6, 0));
        }
    }

    @SubscribeEvent
    public void onIncoming(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !(event.getSource().getDirectEntity() instanceof Projectile)
                || !isElder(p) || !(p.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        if (p.getPersistentData().getLong("ew_dodge") > now) {
            return;
        }
        Vec3 from = p.position().add(0, 1.0, 0);
        for (int i = 0; i < 8; i++) {
            double a = level.random.nextDouble() * Math.PI * 2;
            double d = 2.5 + level.random.nextDouble() * 2.5;
            if (p.randomTeleport(p.getX() + Math.cos(a) * d, p.getY(), p.getZ() + Math.sin(a) * d, true)) {
                p.getPersistentData().putLong("ew_dodge", now + COOLDOWN);
                Fx.enderBurst(level, from);
                Fx.enderBurst(level, p.position().add(0, 1.0, 0));
                level.playSound(null, p.blockPosition(), SoundEvents.FOX_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.2f, 1.0f);
                event.setCanceled(true);
                return;
            }
        }
    }
}
