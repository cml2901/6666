package dev.elderblood.addon.api;

import com.furiusmax.witcherworld.common.classes.OldBloodClass;
import com.furiusmax.witcherworld.core.classes.AbstractPlayerClass;

public final class ClassSwap {
    private ClassSwap() {
    }

    /** Для Старшей крови отдаёт скрытый WitcherClass, для остальных классов — как есть. */
    public static AbstractPlayerClass swap(AbstractPlayerClass original) {
        if (original instanceof OldBloodClass && original instanceof WitcherHolder holder) {
            return holder.ew$witcher();
        }
        return original;
    }
}
