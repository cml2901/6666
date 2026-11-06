package dev.elderblood.addon;

import dev.elderblood.addon.entity.CloneEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/** Аддон: вживляет ведьмака (без знаков) в класс «Старшая кровь» Witcher World и добавляет новые способности. */
@Mod(ElderBloodAddon.MOD_ID)
public class ElderBloodAddon {
    public static final String MOD_ID = "elderblood_addon";

    public ElderBloodAddon(IEventBus modBus) {
        ModEntities.ENTITIES.register(modBus);
        ModAbilities.TYPES.register(modBus);
        modBus.addListener((EntityAttributeCreationEvent e) ->
                e.put(ModEntities.CLONE.get(), CloneEntity.createAttributes().build()));
    }
}
