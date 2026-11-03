package dev.eredrin.abilities.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.eredrin.abilities.EredrinAbilities;
import dev.eredrin.abilities.common.Abilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

/** Радиальное меню на три способности. Держите правый Alt, наведите мышь на сектор и отпустите. */
public class RadialMenuScreen extends Screen {
    private static final float INNER = 24.0F;
    private static final float OUTER = 84.0F;
    private static final int STEPS = 24;

    private static final ItemStack[] ICONS = {
            new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_EYE), new ItemStack(Items.PACKED_ICE)
    };
    private static final String[] KEYS = {"behind", "blink", "ice"};

    public RadialMenuScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int hovered(double mouseX, double mouseY) {
        double dx = mouseX - this.width / 2.0;
        double dy = mouseY - this.height / 2.0;
        if (Math.sqrt(dx * dx + dy * dy) < INNER) {
            return -1;
        }
        double deg = Math.toDegrees(Math.atan2(dy, dx));
        // сектор 0 - вверху, 1 - справа внизу, 2 - слева внизу
        return (int) Math.floor((((deg + 150.0) % 360.0) + 360.0) % 360.0 / 120.0);
    }

    private void choose(int index) {
        if (index >= 0 && index < Abilities.COUNT) {
            ClientInit.selected = index;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.translatable(
                        "message." + EredrinAbilities.MOD_ID + ".selected",
                        Component.translatable("ability." + EredrinAbilities.MOD_ID + "." + KEYS[index])), true);
            }
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
        }
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        if (!InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT)) {
            double mx = mc.mouseHandler.xpos() * this.width / (double) mc.getWindow().getScreenWidth();
            double my = mc.mouseHandler.ypos() * this.height / (double) mc.getWindow().getScreenHeight();
            choose(hovered(mx, my));
            this.onClose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int index = hovered(mouseX, mouseY);
        if (index >= 0) {
            choose(index);
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float cx = this.width / 2.0F;
        float cy = this.height / 2.0F;
        int hover = hovered(mouseX, mouseY);

        for (int i = 0; i < Abilities.COUNT; i++) {
            int color;
            if (i == hover) {
                color = 0xD0E8B84A;
            } else if (i == ClientInit.selected) {
                color = 0xB04A7C8C;
            } else {
                color = 0xB0141418;
            }
            double start = -90.0 - 60.0 + 120.0 * i + 1.5;
            double end = start + 120.0 - 3.0;
            drawSector(g, cx, cy, start, end, color);
        }

        for (int i = 0; i < Abilities.COUNT; i++) {
            double mid = Math.toRadians(-90.0 + 120.0 * i);
            float ix = cx + (float) Math.cos(mid) * (INNER + OUTER) / 2.0F;
            float iy = cy + (float) Math.sin(mid) * (INNER + OUTER) / 2.0F;
            g.renderItem(ICONS[i], (int) ix - 8, (int) iy - 8);
        }

        int shown = hover >= 0 ? hover : ClientInit.selected;
        g.drawCenteredString(this.font,
                Component.translatable("ability." + EredrinAbilities.MOD_ID + "." + KEYS[shown]),
                (int) cx, (int) (cy + OUTER + 8), 0xFFFFFF);
        g.drawCenteredString(this.font,
                Component.translatable("screen." + EredrinAbilities.MOD_ID + ".hint"),
                (int) cx, (int) (cy - OUTER - 16), 0xAAAAAA);
    }

    private void drawSector(GuiGraphics g, float cx, float cy, double startDeg, double endDeg, int argb) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f m = g.pose().last().pose();
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int s = 0; s <= STEPS; s++) {
            double a = Math.toRadians(startDeg + (endDeg - startDeg) * s / STEPS);
            float cos = (float) Math.cos(a);
            float sin = (float) Math.sin(a);
            b.addVertex(m, cx + cos * INNER, cy + sin * INNER, 0.0F).setColor(argb);
            b.addVertex(m, cx + cos * OUTER, cy + sin * OUTER, 0.0F).setColor(argb);
        }
        BufferUploader.drawWithShader(b.buildOrThrow());
        RenderSystem.disableBlend();
    }
}
