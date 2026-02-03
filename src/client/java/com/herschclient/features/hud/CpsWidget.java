package com.herschclient.features.hud;

import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.ArrayDeque;
import java.util.Deque;

public final class CpsWidget extends Widget {

    // Settings
    public final FloatSetting scale = new FloatSetting("scale", "Scale", 1.0f, 0.5f, 3.0f);
    public final BoolSetting background = new BoolSetting("background", "Background", true);
    public final FloatSetting bgOpacity = new FloatSetting("bg_opacity", "BG Opacity", 0.55f, 0.0f, 1.0f);
    public final FloatSetting padding = new FloatSetting("padding", "Padding", 4.0f, 0.0f, 12.0f);
    public final BoolSetting textShadow = new BoolSetting("text_shadow", "Text Shadow", true);
    public final BoolSetting showRightClick = new BoolSetting("show_right_click", "Show Right Click", false);

    // Click timestamps (ms)
    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();

    public CpsWidget() {
        super("CPS", 6, 24);

        settings.add(scale);
        settings.add(background);
        settings.add(bgOpacity);
        settings.add(padding);
        settings.add(textShadow);
        settings.add(showRightClick);
    }

    /** Hud render sırasında çağıracağız: click state poll */
    private void pollClicks(MinecraftClient mc) {
        // Mouse polling: attack/use key (Minecraft input system)
        // Bu yaklaşım “basılı tutma”da sürekli artmasın diye edge-detect yapar.
        // Edge state’i tutuyoruz:
        boolean attackNow = mc.options.attackKey.isPressed();
        boolean useNow = mc.options.useKey.isPressed();

        if (attackNow && !attackPrev) leftClicks.addLast(System.currentTimeMillis());
        if (useNow && !usePrev) rightClicks.addLast(System.currentTimeMillis());

        attackPrev = attackNow;
        usePrev = useNow;
    }

    private boolean attackPrev = false;
    private boolean usePrev = false;

    private static void prune(Deque<Long> q, long now) {
        long cutoff = now - 1000L;
        while (!q.isEmpty() && q.peekFirst() < cutoff) q.removeFirst();
    }

    @Override
    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // input poll
        pollClicks(mc);

        long now = System.currentTimeMillis();
        prune(leftClicks, now);
        prune(rightClicks, now);

        int l = leftClicks.size();
        int r = rightClicks.size();

        String text = showRightClick.get()
                ? (l + " | " + r + " CPS")
                : (l + " CPS");

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
            int bg = (a << 24); // black with alpha
            ctx.fill(0, 0, boxW, boxH, bg);
        }

        int tx = pad;
        int ty = pad;

        if (textShadow.get()) {
            ctx.drawTextWithShadow(mc.textRenderer, text, tx, ty, 0xFFFFFF);
        } else {
            ctx.drawText(mc.textRenderer, text, tx, ty, 0xFFFFFF, false);
        }

        ctx.getMatrices().pop();
    }

    @Override
    public int getWidth(MinecraftClient mc) {
        // tahmini genişlik (opsiyonel)
        return mc.textRenderer.getWidth("99 | 99 CPS");
    }

    @Override
    public Identifier getIcon() {
        // icon dosyanı ekleyeceğiz (aşağıda)
        return Identifier.of("herschclient", "textures/gui/icons/cps.png");
    }

    @Override
    public int getIconTextureSize() {
        return 64;
    }
}
