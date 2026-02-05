package com.herschclient.features.hud;

import com.herschclient.core.hud.HudDraw;
import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class HeldItemDurabilityWidget extends Widget {

    public final FloatSetting scale = new FloatSetting("scale", "Scale", 1.0f, 0.5f, 3.0f);
    public final BoolSetting background = new BoolSetting("background", "Background", true);
    public final FloatSetting bgOpacity = new FloatSetting("bg_opacity", "BG Opacity", 0.55f, 0.0f, 1.0f);
    public final FloatSetting padding = new FloatSetting("padding", "Padding", 4.0f, 0.0f, 12.0f);
    public final BoolSetting textShadow = new BoolSetting("text_shadow", "Text Shadow", true);

    public final BoolSetting showPercent = new BoolSetting("percent", "Show %", true);
    public final BoolSetting showName = new BoolSetting("name", "Show Item Name", true);

    public HeldItemDurabilityWidget() {
        super("ITEM DURABILITY", 6, 34);
        settings.add(scale);
        settings.add(background);
        settings.add(bgOpacity);
        settings.add(padding);
        settings.add(textShadow);
        settings.add(showPercent);
        settings.add(showName);
    }

    @Override
    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        boolean isEditing = mc.currentScreen instanceof com.herschclient.ui.HudEditScreen;
        ItemStack s = mc.player.getMainHandStack();
        
        if (s == null || s.isEmpty() || !s.isDamageable()) {
            if (isEditing) {
                s = new ItemStack(net.minecraft.item.Items.DIAMOND_SWORD);
                s.setDamage(100);
            } else {
                return;
            }
        }

        int max = s.getMaxDamage();
        int dmg = s.getDamage();
        int left = Math.max(0, max - dmg);

        String dur = showPercent.get()
                ? ((int) Math.round((left / (double) max) * 100.0)) + "%"
                : String.valueOf(left);

        String text = (showName.get() ? s.getName().getString() + ": " : "") + dur;

        // Cache dimensions
        MinecraftClient finalMc = mc;
        int textW = finalMc.textRenderer.getWidth(text);
        int textH = finalMc.textRenderer.fontHeight;
        int pad = Math.round(padding.get());
        float sc = scale.get();

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
    public int getHeight(MinecraftClient mc) {
        return super.getHeight(mc);
    }

    @Override
    public Identifier getIcon() {
        return Identifier.of("herschclient", "textures/gui/icons/durability.png");
    }

    @Override
    public int getIconTextureSize() { return 64; }
}
