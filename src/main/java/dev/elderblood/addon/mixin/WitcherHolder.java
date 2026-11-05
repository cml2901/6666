package dev.elderblood.addon.mixin;

import com.furiusmax.witcherworld.common.classes.WitcherClass;

/** Реализуется миксином в OldBloodClass: скрытый «ведьмачий» блок внутри Старшей крови. */
public interface WitcherHolder {
    WitcherClass ew$witcher();

    void ew$setWitcher(WitcherClass witcher);
}
