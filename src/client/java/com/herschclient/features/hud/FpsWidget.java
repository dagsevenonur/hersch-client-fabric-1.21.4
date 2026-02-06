package com.herschclient.features.hud;

import com.herschclient.core.hud.HudDraw;
import com.herschclient.core.hud.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;

public final class FpsWidget extends Widget {

    public FpsWidget() {
        super("FPS", 6, 6);
        settings.add(scale);
        settings.add(background);
        settings.add(bgOpacity);
        settings.add(padding);
        settings.add(textShadow);
    }

    public final FloatSetting scale = new FloatSetting("scale", "Scale", 1.0f, 0.5f, 3.0f);
    public final BoolSetting background = new BoolSetting("background", "Background", true);
    public final FloatSetting bgOpacity = new FloatSetting("bg_opacity", "BG Opacity", 0.55f, 0.0f, 1.0f);
    public final FloatSetting padding = new FloatSetting("padding", "Padding", 4.0f, 0.0f, 12.0f);
    public final BoolSetting textShadow = new BoolSetting("text_shadow", "Text Shadow", true);


    @Override
    public void render(DrawContext ctx) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        String text = mc.getCurrentFps() + " FPS";

        float sc = scale.get();
        int pad = Math.round(padding.get());

        // ölçüm (scale sonrası)
        int textW = mc.textRenderer.getWidth(text);
        int textH = mc.textRenderer.fontHeight;

        int boxW = textW + pad * 2;
        int boxH = textH + pad * 2;
        
        this.cachedWidth = (int) (boxW * sc);
        this.cachedHeight = (int) (boxH * sc);

        HudDraw.drawBoxedText(
                ctx, x, y,
                scale.get(),
                background.get(),
                bgOpacity.get(),
                Math.round(padding.get()),
                textShadow.get(),
                text
        );
    }

    @Override
    public int getWidth(MinecraftClient mc) {
        return super.getWidth(mc);
    }

    @Override
    public Identifier getIcon() {
        return Identifier.of("herschclient", "textures/gui/icons/fps.png");
    }

    @Override
    public int getIconTextureSize() {
        return 64; // fps.png 64x64 ise
    }
}
