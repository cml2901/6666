package dev.villageguard;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class VillageGuardClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.VILLAGE_GUARD, VillageGuardRenderer::new);
	}
}
