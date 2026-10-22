package dev.villageguard;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.VillagerRenderer;

public class VillageGuardClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Ванильная модель, текстура и слои жителя (в руке виден меч)
		EntityRenderers.register(ModEntities.VILLAGE_GUARD, context -> new VillagerRenderer(context));
	}
}
