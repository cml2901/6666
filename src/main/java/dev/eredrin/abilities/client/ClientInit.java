package dev.eredrin.abilities.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.eredrin.abilities.common.EredrinSet;
import dev.eredrin.abilities.common.UseAbilityPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Клавиши и клиентские события. Загружается только на клиенте. */
public final class ClientInit {
    private static final String CATEGORY = "key.categories.eredrin_abilities";

    /** Радиальное меню: правый Alt. */
    public static final KeyMapping RADIAL = new KeyMapping(
            "key.eredrin_abilities.radial", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT, CATEGORY);
    /** Применить выбранную способность: X (на русской раскладке это Ч). */
    public static final KeyMapping USE = new KeyMapping(
            "key.eredrin_abilities.use", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY);

    /** Номер выбранной способности (0 - за спину, 1 - на прицел, 2 - шипы). */
    public static int selected = 0;

    private ClientInit() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientInit::registerKeys);
        NeoForge.EVENT_BUS.addListener(ClientInit::onClientTick);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(RADIAL);
        event.register(USE);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        while (RADIAL.consumeClick()) {
            if (mc.screen == null && EredrinSet.isWearing(mc.player)) {
                mc.setScreen(new RadialMenuScreen());
            }
        }
        while (USE.consumeClick()) {
            if (mc.screen == null && EredrinSet.isWearing(mc.player)) {
                PacketDistributor.sendToServer(new UseAbilityPayload(selected));
            }
        }
    }
}
