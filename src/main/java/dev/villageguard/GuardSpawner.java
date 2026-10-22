package dev.villageguard;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;

/**
 * Когда в мире появляется (загружается) обычный житель, с некоторой вероятностью рядом с ним
 * появляется страж.
 *
 * Лимит: примерно один страж на трёх жителей в радиусе 48 блоков, не больше 8. Благодаря лимиту
 * повторная загрузка чанков не приводит к бесконечному спавну: стражи лишь доукомплектовываются
 * до нормы.
 */
public final class GuardSpawner {
	private static final float SPAWN_CHANCE = 0.5F;
	private static final int RADIUS = 48;
	private static final int MAX_GUARDS = 8;
	private static final int PER_TICK_BUDGET = 4;

	private record Pending(ServerLevel level, BlockPos pos) {
	}

	private static final Queue<Pending> QUEUE = new ConcurrentLinkedQueue<>();
	// Жители, которых уже обработали в этой игровой сессии
	private static final Set<UUID> SEEN = new HashSet<>();

	private GuardSpawner() {
	}

	public static void init() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (!(entity instanceof Villager villager) || entity instanceof VillageGuardEntity) {
				return; // стражи и всё остальное не считаются
			}
			if (villager.isBaby() || !SEEN.add(entity.getUUID())) {
				return;
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
		int villagers = level.getEntitiesOfClass(Villager.class, area).size() - guards;
		int allowed = Math.min(MAX_GUARDS, (villagers + 2) / 3);
		if (guards >= allowed) {
			return;
		}

		ModEntities.VILLAGE_GUARD.spawn(level, pos, EntitySpawnReason.EVENT);
	}
}
