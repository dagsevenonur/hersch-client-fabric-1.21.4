package com.herschclient.features.hud;

import com.herschclient.core.hud.HudDraw;
import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TargetHudWidget extends Widget {

    public final FloatSetting scale = new FloatSetting("scale", "Scale", 1.0f, 0.5f, 3.0f);
    public final BoolSetting background = new BoolSetting("background", "Background", true);
    public final FloatSetting bgOpacity = new FloatSetting("bg_opacity", "BG Opacity", 0.55f, 0.0f, 1.0f);
    public final FloatSetting padding = new FloatSetting("padding", "Padding", 6.0f, 0.0f, 12.0f);
    public final BoolSetting textShadow = new BoolSetting("text_shadow", "Text Shadow", true);

    public final BoolSetting showDistance = new BoolSetting("distance", "Show Distance", true);

    public TargetHudWidget() {
        super("TARGET HUD", 6, 108);
        settings.add(scale);
        settings.add(background);
        settings.add(bgOpacity);
        settings.add(padding);
        settings.add(textShadow);
        settings.add(showDistance);
    }

    @Override
    public int getWidth(MinecraftClient mc) {
        return super.getWidth(mc);
    }

    @Override
    public int getHeight(MinecraftClient mc) {
        return super.getHeight(mc);
    }

    @Override
    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        boolean isEditing = mc.currentScreen instanceof com.herschclient.ui.HudEditScreen;
        LivingEntity target = (mc.targetedEntity instanceof LivingEntity le) ? le : null;
        
        // Dummy data for editing
        if (target == null || !target.isAlive()) {
             if (!isEditing) return;
        }

        float sc = scale.get();
        int pad = Math.round(padding.get());

        String name = (target != null) ? target.getName().getString() : "Zombie";
        float hp = (target != null) ? target.getHealth() : 20.0f;
        float max = (target != null) ? target.getMaxHealth() : 20.0f;

        String line1 = name;
        String line2 = String.format("HP: %.1f / %.1f", hp, max);

        String line3 = "";
        if (showDistance.get()) {
            double d = (target != null && mc.player != null) ? mc.player.distanceTo(target) : 8.5;
            line3 = String.format("Dist: %.1f", d);
        }

        int w1 = mc.textRenderer.getWidth(line1);
        int w2 = mc.textRenderer.getWidth(line2);
        int w3 = line3.isEmpty() ? 0 : mc.textRenderer.getWidth(line3);
        int maxW = Math.max(w1, Math.max(w2, w3));

        int lineH = mc.textRenderer.fontHeight + 2;
        int lines = line3.isEmpty() ? 2 : 3;

        int boxW = maxW + pad * 2;
        int boxH = (lines * lineH) + pad * 2;
        
        this.cachedWidth = (int) (boxW * sc);
        this.cachedHeight = (int) (boxH * sc);

        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(sc, sc, 1.0f);

        if (background.get()) {
            int a = Math.round(bgOpacity.get() * 255.0f);
            int bgColor = (a << 24);
            int borderColor = 0x40FFFFFF;
            HudDraw.drawSharpBox(ctx, 0, 0, boxW, boxH, bgColor, borderColor);
        }

        int ty = pad;
        drawLine(ctx, line1, pad, ty, textShadow.get());
        ty += lineH;
        drawLine(ctx, line2, pad, ty, textShadow.get());
        if (!line3.isEmpty()) {
            ty += lineH;
            drawLine(ctx, line3, pad, ty, textShadow.get());
        }

        ctx.getMatrices().pop();
    }

    private void drawLine(DrawContext ctx, String text, int x, int y, boolean shadow) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (shadow) ctx.drawTextWithShadow(mc.textRenderer, text, x, y, 0xFFFFFF);
        else ctx.drawText(mc.textRenderer, text, x, y, 0xFFFFFF, false);
    }

    @Override
    public Identifier getIcon() {
        return Identifier.of("herschclient", "textures/gui/icons/target.png");
    }

    @Override
    public int getIconTextureSize() { return 64; }
}
