package com.herschclient.features.hud;

import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;

public final class PingWidget extends Widget {

    public final FloatSetting scale = new FloatSetting("scale", "Scale", 1.0f, 0.5f, 3.0f);
    public final BoolSetting background = new BoolSetting("background", "Background", true);
    public final FloatSetting bgOpacity = new FloatSetting("bg_opacity", "BG Opacity", 0.55f, 0.0f, 1.0f);
    public final FloatSetting padding = new FloatSetting("padding", "Padding", 4.0f, 0.0f, 12.0f);
    public final BoolSetting textShadow = new BoolSetting("text_shadow", "Text Shadow", true);

    public final BoolSetting showLabel = new BoolSetting("show_label", "Show Label", true);

    public PingWidget() {
        super("PING", 6, 48);
        settings.add(scale);
        settings.add(background);
        settings.add(bgOpacity);
        settings.add(padding);
        settings.add(textShadow);
        settings.add(showLabel);
    }

    @Override
    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        int ping = entry != null ? entry.getLatency() : -1;

        String text;
        if (ping < 0) text = showLabel.get() ? "PING: ?" : "?";
        else text = showLabel.get() ? ("PING: " + ping + "ms") : (ping + "ms");

        drawTextBox(ctx, text);
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
        return Identifier.of("herschclient", "textures/gui/icons/ping.png");
    }

    @Override
    public int getIconTextureSize() {
        return 64;
    }
}
