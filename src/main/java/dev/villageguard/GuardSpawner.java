package dev.villageguard;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * Когда в мире появляется новый обычный житель (генерация деревни, размножение, вылечили
 * зомби-жителя), с некоторой вероятностью рядом с ним появляется страж.
 *
 * Лимит: примерно один страж на трёх жителей в радиусе 48 блоков, не больше 8.
 * Уже обработанные жители помечаются тегом, поэтому при перезагрузке чанков повторного спавна нет.
 */
public final class GuardSpawner {
	private static final String CHECKED_TAG = "villageguard_checked";
	private static final float SPAWN_CHANCE = 0.5F;
	private static final int RADIUS = 48;
	private static final int MAX_GUARDS = 8;
	private static final int PER_TICK_BUDGET = 4;

	private record Pending(ServerLevel level, BlockPos pos) {
	}

	private static final Queue<Pending> QUEUE = new ConcurrentLinkedQueue<>();

	private GuardSpawner() {
	}

	public static void init() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity.getType() != EntityType.VILLAGER) {
				return; // стражи (другой тип) и всё остальное не считаются
			}
			if (entity.getTags().contains(CHECKED_TAG)) {
				return;
			}
			entity.addTag(CHECKED_TAG);
			if (entity instanceof LivingEntity living && living.isBaby()) {
				return; // детей стражи не "приводят"; вырастут — уже в счёте деревни
			}
			QUEUE.add(new Pending(level, entity.blockPosition()));
		});

		// Спавним не прямо в событии загрузки (мир ещё заполняется), а в конце тика
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			int budget = PER_TICK_BUDGET;
			Pending next;
			while (budget-- > 0 && (next = QUEUE.poll()) != null) {
				trySpawn(next);
			}
		});
	}

	private static void trySpawn(Pending pending) {
		ServerLevel level = pending.level();
		BlockPos pos = pending.pos();
		if (!level.isLoaded(pos)) {
			return;
		}
		if (level.getRandom().nextFloat() > SPAWN_CHANCE) {
			return;
		}

		AABB area = new AABB(pos).inflate(RADIUS);
		int guards = level.getEntitiesOfClass(VillageGuardEntity.class, area).size();
		int villagers = level.getEntities(EntityType.VILLAGER, area, villager -> true).size();
		int allowed = Math.min(MAX_GUARDS, (villagers + 2) / 3);
		if (guards >= allowed) {
			return;
		}

		ModEntities.VILLAGE_GUARD.spawn(level, pos, EntitySpawnReason.EVENT);
	}
}
