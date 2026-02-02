package com.herschclient.core.hud;

import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HudManager {
    private final List<Widget> widgets = new ArrayList<>();

    public void register(Widget widget) {
        widgets.add(widget);
    }

    public List<Widget> all() {
        return Collections.unmodifiableList(widgets);
    }

    public List<Widget> getWidgets() {
        return java.util.Collections.unmodifiableList(this.widgets);
    }

    public void render(DrawContext ctx) {
        for (Widget w : widgets) {
            if (!w.isEnabled()) continue;
            w.render(ctx);
        }
    }
}
