package dev.elderblood.addon.mixin;

import com.furiusmax.witcherworld.client.gui.ability.AbilitiesScreen;
import com.furiusmax.witcherworld.client.gui.ability.AbilityList;
import com.furiusmax.witcherworld.common.classes.OldBloodClass;
import com.furiusmax.witcherworld.common.classes.WitcherClass;
import dev.elderblood.addon.api.WitcherHolder;
import com.furiusmax.witcherworld.common.skills.witcher.alchemy.mutation.AcquiredTolerance;
import com.furiusmax.witcherworld.common.skills.witcher.alchemy.oils.PoisonedBlades;
import com.furiusmax.witcherworld.common.skills.witcher.alchemy.potions.HeightenedTolerance;
import com.furiusmax.witcherworld.common.skills.witcher.combat.MuscleMemory;
import com.furiusmax.witcherworld.common.skills.witcher.general.Gourmet;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OldBloodClass.class, remap = false)
public abstract class OldBloodClassMixin implements WitcherHolder {
    private static final String KEY = "ew_witcher";

    @Unique
    private WitcherClass ew$witcher;

    @Override
    public WitcherClass ew$witcher() {
        if (ew$witcher == null) {
            ew$witcher = new WitcherClass();
        }
        return ew$witcher;
    }

    @Override
    public void ew$setWitcher(WitcherClass witcher) {
        this.ew$witcher = witcher;
    }

    /** Выносливость, токсичность, регенерация — тикают как у ведьмака. */
    @Inject(method = "tick(Lnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
    private void ew$tick(Player player, CallbackInfo ci) {
        ew$witcher().tick(player);
    }

    @ModifyReturnValue(method = "saveData(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private CompoundTag ew$save(CompoundTag original) {
        original.put(KEY, ew$witcher().saveData(new CompoundTag()));
        return original;
    }

    @Inject(method = "readData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void ew$read(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(KEY)) {
            ew$witcher().readData(tag.getCompound(KEY));
        }
    }

    @ModifyReturnValue(method = "clone()Lcom/furiusmax/witcherworld/common/classes/OldBloodClass;", at = @At("RETURN"))
    private OldBloodClass ew$clone(OldBloodClass copy) {
        try {
            ((WitcherHolder) (Object) copy).ew$setWitcher(ew$witcher().clone());
        } catch (CloneNotSupportedException ignored) {
        }
        return copy;
    }

    /** Боевое/общее/алхимическое дерево ведьмака (без знаков) — в то же дерево Старшей крови. */
    @Inject(method = "abilityTree(Lnet/minecraft/world/entity/player/Player;Lcom/furiusmax/witcherworld/client/gui/ability/AbilitiesScreen;)V", at = @At("TAIL"))
    private void ew$tree(Player player, AbilitiesScreen screen, CallbackInfo ci) {
        screen.createAndAddList(new AbilityList(screen, Gourmet.INSTANCE));
        screen.createAndAddList(new AbilityList(screen, MuscleMemory.INSTANCE));
        screen.createAndAddList(new AbilityList(screen, PoisonedBlades.INSTANCE));
        screen.createAndAddList(new AbilityList(screen, HeightenedTolerance.INSTANCE));
        screen.createAndAddList(new AbilityList(screen, AcquiredTolerance.INSTANCE));
    }
}
