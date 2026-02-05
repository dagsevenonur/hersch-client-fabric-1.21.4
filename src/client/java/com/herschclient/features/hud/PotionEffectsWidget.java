package com.herschclient.features.hud;

import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PotionEffectsWidget extends Widget {

    public final FloatSetting scale = new FloatSetting("scale", "Scale", 1.0f, 0.5f, 3.0f);
    public final BoolSetting background = new BoolSetting("background", "Background", true);
    public final FloatSetting bgOpacity = new FloatSetting("bg_opacity", "BG Opacity", 0.55f, 0.0f, 1.0f);
    public final FloatSetting padding = new FloatSetting("padding", "Padding", 4.0f, 0.0f, 12.0f);
    public final BoolSetting textShadow = new BoolSetting("text_shadow", "Text Shadow", true);

    public final BoolSetting showAmplifier = new BoolSetting("amp", "Show Level", true);
    public final BoolSetting showSeconds = new BoolSetting("secs", "Show Seconds", true);

    public PotionEffectsWidget() {
        super("POTIONS", 6, 48);
        settings.add(scale);
        settings.add(background);
        settings.add(bgOpacity);
        settings.add(padding);
        settings.add(textShadow);
        settings.add(showAmplifier);
        settings.add(showSeconds);
    }

    @Override
    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
        if (effects.isEmpty()) return;

        effects.sort(Comparator.comparing(
                e -> e.getEffectType().value().getName().getString()
        ));

        float sc = scale.get();
        int pad = Math.round(padding.get());

        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(sc, sc, 1.0f);

        // satırları hazırlayalım
        List<String> lines = new ArrayList<>();
        effects.sort(Comparator.comparing(
                e -> e.getEffectType().value().getName().getString()
        ));

        for (StatusEffectInstance e : effects) {
            String name = e.getEffectType().value().getName().getString();

            if (showAmplifier.get()) {
                int lvl = e.getAmplifier() + 1;
                name += " " + lvl;
            }

            String time = formatTime(e.getDuration());
            lines.add(name + " (" + time + ")");
        }

        int maxW = 0;
        for (String s : lines) maxW = Math.max(maxW, mc.textRenderer.getWidth(s));
        int lineH = mc.textRenderer.fontHeight + 2;
        int boxW = maxW + pad * 2;
        int boxH = (lines.size() * lineH) + pad * 2;

        if (background.get()) {
            int a = Math.round(bgOpacity.get() * 255.0f);
            ctx.fill(0, 0, boxW, boxH, (a << 24));
        }

        this.cachedWidth = (int) (boxW * sc);
        this.cachedHeight = (int) (boxH * sc);

        int ty = pad;
        for (String s : lines) {
            if (textShadow.get()) ctx.drawTextWithShadow(mc.textRenderer, s, pad, ty, 0xFFFFFF);
            else ctx.drawText(mc.textRenderer, s, pad, ty, 0xFFFFFF, false);
            ty += lineH;
        }

        ctx.getMatrices().pop();
    }

    private String formatTime(int ticks) {
        int totalSeconds = ticks / 20;
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        if (!showSeconds.get()) return m + "m";
        return String.format("%d:%02d", m, s);
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
    public Identifier getIcon() {
        return Identifier.of("herschclient", "textures/gui/icons/potions.png");
    }

    @Override
    public int getIconTextureSize() { return 64; }
}
