package dev.elderblood.addon.mixin;

import com.furiusmax.witcherworld.core.classes.AbstractPlayerClass;
import dev.elderblood.addon.api.ClassSwap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Слоты зелья/медальона в инвентаре (код Witcher World вмиксован в InventoryScreen). */
@Mixin(value = InventoryScreen.class, priority = 2000)
public abstract class SwapInventoryMixin {
    @ModifyExpressionValue(method = "/.*/", at = @At(value = "INVOKE", target = "Lcom/furiusmax/witcherworld/common/attachments/PlayerClassAttachment;getActiveClass()Lcom/furiusmax/witcherworld/core/classes/AbstractPlayerClass;"), require = 0)
    private static AbstractPlayerClass ew$swap(AbstractPlayerClass original) {
        return ClassSwap.swap(original);
    }
}
