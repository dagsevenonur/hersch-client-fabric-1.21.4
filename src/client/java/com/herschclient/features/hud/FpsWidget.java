package com.herschclient.features.hud;

import com.herschclient.core.hud.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class FpsWidget extends Widget {

    public FpsWidget() {
        super("FPS", 6, 6);
    }

    @Override
    public void render(DrawContext ctx) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        int fps = mc.getCurrentFps(); // statik değil!
        ctx.drawText(mc.textRenderer, "FPS: " + fps, this.x, this.y, 0xFFFFFF, true);
    }

    @Override
    public int getWidth(MinecraftClient mc) {
        return mc.textRenderer.getWidth("FPS: 999");
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
