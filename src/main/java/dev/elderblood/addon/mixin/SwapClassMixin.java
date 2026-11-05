package dev.elderblood.addon.mixin;

import com.furiusmax.witcherworld.core.classes.AbstractPlayerClass;
import dev.elderblood.addon.api.ClassSwap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Везде, где Witcher World спрашивает «активный класс — ведьмак?» (броня, зелья, медальон, HUD, навыки). */
@Mixin(targets = {
        "com.furiusmax.witcherworld.client.MedallionHUD",
        "com.furiusmax.witcherworld.client.PlayerHealthArmorHUD",
        "com.furiusmax.witcherworld.common.InputEvents",
        "com.furiusmax.witcherworld.common.item.WitcherPotionItem",
        "com.furiusmax.witcherworld.common.item.armor.WolfArmor",
        "com.furiusmax.witcherworld.common.item.armor.BearArmor",
        "com.furiusmax.witcherworld.common.item.armor.FelineArmor",
        "com.furiusmax.witcherworld.common.item.armor.GriffinArmor",
        "com.furiusmax.witcherworld.common.mobeffects.WhiteHoneyEffect",
        "com.furiusmax.witcherworld.common.skills.witcher.alchemy.mutation.AcquiredTolerance",
        "com.furiusmax.witcherworld.common.skills.witcher.alchemy.mutation.EndurePain",
        "com.furiusmax.witcherworld.common.skills.witcher.alchemy.potions.HeightenedTolerance",
        "com.furiusmax.witcherworld.common.skills.witcher.general.MetabolicControl",
        "com.furiusmax.witcherworld.core.networking.PayloadHandler",
        "com.furiusmax.witcherworld.common.blocks.placeofpower.AardPlaceOfPowerBlock",
        "com.furiusmax.witcherworld.common.blocks.placeofpower.AxiiPlaceOfPowerBlock",
        "com.furiusmax.witcherworld.common.blocks.placeofpower.IgniPlaceOfPowerBlock",
        "com.furiusmax.witcherworld.common.blocks.placeofpower.QuenPlaceOfPowerBlock",
        "com.furiusmax.witcherworld.common.blocks.placeofpower.YdernPlaceOfPowerBlock"
}, remap = false)
public abstract class SwapClassMixin {
    @ModifyExpressionValue(method = "/.*/", at = @At(value = "INVOKE", target = "Lcom/furiusmax/witcherworld/common/attachments/PlayerClassAttachment;getActiveClass()Lcom/furiusmax/witcherworld/core/classes/AbstractPlayerClass;"))
    private static AbstractPlayerClass ew$swap(AbstractPlayerClass original) {
        return ClassSwap.swap(original);
    }
}
