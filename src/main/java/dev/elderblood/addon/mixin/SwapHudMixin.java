package dev.elderblood.addon.mixin;

import com.furiusmax.witcherworld.core.classes.AbstractPlayerClass;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Полоски выносливости/токсичности рядом с полоской хаоса (в HUD вторая проверка — как раз ведьмачья). */
@Mixin(targets = "com.furiusmax.witcherworld.client.PlayerAbilityHUD", remap = false)
public abstract class SwapHudMixin {
    @ModifyExpressionValue(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lcom/furiusmax/witcherworld/common/attachments/PlayerClassAttachment;getActiveClass()Lcom/furiusmax/witcherworld/core/classes/AbstractPlayerClass;", ordinal = 1))
    private AbstractPlayerClass ew$swap(AbstractPlayerClass original) {
        return ClassSwap.swap(original);
    }
}
