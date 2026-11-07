package dev.elderblood.addon.ability;

import dev.elderblood.addon.fx.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Врата-портал: 1-й каст ставит первые врата, 2-й — вторые; пара живёт 60 секунд, шаг в любые врата переносит в другие. */
public class GatePortal extends AddonAbility {
    public static final GatePortal INSTANCE = new GatePortal();

    private static final class Gate {
        ResourceKey<Level> dim;
        Vec3 a;
        Vec3 b;
        long expire;
    }

    private static final Map<UUID, Gate> GATES = new HashMap<>();

    private GatePortal() {
        super("gate_portal", 5f, 40);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> GATES.clear());
    }

    @Override
    protected void cast(ServerLevel level, Player p) {
        Gate g = GATES.get(p.getUUID());
        Vec3 here = p.position();
        long now = level.getGameTime();
        if (g == null || g.b != null || g.dim != level.dimension() || g.expire < now) {
            g = new Gate();
            g.dim = level.dimension();
            g.a = here;
            g.expire = now + 1200;
            GATES.put(p.getUUID(), g);
        } else {
            g.b = here;
            g.expire = now + 1200;
        }
        level.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.5f, 0.7f);
        Fx.sound(level, here, Fx.teleport(), 1.2f, 1.0f);
        Fx.enderBurst(level, here.add(0, 1.0, 0));
    }

    private void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (GATES.isEmpty() || server.getTickCount() % 2 != 0) {
            return;
        }
        Iterator<Map.Entry<UUID, Gate>> it = GATES.entrySet().iterator();
        while (it.hasNext()) {
            Gate g = it.next().getValue();
            ServerLevel level = server.getLevel(g.dim);
            if (level == null || g.expire < level.getGameTime()) {
                it.remove();
                continue;
            }
            boolean show = level.getGameTime() % 6 == 0;
            for (Vec3 pos : g.b == null ? new Vec3[]{g.a} : new Vec3[]{g.a, g.b}) {
                if (show) {
                    level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 1.0, pos.z, 10, 0.3, 0.8, 0.3, 0.1);
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.x, pos.y + 1.0, pos.z, 4, 0.3, 0.8, 0.3, 0.05);
                    Fx.ring(level, Fx.wisp(0.8f, 14), pos.add(0, 1.0, 0), 0.9, 10, 0.02);
                    level.sendParticles(Fx.spark(0.5f, 18), pos.x, pos.y + 1.0, pos.z, 3, 0.4, 0.9, 0.4, 0.01);
                }
            }
            if (g.b == null) {
                continue;
            }
            teleportNear(level, g.a, g.b);
            teleportNear(level, g.b, g.a);
        }
    }

    private static void teleportNear(ServerLevel level, Vec3 from, Vec3 to) {
        long now = level.getGameTime();
        AABB box = new AABB(from, from).inflate(0.9, 1.0, 0.9).move(0, 1.0, 0);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e.getPersistentData().getLong("ew_gate") > now) {
                continue;
            }
            e.getPersistentData().putLong("ew_gate", now + 40);
            e.teleportTo(level, to.x, to.y, to.z, Set.of(), e.getYRot(), e.getXRot());
            level.playSound(null, e.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            Fx.enderBurst(level, from.add(0, 1.0, 0));
            Fx.enderBurst(level, to.add(0, 1.0, 0));
        }
    }
}
