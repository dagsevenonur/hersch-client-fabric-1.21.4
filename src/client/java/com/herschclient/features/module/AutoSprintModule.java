package com.herschclient.features.module;

import com.herschclient.core.module.Module;
import com.herschclient.core.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

public final class AutoSprintModule extends Module {

    public AutoSprintModule() {
        super("Auto Sprint", ModuleCategory.MOVEMENT);
    }

    /** Tick’te çağıracağız */
    public void onTick(MinecraftClient mc) {
        if (!isEnabled()) return;
        if (mc == null || mc.player == null) return;
        if (mc.currentScreen != null) return;

        var p = mc.player;

        // Basit güvenli koşullar
        if (p.isSpectator()) return;
        if (p.isSneaking()) return;
        if (p.isSwimming()) return;
        if (p.hasVehicle()) return;
        if (!mc.options.forwardKey.isPressed()) return;

        // Açlık düşükse sprint atamasın
        if (p.getHungerManager().getFoodLevel() <= 6) return;

        if (!p.isSprinting()) {
            p.setSprinting(true);
        }
    }

    @Override
    protected void onEnable() {
        // İstersen log
        // System.out.println("[HerschClient] Auto Sprint enabled");
    }

    @Override
    protected void onDisable() {
        // System.out.println("[HerschClient] Auto Sprint disabled");
    }
}
