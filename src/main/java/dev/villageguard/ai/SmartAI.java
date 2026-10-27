package dev.villageguard.ai;

import java.util.Set;

import dev.villageguard.VillageGuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

/**
 * Вешает дополнительный ИИ на жителей и монстров при загрузке в мир.
 * Коровы, свиньи и вся остальная мирная живность не затрагиваются.
 */
public final class SmartAI {
	/** Мобы с собственным "мозгом" или особой механикой — их не трогаем. */
	private static final Set<String> SKIP = Set.of(
		"warden", "enderman", "piglin", "piglin_brute", "hoglin", "zoglin",
		"ghast", "blaze", "shulker", "ender_dragon", "wither", "vex", "slime", "magma_cube",
		"guardian", "elder_guardian", "silverfish", "endermite", "phantom", "breeze", "creaking"
	);

	private SmartAI() {
	}

	public static void init() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof VillageGuardEntity) {
				return; // у стражей своё поведение
			}
			if (entity instanceof Villager villager) {
				addVillagerGoals(villager);
			} else if (entity instanceof Monster monster) {
				addMonsterGoals(monster);
			}
		});
	}

	private static void addVillagerGoals(Villager v) {
		if (v.isBaby()) {
			return;
		}
		var goals = v.getGoalSelector();
		// Убегать от любых монстров, не только от зомби
		goals.addGoal(1, new AvoidEntityGoal<>(v, Monster.class, 10.0F, 0.9, 1.15));
		goals.addGoal(4, new VillagerTorchGoal(v));
		goals.addGoal(5, new VillagerFarmGoal(v));
	}

	private static void addMonsterGoals(Monster m) {
		String id = BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).getPath();
		if (SKIP.contains(id) || m.getType().getCategory() != MobCategory.MONSTER) {
			return;
		}
		var goals = m.getGoalSelector();

		// Все зомби ломают двери на любой сложности
		if (m instanceof Zombie zombie) {
			zombie.setCanBreakDoors(true);
			if (!(m instanceof Husk) && !(m instanceof Drowned)) { // они не горят на солнце
				goals.addGoal(3, new FleeSunGoal(m, 1.0));
			}
		}

		// Скелеты держат дистанцию от игрока
		if (m instanceof AbstractSkeleton) {
			goals.addGoal(2, new AvoidEntityGoal<>(m, Player.class, 5.0F, 1.0, 1.3, e -> true));
		}

		if (m instanceof Creeper) {
			return;
		}

		// Отступают при низком здоровье
		goals.addGoal(1, new AvoidEntityGoal<>(
			m, Player.class, 12.0F, 1.0, 1.25,
			e -> m.getHealth() < m.getMaxHealth() * 0.3F
		));
		// Зовут на помощь тех, кто рядом
		goals.addGoal(1, new HurtByTargetGoal(m).setAlertOthers());
		// Ближники окружают игрока группой
		if (!(m instanceof RangedAttackMob)) {
			goals.addGoal(1, new SurroundTargetGoal(m, 1.1));
		}
	}
}
