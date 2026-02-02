package com.herschclient.core.event.events;

import net.minecraft.client.MinecraftClient;

public record TickEvent(MinecraftClient client) {}