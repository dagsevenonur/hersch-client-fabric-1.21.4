package com.herschclient.features.module;

import com.herschclient.core.module.Module;
import com.herschclient.core.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

public final class FullbrightModule extends Module {

    private double originalGamma = 1.0;

    public FullbrightModule() {
        super("Fullbright", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) {
            // Trigger lightmap update
            mc.options.getGamma().setValue(mc.options.getGamma().getValue());
        }
    }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) {
            // Trigger lightmap update
            mc.options.getGamma().setValue(mc.options.getGamma().getValue());
        }
    }

    public void onTick(MinecraftClient mc) {
        // No tick logic needed
    }
}
