package dev.eredrin.abilities.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Проверка, что на игроке надет полный комплект Эредина из мода Fantasy Armor. */
public final class EredrinSet {
    private static final String NAMESPACE = "fantasy_armor";

    private EredrinSet() {}

    public static boolean isWearing(Player player) {
        return has(player, EquipmentSlot.HEAD, "eredrin_helmet")
                && has(player, EquipmentSlot.CHEST, "eredrin_chestplate")
                && has(player, EquipmentSlot.LEGS, "eredrin_leggings")
                && has(player, EquipmentSlot.FEET, "eredrin_boots");
    }

    private static boolean has(Player player, EquipmentSlot slot, String path) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key.getNamespace().equals(NAMESPACE) && key.getPath().equals(path);
    }
}
