package dev.eredrin.abilities.common;

import dev.eredrin.abilities.EredrinAbilities;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Серверная логика трёх способностей Эредина. */
public final class Abilities {
    public static final int BEHIND = 0;
    public static final int BLINK = 1;
    public static final int ICE = 2;
    public static final int COUNT = 3;

    // ---- настройки (при желании меняйте и пересобирайте) ----
    private static final int[] COOLDOWN_TICKS = {80, 40, 100}; // 4 c, 2 c, 5 c
    private static final double BEHIND_RANGE = 40.0;
    private static final double BLINK_RANGE = 40.0;
    private static final double SPIKE_RANGE = 24.0;
    private static final float SPIKE_DAMAGE = 4.0F;
    private static final int EFFECT_TICKS = 600; // 30 секунд
    private static final int EFFECT_AMPLIFIER = 4; // уровень V

    private static final Map<UUID, long[]> COOLDOWNS = new HashMap<>();

    private Abilities() {}

    public static void use(ServerPlayer player, int id) {
        if (id < 0 || id >= COUNT) {
            return;
        }
        if (!EredrinSet.isWearing(player)) {
            message(player, "need_set");
            return;
        }
        long now = player.serverLevel().getGameTime();
        long[] ready = COOLDOWNS.computeIfAbsent(player.getUUID(), k -> new long[COUNT]);
        if (now < ready[id]) {
            long seconds = (ready[id] - now + 19) / 20;
            player.displayClientMessage(
                    Component.translatable("message." + EredrinAbilities.MOD_ID + ".cooldown", seconds), true);
            return;
        }
        boolean done = switch (id) {
            case BEHIND -> teleportBehind(player);
            case BLINK -> blink(player);
            default -> iceSpikes(player);
        };
        if (done) {
            ready[id] = now + COOLDOWN_TICKS[id];
        }
    }

    // ------------------------------------------------------------------ телепорт за спину цели

    private static boolean teleportBehind(ServerPlayer p) {
        LivingEntity target = rayEntity(p, BEHIND_RANGE);
        if (target == null) {
            message(p, "no_target");
            return false;
        }
        Vec3 facing = Vec3.directionFromRotation(0.0F, target.getYRot());
        for (double dist : new double[] {1.7, 1.3, 2.3, 3.0}) {
            Vec3 spot = target.position().subtract(facing.scale(dist));
            Vec3 safe = findSafe(p, spot);
            if (safe != null) {
                teleport(p, safe, target.getEyePosition());
                return true;
            }
        }
        message(p, "no_space");
        return false;
    }

    // ------------------------------------------------------------------ телепорт на прицел

    private static boolean blink(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(BLINK_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        boolean missed = hit.getType() == HitResult.Type.MISS;
        boolean onTop = !missed && hit.getDirection() == Direction.UP;
        Vec3 aim = hit.getLocation();

        for (int i = 0; i <= 14; i++) {
            Vec3 spot;
            if (onTop && i == 0) {
                spot = aim; // встаём на поверхность, на которую смотрим
            } else {
                double back = 0.3 * i + (missed ? 0.0 : 0.4);
                spot = aim.subtract(look.scale(back)).subtract(0.0, p.getEyeHeight(), 0.0);
            }
            Vec3 safe = findSafe(p, spot);
            if (safe != null) {
                teleport(p, safe, null);
                return true;
            }
        }
        message(p, "no_space");
        return false;
    }

    // ------------------------------------------------------------------ ледяные шипы

    private static boolean iceSpikes(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        LivingEntity target = rayEntity(p, SPIKE_RANGE);
        Vec3 start = p.position();

        Vec3 dir;
        double length;
        if (target != null) {
            Vec3 d = target.position().subtract(start);
            dir = new Vec3(d.x, 0.0, d.z);
            length = dir.length() + 0.5;
        } else {
            Vec3 look = p.getLookAngle();
            dir = new Vec3(look.x, 0.0, look.z);
            length = SPIKE_RANGE;
        }
        if (dir.lengthSqr() < 1.0E-4) {
            dir = Vec3.directionFromRotation(0.0F, p.getYRot());
        }
        dir = dir.normalize();

        Set<UUID> alreadyHit = new HashSet<>();
        int count = (int) Math.max(1, Math.floor((length - 1.0) / 1.3));
        for (int i = 0; i < count; i++) {
            Vec3 pos = start.add(dir.scale(1.4 + i * 1.3));
            ServerScheduler.schedule(i * 2, () -> spawnSpike(level, p, pos, alreadyHit));
        }
        level.playSound(null, start.x, start.y, start.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.2F);
        return true;
    }

    private static void spawnSpike(ServerLevel level, ServerPlayer owner, Vec3 pos, Set<UUID> alreadyHit) {
        double gy = groundY(level, pos);
        Vec3 base = new Vec3(pos.x, gy, pos.z);
        level.playSound(null, base.x, base.y, base.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.6F, 1.5F);

        // Попадание: все, кого задел шип. Эффекты накладываются только в этом случае.
        AABB box = new AABB(base.x - 0.9, base.y - 0.5, base.z - 0.9, base.x + 0.9, base.y + 2.5, base.z + 0.9);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != owner && entity.isAlive() && !entity.isSpectator())) {
            if (alreadyHit.add(e.getUUID())) {
                applyIce(level, owner, e);
            }
        }

        // Анимация: шип поднимается из земли и опускается обратно.
        for (int age = 0; age <= 12; age += 2) {
            final int a = age;
            ServerScheduler.schedule(age, () -> spikeParticles(level, base, a));
        }
    }

    private static void applyIce(ServerLevel level, ServerPlayer owner, LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, EFFECT_TICKS, EFFECT_AMPLIFIER));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_TICKS, EFFECT_AMPLIFIER));
        target.setTicksFrozen(Math.max(target.getTicksFrozen(), 160));
        target.hurt(level.damageSources().indirectMagic(owner, owner), SPIKE_DAMAGE);
    }

    private static void spikeParticles(ServerLevel level, Vec3 base, int age) {
        double h = age <= 6 ? 0.5 + age * 0.35 : 2.6 - (age - 6) * 0.3;
        if (h <= 0.1) {
            return;
        }
        BlockParticleOption ice = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState());
        for (double y = 0.0; y <= h; y += 0.3) {
            double r = 0.55 * (1.0 - y / (h + 0.4));
            int points = r > 0.2 ? 5 : 2;
            for (int k = 0; k < points; k++) {
                double ang = k * 2.0 * Math.PI / points + y * 2.0;
                level.sendParticles(ice, base.x + Math.cos(ang) * r, base.y + y, base.z + Math.sin(ang) * r,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, base.x, base.y + h, base.z, 2, 0.1, 0.1, 0.1, 0.02);
    }

    private static double groundY(ServerLevel level, Vec3 pos) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int x = Mth.floor(pos.x);
        int z = Mth.floor(pos.z);
        int y = Mth.floor(pos.y);
        for (int dy = 3; dy >= -5; dy--) {
            m.set(x, y + dy, z);
            BlockPos below = m.below();
            boolean solidBelow = !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
            boolean freeHere = level.getBlockState(m).getCollisionShape(level, m).isEmpty();
            if (solidBelow && freeHere) {
                return m.getY();
            }
        }
        return pos.y;
    }

    // ------------------------------------------------------------------ общие помощники

    /** Живое существо под прицелом (с учётом блоков на пути). */
    private static LivingEntity rayEntity(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));
        BlockHitResult blockHit = p.level().clip(
                new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        double maxDistSq = blockHit.getType() != HitResult.Type.MISS
                ? blockHit.getLocation().distanceToSqr(eye)
                : range * range;
        AABB search = p.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(p, eye, end, search,
                e -> !e.isSpectator() && e.isPickable() && e instanceof LivingEntity, maxDistSq);
        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    /** Ищет рядом с точкой место, где поместится игрок. */
    private static Vec3 findSafe(ServerPlayer p, Vec3 base) {
        for (double dy : new double[] {0.0, 1.0, -1.0, 2.0, -2.0}) {
            Vec3 c = base.add(0.0, dy, 0.0);
            AABB box = p.getDimensions(p.getPose()).makeBoundingBox(c);
            if (p.level().noCollision(p, box)) {
                return c;
            }
        }
        return null;
    }

    /** Телепорт; если lookAt задан, игрок разворачивается к этой точке. */
    private static void teleport(ServerPlayer p, Vec3 pos, Vec3 lookAt) {
        ServerLevel level = p.serverLevel();
        effects(level, p.position());

        float yaw = p.getYRot();
        float pitch = p.getXRot();
        if (lookAt != null) {
            Vec3 d = lookAt.subtract(pos.add(0.0, p.getEyeHeight(), 0.0));
            double flat = Math.sqrt(d.x * d.x + d.z * d.z);
            yaw = (float) (Mth.atan2(d.z, d.x) * (180.0 / Math.PI)) - 90.0F;
            pitch = (float) -(Mth.atan2(d.y, flat) * (180.0 / Math.PI));
        }
        p.connection.teleport(pos.x, pos.y, pos.z, yaw, pitch);
        p.fallDistance = 0.0F;
        effects(level, pos);
    }

    private static void effects(ServerLevel level, Vec3 at) {
        level.sendParticles(ParticleTypes.PORTAL, at.x, at.y + 1.0, at.z, 40, 0.3, 0.6, 0.3, 0.3);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void message(ServerPlayer p, String key) {
        p.displayClientMessage(Component.translatable("message." + EredrinAbilities.MOD_ID + "." + key), true);
    }
}
