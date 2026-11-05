package dev.elderblood.addon.mixin;

import com.furiusmax.witcherworld.core.classes.AbstractPlayerClass;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Вход/респавн/смена измерения: медальон и атрибуты ведьмака. playerTick намеренно не трогаем. */
@Mixin(targets = "com.furiusmax.witcherworld.common.attachments.PlayerClassAttachment$EventHandler", remap = false)
public abstract class SwapEventsMixin {
    @ModifyExpressionValue(method = {
            "playerJoin(Lnet/neoforged/neoforge/event/entity/player/PlayerEvent$PlayerLoggedInEvent;)V",
            "playerRespawn(Lnet/neoforged/neoforge/event/entity/player/PlayerEvent$PlayerRespawnEvent;)V",
            "onPlayerDimChangedEvent(Lnet/neoforged/neoforge/event/entity/player/PlayerEvent$PlayerChangedDimensionEvent;)V"
    }, at = @At(value = "INVOKE", target = "Lcom/furiusmax/witcherworld/common/attachments/PlayerClassAttachment;getActiveClass()Lcom/furiusmax/witcherworld/core/classes/AbstractPlayerClass;"))
    private static AbstractPlayerClass ew$swap(AbstractPlayerClass original) {
        return ClassSwap.swap(original);
    }
}
