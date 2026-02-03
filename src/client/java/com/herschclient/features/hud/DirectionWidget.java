package com.herschclient.features.hud;

import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class DirectionWidget extends Widget {

    public final FloatSetting scale = new FloatSetting("scale", "Scale", 1.0f, 0.5f, 3.0f);
    public final BoolSetting background = new BoolSetting("background", "Background", true);
    public final FloatSetting bgOpacity = new FloatSetting("bg_opacity", "BG Opacity", 0.55f, 0.0f, 1.0f);
    public final FloatSetting padding = new FloatSetting("padding", "Padding", 4.0f, 0.0f, 12.0f);
    public final BoolSetting textShadow = new BoolSetting("text_shadow", "Text Shadow", true);

    public final BoolSetting showYaw = new BoolSetting("show_yaw", "Show Yaw", false);
    public final BoolSetting shortNames = new BoolSetting("short_names", "Short Names", true);

    public DirectionWidget() {
        super("DIRECTION", 6, 62);

        settings.add(scale);
        settings.add(background);
        settings.add(bgOpacity);
        settings.add(padding);
        settings.add(textShadow);
        settings.add(showYaw);
        settings.add(shortNames);
    }

    @Override
    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        float yaw = normalizeYaw(mc.player.getYaw());
        String dir = getDirection(yaw, shortNames.get());

        String text;
        if (showYaw.get()) {
            text = dir + " (" + Math.round(yaw) + "°)";
        } else {
            text = dir;
        }

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

        if (textShadow.get()) {
            ctx.drawTextWithShadow(mc.textRenderer, text, tx, ty, 0xFFFFFF);
        } else {
            ctx.drawText(mc.textRenderer, text, tx, ty, 0xFFFFFF, false);
        }

        ctx.getMatrices().pop();
    }

    private static float normalizeYaw(float yaw) {
        yaw %= 360.0f;
        if (yaw < 0) yaw += 360.0f;
        return yaw;
    }

    private static String getDirection(float yaw, boolean shortName) {
        int index = Math.round(yaw / 45.0f) & 7;

        if (shortName) {
            return switch (index) {
                case 0 -> "S";
                case 1 -> "SW";
                case 2 -> "W";
                case 3 -> "NW";
                case 4 -> "N";
                case 5 -> "NE";
                case 6 -> "E";
                case 7 -> "SE";
                default -> "?";
            };
        } else {
            return switch (index) {
                case 0 -> "South";
                case 1 -> "South-West";
                case 2 -> "West";
                case 3 -> "North-West";
                case 4 -> "North";
                case 5 -> "North-East";
                case 6 -> "East";
                case 7 -> "South-East";
                default -> "?";
            };
        }
    }

    @Override
    public int getWidth(MinecraftClient mc) {
        return mc.textRenderer.getWidth("South-West (360°)");
    }

    @Override
    public Identifier getIcon() {
        return Identifier.of("herschclient", "textures/gui/icons/direction.png");
    }

    @Override
    public int getIconTextureSize() {
        return 64;
    }
}
