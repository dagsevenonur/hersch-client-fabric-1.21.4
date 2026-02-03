package com.herschclient.features.module;

import com.herschclient.core.settings.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class AutoSprintModule {

    public final BoolSetting enabled =
            new BoolSetting("auto_sprint", "Auto Sprint", true);

    public void onTick(MinecraftClient mc) {
        if (!enabled.get()) return;
        if (mc == null || mc.player == null) return;
        if (mc.currentScreen != null) return;

        ClientPlayerEntity p = mc.player;

        // vanilla-like sprint checks
        if (p.isSpectator()) return;
        if (p.isSneaking()) return;
        if (!p.isOnGround()) return;
        if (p.isSwimming()) return;
        if (p.hasVehicle()) return;

        // W basılı mı
        if (!mc.options.forwardKey.isPressed()) return;

        // açlık kontrolü (vanilla sprint şartı)
        if (p.getHungerManager().getFoodLevel() <= 6) return;

        if (!p.isSprinting()) {
            p.setSprinting(true);
        }
    }
}
