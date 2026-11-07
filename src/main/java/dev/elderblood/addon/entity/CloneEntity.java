package dev.elderblood.addon.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

/** Боевой клон игрока: живёт 5 секунд, бьёт врагов. */
public class CloneEntity extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(CloneEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public static final int LIFETIME = 100;

    public CloneEntity(EntityType<? extends CloneEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25, true));
    }

    /** Копирует экипировку и силу удара владельца. */
    public void setupFrom(Player owner) {
        this.entityData.set(OWNER, Optional.of(owner.getUUID()));
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR || slot == EquipmentSlot.MAINHAND) {
                this.setItemSlot(slot, owner.getItemBySlot(slot).copy());
                this.setDropChance(slot, 0.0f);
            }
        }
        boolean armed = !owner.getMainHandItem().isEmpty();
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(armed ? 1.0 : Math.max(4.0, owner.getAttributeValue(Attributes.ATTACK_DAMAGE)));
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(OWNER);
    }

    public Player getOwner() {
        return getOwnerUUID().map(id -> this.level().getPlayerByUUID(id)).orElse(null);
    }

    public boolean isOwnedBy(Player p) {
        return getOwnerUUID().map(id -> id.equals(p.getUUID())).orElse(false);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.tickCount >= LIFETIME) {
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.POOF, getX(), getY() + 1, getZ(), 15, 0.3, 0.6, 0.3, 0.05);
                dev.elderblood.addon.fx.Fx.enderBurst(sl, position().add(0, 1.0, 0));
                dev.elderblood.addon.fx.Fx.sound(sl, position(), dev.elderblood.addon.fx.Fx.cloneDead(), 1.0f, 1.0f);
            }
            this.discard();
            return;
        }
        Player owner = getOwner();
        LivingEntity target = this.getTarget();
        if ((target == null || !target.isAlive()) && this.tickCount % 5 == 0) {
            LivingEntity pick = null;
            if (owner != null) {
                LivingEntity last = owner.getLastHurtMob();
                if (last != null && last.isAlive() && last != this && !(last instanceof CloneEntity)) {
                    pick = last;
                }
            }
            if (pick == null) {
                pick = this.level().getNearestEntity(LivingEntity.class,
                        net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat().range(16.0)
                                .selector(e -> e instanceof Enemy && !(e instanceof CloneEntity)),
                        this, getX(), getEyeY(), getZ(), this.getBoundingBox().inflate(16.0));
            }
            this.setTarget(pick);
        }
        if (this.getTarget() == null && owner != null && this.distanceToSqr(owner) > 36.0) {
            this.getNavigation().moveTo(owner, 1.2);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Player owner = getOwner();
        if (owner != null && source.getEntity() == owner) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof Player) && !(target instanceof CloneEntity) && super.canAttack(target);
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    protected int getBaseExperienceReward() {
        return 0;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        getOwnerUUID().ifPresent(id -> tag.putUUID("Owner", id));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            this.entityData.set(OWNER, Optional.of(tag.getUUID("Owner")));
        }
    }
}
