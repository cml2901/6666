package dev.villageguard;

import dev.villageguard.ai.SmartAI;
import net.fabricmc.api.ModInitializer;

public class VillageGuardMod implements ModInitializer {
	public static final String MOD_ID = "villageguard";

	@Override
	public void onInitialize() {
		ModEntities.register();
		GuardSpawner.init();
		SmartAI.init();
	}
}
