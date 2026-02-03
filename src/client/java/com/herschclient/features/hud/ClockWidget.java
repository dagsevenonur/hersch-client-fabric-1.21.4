package com.herschclient.features.hud;

import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ClockWidget extends Widget {

    public final FloatSetting scale = new FloatSetting("scale", "Scale", 1.0f, 0.5f, 3.0f);
    public final BoolSetting background = new BoolSetting("background", "Background", true);
    public final FloatSetting bgOpacity = new FloatSetting("bg_opacity", "BG Opacity", 0.55f, 0.0f, 1.0f);
    public final FloatSetting padding = new FloatSetting("padding", "Padding", 4.0f, 0.0f, 12.0f);
    public final BoolSetting textShadow = new BoolSetting("text_shadow", "Text Shadow", true);

    public final BoolSetting use24h = new BoolSetting("use_24h", "24h Format", true);
    public final BoolSetting showSeconds = new BoolSetting("show_seconds", "Show Seconds", false);

    private static final DateTimeFormatter F24 = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter F24S = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter F12 = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter F12S = DateTimeFormatter.ofPattern("hh:mm:ss a");

    public ClockWidget() {
        super("CLOCK", 6, 34);
        settings.add(scale);
        settings.add(background);
        settings.add(bgOpacity);
        settings.add(padding);
        settings.add(textShadow);
        settings.add(use24h);
        settings.add(showSeconds);
    }

    @Override
    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();

        LocalTime now = LocalTime.now();
        DateTimeFormatter fmt =
                use24h.get()
                        ? (showSeconds.get() ? F24S : F24)
                        : (showSeconds.get() ? F12S : F12);

        String text = "TIME: " + now.format(fmt);
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
        return mc.textRenderer.getWidth("TIME: 23:59:59");
    }

    @Override
    public Identifier getIcon() {
        return Identifier.of("herschclient", "textures/gui/icons/clock.png");
    }

    @Override
    public int getIconTextureSize() {
        return 64;
    }
}
