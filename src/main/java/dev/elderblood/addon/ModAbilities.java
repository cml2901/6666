package dev.elderblood.addon;

import com.furiusmax.bjornlib.ability.AbilityType;
import com.furiusmax.bjornlib.registry.AbilityRegistry;
import dev.elderblood.addon.ability.*;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModAbilities {
    public static final DeferredRegister<AbilityType> TYPES =
            DeferredRegister.create(AbilityRegistry.ABILITY_REGISTRY, ElderBloodAddon.MOD_ID);

    /** Способности, которые добавляются в колесо Старшей крови. */
    public static final List<AbilityType> ALL = List.of(
            CiriScream.INSTANCE, TimeStop.INSTANCE, SpaceRift.INSTANCE, GatePortal.INSTANCE,
            ShadowClones.INSTANCE, ElderFury.INSTANCE, ChaosRegen.INSTANCE);

    static {
        for (AbilityType t : ALL) {
            TYPES.register(t.getId().getPath(), () -> t);
        }
    }

    private ModAbilities() {
    }
}
