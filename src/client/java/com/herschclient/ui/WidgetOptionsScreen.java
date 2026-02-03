package com.herschclient.ui;

import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import com.herschclient.core.settings.Setting;
import com.herschclient.core.hud.Widget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public final class WidgetOptionsScreen extends Screen {
    private final Screen parent;
    private final Widget widget;

    public WidgetOptionsScreen(Screen parent, Widget widget) {
        super(Text.literal(widget.getName() + " Options"));
        this.parent = parent;
        this.widget = widget;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = 40;

        for (Setting<?> s : widget.getSettings()) {
            if (s instanceof BoolSetting bs) {
                var btn = ButtonWidget.builder(Text.literal(label(bs)), b -> {
                    bs.toggle();
                    b.setMessage(Text.literal(label(bs)));
                }).dimensions(cx - 110, y, 220, 20).build();

                addDrawableChild(btn);
                y += 26;
            } else if (s instanceof FloatSetting fs) {
                addDrawableChild(new FloatSlider(cx - 110, y, 220, 20, fs));
                y += 26;
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Geri"), b -> {
            if (client != null) client.setScreen(parent);
        }).dimensions(cx - 110, height - 32, 220, 20).build());
    }

    private String label(BoolSetting bs) {
        return bs.getDisplayName() + ": " + (bs.get() ? "ON" : "OFF");
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private final class FloatSlider extends SliderWidget {
        private final FloatSetting setting;

        private FloatSlider(int x, int y, int w, int h, FloatSetting setting) {
            super(x, y, w, h, Text.empty(), toProgress(setting));
            this.setting = setting;
            updateMessage();
        }

        private static double toProgress(FloatSetting s) {
            float v = s.get();
            return (v - s.min()) / (s.max() - s.min());
        }

        private float fromProgress() {
            return (float) (setting.min() + value * (setting.max() - setting.min()));
        }

        @Override
        protected void updateMessage() {
            float v = fromProgress();
            // scale için 2 decimal, diğerleri 0-100 gibi de yapılabilir
            String shown = String.format("%.2f", v);
            setMessage(Text.literal(setting.getDisplayName() + ": " + shown));
        }

        @Override
        protected void applyValue() {
            setting.setClamped(fromProgress());
        }
    }
}
