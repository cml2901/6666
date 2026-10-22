package dev.villageguard;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Страж деревни.
 *
 * Наследуется от Villager, поэтому у него настоящая модель жителя (используется ванильный
 * VillagerRenderer), а зомби, разбойники и прочие "охотники на жителей" считают его жителем.
 * Мозг (Brain) жителя отключён: торговля, сон, работа и паника не нужны — ИИ задан целями (goals).
 */
public class VillageGuardEntity extends Villager {
	public VillageGuardEntity(EntityType<? extends Villager> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createGuardAttributes() {
		return Villager.createAttributes()
			.add(Attributes.MAX_HEALTH, 40.0)
			// базовый урон 1 + железный меч (+6) = 7 урона
			.add(Attributes.ATTACK_DAMAGE, 1.0)
			.add(Attributes.ATTACK_KNOCKBACK, 0.4)
			.add(Attributes.MOVEMENT_SPEED, 0.38)
			.add(Attributes.FOLLOW_RANGE, 32.0)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.2);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15, true));
		this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.8));
		this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6));
		this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

		// 1) Кто ударил стража — тот и цель (в том числе игрок). Зовёт на помощь остальных стражей.
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());

		// 2) Игрок, который недавно бил жителя или стража, становится целью для всех стражей рядом.
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
			this, Player.class, 10, true, false,
			(target, level) -> target instanceof Player p && hitVillagerRecently(p)
		));

		// 3) Любые монстры в радиусе (зомби, скелеты, разбойники, пауки и т.д.), кроме криперов,
		//    чтобы они не взрывали деревню.
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
			this, Mob.class, 5, true, false,
			(target, level) -> target.getType().getCategory() == MobCategory.MONSTER
				&& !"minecraft:creeper".equals(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(target.getType())))
		));
	}

	private static boolean hitVillagerRecently(Player player) {
		LivingEntity victim = player.getLastHurtMob();
		if (victim == null) {
			return false;
		}
		// VillageGuardEntity тоже наследует Villager, так что проверка покрывает и стражей
		return victim instanceof Villager && player.tickCount - player.getLastHurtMobTimestamp() < 200;
	}

	/** Вместо мозга жителя: выдаём снаряжение, если его нет, и медленно лечимся вне боя. */
	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
			giveGear();
		}
		if (this.tickCount % 100 == 0 && this.getTarget() == null && this.getHealth() < this.getMaxHealth()) {
			this.heal(1.0F);
		}
	}

	private void giveGear() {
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
		this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
		this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
		this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
		this.setPersistenceRequired();
	}

	/** Стражи не торгуют. */
	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		return InteractionResult.PASS;
	}

	@Override
	public boolean removeWhenFarAway(double distanceSqr) {
		return false;
	}
}
