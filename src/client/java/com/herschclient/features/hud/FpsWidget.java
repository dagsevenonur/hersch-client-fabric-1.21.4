package com.herschclient.features.hud;

import com.herschclient.core.hud.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class FpsWidget extends Widget {

    public FpsWidget() {
        super(6, 6); // sol üst
    }

    @Override
    public String getId() {
        return "hud.fps";
    }

    @Override
    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;

        int fps = mc.getCurrentFps();
        ctx.drawTextWithShadow(mc.textRenderer, "FPS: " + fps, getX(), getY(), 0xFFFFFF);
    }
}
