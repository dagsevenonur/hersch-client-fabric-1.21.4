package com.herschclient.core.event;

import com.herschclient.core.event.events.Render2DEvent;
import com.herschclient.core.event.events.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBus {

    private final CopyOnWriteArrayList<Consumer<TickEvent>> tickListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Consumer<Render2DEvent>> render2DListeners = new CopyOnWriteArrayList<>();

    public void onTick(Consumer<TickEvent> listener) {
        tickListeners.add(listener);
    }

    public void onRender2D(Consumer<Render2DEvent> listener) {
        render2DListeners.add(listener);
    }

    public void postTick(MinecraftClient client) {
        TickEvent ev = new TickEvent(client);
        for (Consumer<TickEvent> l : tickListeners) l.accept(ev);
    }

    public void postRender2D(DrawContext ctx) {
        Render2DEvent ev = new Render2DEvent(ctx);
        for (Consumer<Render2DEvent> l : render2DListeners) l.accept(ev);
    }
}
