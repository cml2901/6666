package dev.eredrin.abilities;

import dev.eredrin.abilities.client.ClientInit;
import dev.eredrin.abilities.common.Abilities;
import dev.eredrin.abilities.common.ServerScheduler;
import dev.eredrin.abilities.common.UseAbilityPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(EredrinAbilities.MOD_ID)
public class EredrinAbilities {
    public static final String MOD_ID = "eredrin_abilities";

    public EredrinAbilities(IEventBus modBus) {
        modBus.addListener(EredrinAbilities::registerPayloads);
        NeoForge.EVENT_BUS.addListener(ServerScheduler::onServerTick);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientInit.init(modBus);
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(UseAbilityPayload.TYPE, UseAbilityPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        Abilities.use(player, payload.ability());
                    }
                }));
    }
}
