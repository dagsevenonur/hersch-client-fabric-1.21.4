package com.herschclient.features.hud;

import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class CoordinatesWidget extends Widget {

    public final FloatSetting scale = new FloatSetting("scale", "Scale", 1.0f, 0.5f, 3.0f);
    public final BoolSetting background = new BoolSetting("background", "Background", true);
    public final FloatSetting bgOpacity = new FloatSetting("bg_opacity", "BG Opacity", 0.55f, 0.0f, 1.0f);
    public final FloatSetting padding = new FloatSetting("padding", "Padding", 4.0f, 0.0f, 12.0f);
    public final BoolSetting textShadow = new BoolSetting("text_shadow", "Text Shadow", true);

    public final BoolSetting showY = new BoolSetting("show_y", "Show Y", true);
    public final BoolSetting showDim = new BoolSetting("show_dimension", "Show Dimension", true);

    public CoordinatesWidget() {
        super("COORDS", 6, 20);
        settings.add(scale);
        settings.add(background);
        settings.add(bgOpacity);
        settings.add(padding);
        settings.add(textShadow);
        settings.add(showY);
        settings.add(showDim);
    }

    @Override
    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String dim = "";
        if (showDim.get() && mc.world != null) {
            // örn: minecraft:overworld
            dim = mc.world.getRegistryKey().getValue().getPath().toUpperCase();
        }

        int px = (int) Math.floor(mc.player.getX());
        int py = (int) Math.floor(mc.player.getY());
        int pz = (int) Math.floor(mc.player.getZ());

        String line;
        if (showY.get()) line = "X: " + px + "  Y: " + py + "  Z: " + pz;
        else line = "X: " + px + "  Z: " + pz;

        if (!dim.isEmpty()) line = dim + " | " + line;

        drawTextBox(ctx, line);
    }

    private void drawTextBox(DrawContext ctx, String text) {
        MinecraftClient mc = MinecraftClient.getInstance();

        float sc = scale.get();
        int pad = Math.round(padding.get());

        int textW = mc.textRenderer.getWidth(text);
        int textH = mc.textRenderer.fontHeight;

        int boxW = textW + pad * 2;
        int boxH = textH + pad * 2;
        
        this.cachedWidth = (int) (boxW * sc);
        this.cachedHeight = (int) (boxH * sc);

        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(sc, sc, 1.0f);

        if (background.get()) {
            int a = Math.round(bgOpacity.get() * 255.0f);
            int bg = (a << 24);
            ctx.fill(0, 0, boxW, boxH, bg);
        }

        int tx = pad;
        int ty = pad;

        if (textShadow.get()) ctx.drawTextWithShadow(mc.textRenderer, text, tx, ty, 0xFFFFFF);
        else ctx.drawText(mc.textRenderer, text, tx, ty, 0xFFFFFF, false);

        ctx.getMatrices().pop();
    }

    @Override
    public int getWidth(MinecraftClient mc) {
        return super.getWidth(mc);
    }

    @Override
    public Identifier getIcon() {
        return Identifier.of("herschclient", "textures/gui/icons/coords.png");
    }

    @Override
    public int getIconTextureSize() {
        return 64;
    }
}
