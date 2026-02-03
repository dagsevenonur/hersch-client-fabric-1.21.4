package com.herschclient.features.hud;

import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class KeystrokesWidget extends Widget {

    public final FloatSetting scale = new FloatSetting("scale", "Scale", 1.0f, 0.5f, 3.0f);
    public final BoolSetting background = new BoolSetting("background", "Background", true);
    public final FloatSetting bgOpacity = new FloatSetting("bg_opacity", "BG Opacity", 0.35f, 0.0f, 1.0f);
    public final FloatSetting padding = new FloatSetting("padding", "Padding", 2.0f, 0.0f, 8.0f);
    public final BoolSetting textShadow = new BoolSetting("text_shadow", "Text Shadow", true);

    public KeystrokesWidget() {
        super("KEYSTROKES", 6, 78);
        settings.add(scale);
        settings.add(background);
        settings.add(bgOpacity);
        settings.add(padding);
        settings.add(textShadow);
    }

    @Override
    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return;

        float sc = scale.get();
        int pad = Math.round(padding.get());

        int key = 18;     // tek tuş kutusu
        int gap = 3;
        int w = (key * 3) + (gap * 2);
        int h = (key * 2) + gap + key; // 2 satır + space

        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(sc, sc, 1.0f);

        if (background.get()) {
            int a = Math.round(bgOpacity.get() * 255.0f);
            ctx.fill(0, 0, w + pad*2, h + pad*2, (a << 24));
        }

        int ox = pad;
        int oy = pad;

        // positions
        //   [W]
        // [A][S][D]
        // [  SPACE  ]
        drawKey(ctx, ox + key + gap, oy, key, key, "W", mc.options.forwardKey.isPressed(), textShadow.get());
        drawKey(ctx, ox, oy + key + gap, key, key, "A", mc.options.leftKey.isPressed(), textShadow.get());
        drawKey(ctx, ox + key + gap, oy + key + gap, key, key, "S", mc.options.backKey.isPressed(), textShadow.get());
        drawKey(ctx, ox + (key + gap) * 2, oy + key + gap, key, key, "D", mc.options.rightKey.isPressed(), textShadow.get());

        int spaceW = (key * 3) + (gap * 2);
        drawKey(ctx, ox, oy + (key + gap) * 2, spaceW, key, "SPACE", mc.options.jumpKey.isPressed(), textShadow.get());

        ctx.getMatrices().pop();
    }

    private void drawKey(DrawContext ctx, int x, int y, int w, int h, String label, boolean pressed, boolean shadow) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int bg = pressed ? 0xFF2E5CFF : 0xFF202020;
        int br = 0xFF2B2B2B;

        ctx.fill(x, y, x + w, y + h, bg);
        // border
        ctx.fill(x, y, x + w, y + 1, br);
        ctx.fill(x, y + h - 1, x + w, y + h, br);
        ctx.fill(x, y, x + 1, y + h, br);
        ctx.fill(x + w - 1, y, x + w, y + h, br);

        int tx = x + (w - mc.textRenderer.getWidth(label)) / 2;
        int ty = y + (h - mc.textRenderer.fontHeight) / 2;

        if (shadow) ctx.drawTextWithShadow(mc.textRenderer, label, tx, ty, 0xFFFFFF);
        else ctx.drawText(mc.textRenderer, label, tx, ty, 0xFFFFFF, false);
    }

    @Override
    public Identifier getIcon() {
        return Identifier.of("herschclient", "textures/gui/icons/keys.png");
    }

    @Override
    public int getIconTextureSize() { return 64; }
}
