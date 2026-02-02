package com.herschclient.core.event.events;

import net.minecraft.client.gui.DrawContext;

public record Render2DEvent(DrawContext ctx) {}