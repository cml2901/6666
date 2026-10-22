package dev.villageguard;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	public static final ResourceKey<EntityType<?>> VILLAGE_GUARD_KEY = ResourceKey.create(
		Registries.ENTITY_TYPE,
		Identifier.fromNamespaceAndPath(VillageGuardMod.MOD_ID, "village_guard")
	);

	// MISC: страж не считается монстром (его не атакуют стражи/големы) и не спавнится естественно
	public static final EntityType<VillageGuardEntity> VILLAGE_GUARD = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		VILLAGE_GUARD_KEY,
		EntityType.Builder.<VillageGuardEntity>of(VillageGuardEntity::new, MobCategory.MISC)
			.sized(0.6F, 1.95F)
			.clientTrackingRange(10)
			.build(VILLAGE_GUARD_KEY)
	);

	private ModEntities() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(VILLAGE_GUARD, VillageGuardEntity.createGuardAttributes());
	}
}
