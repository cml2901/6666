package dev.elderblood.addon.ability;

import com.furiusmax.bjornlib.ability.AbilityType;
import com.furiusmax.bjornlib.neo.AbilityEvents;
import com.furiusmax.witcherworld.common.attachments.PlayerClassAttachment;
import com.furiusmax.witcherworld.common.classes.OldBloodClass;
import com.furiusmax.witcherworld.common.skills.WitcherAbilityType;
import com.furiusmax.witcherworld.core.registry.AttachmentsRegistry;
import dev.elderblood.addon.ElderBloodAddon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

/** Базовый класс способности Старшей крови: проверка класса/хаоса, списание хаоса, вызов cast() на сервере. */
public abstract class AddonAbility extends AbilityType {
    protected AddonAbility(String path, float chaosCost, int cooldownTicks) {
        super(ResourceLocation.fromNamespaceAndPath(ElderBloodAddon.MOD_ID, path), 1, chaosCost, (float) cooldownTicks, AbilityCastType.SELF);
        NeoForge.EVENT_BUS.register(this);
    }

    protected abstract void cast(ServerLevel level, Player player);

    @SubscribeEvent
    public void onCast(AbilityEvents.CastAbility event) {
        if (event.abilityType != this) {
            return;
        }
        Player player = event.player;
        PlayerClassAttachment att = player.getData(AttachmentsRegistry.PLAYER_CLASS);
        if (!(att.getActiveClass() instanceof OldBloodClass obc)) {
            return;
        }
        if (!WitcherAbilityType.canOldBloodCastSpell(att, obc, player)) {
            return;
        }
        obc.setUncontrolValue(obc.getUncontrolValue() + 1);
        obc.setChaos(player, obc.getChaos() - getCost());
        if (player.level() instanceof ServerLevel sl) {
            cast(sl, player);
        }
    }

    /** Живые цели вокруг точки (без игроков и без клонов владельца). */
    protected static List<LivingEntity> enemiesAround(ServerLevel level, Player owner, net.minecraft.world.phys.Vec3 c, double r) {
        AABB box = new AABB(c, c).inflate(r);
        return level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != owner && e.isAlive() && !(e instanceof Player) && !(e instanceof dev.elderblood.addon.entity.CloneEntity)
                        && e.position().distanceTo(c) <= r);
    }
}
