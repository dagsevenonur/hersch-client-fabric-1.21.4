package com.herschclient;

import com.herschclient.core.event.EventBus;
import com.herschclient.core.hud.HudManager;
import com.herschclient.core.module.ModuleManager;
import com.herschclient.features.hud.*;
import com.herschclient.features.module.AutoSprintModule;
import com.herschclient.update.UpdateManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import com.herschclient.core.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public final class HerschClient implements ClientModInitializer {

    public static final String CLIENT_NAME = "HerschClient";
    public static final String VERSION = "0.1.0";

    public static final EventBus EVENT_BUS = new EventBus();
    public static final ModuleManager MODULES = new ModuleManager();
    public static final HudManager HUD = new HudManager();

    public static final AutoSprintModule AUTO_SPRINT = new AutoSprintModule();
    public static final com.herschclient.features.module.FullbrightModule FULLBRIGHT = new com.herschclient.features.module.FullbrightModule();
    public static final com.herschclient.features.module.ZoomModule ZOOM = new com.herschclient.features.module.ZoomModule();
    public static final com.herschclient.features.module.XrayModule XRAY = new com.herschclient.features.module.XrayModule();

    @Override
    public void onInitializeClient() {
        // ... widgets ...
        HUD.register(new FpsWidget());
        HUD.register(new CpsWidget());
        HUD.register(new CoordinatesWidget());
        HUD.register(new ClockWidget());
        HUD.register(new PingWidget());
        HUD.register(new DirectionWidget());
        HUD.register(new ArmorStatusWidget());
        HUD.register(new HeldItemDurabilityWidget());
        HUD.register(new PotionEffectsWidget());
        HUD.register(new ServerInfoWidget());
        HUD.register(new KeystrokesWidget());
        HUD.register(new TargetHudWidget());

        MODULES.register(AUTO_SPRINT);
        MODULES.register(FULLBRIGHT);
        MODULES.register(ZOOM);
        MODULES.register(XRAY);

        XRAY.init();


        // 2) Config yükle (widgetlar register edildikten sonra!)
        ConfigManager.load();

        // 3) Kapanırken otomatik kaydet
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ConfigManager.save();
        });

        // 2) Tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AUTO_SPRINT.onTick(client);
            FULLBRIGHT.onTick(client);
            ZOOM.onTick(client); // Zoom logic handles itself via keybinding check
        });

        // 3) Render2D event (HUD çizimi)
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            // tickDelta tipi sürüme göre değişebiliyor (DeltaTracker gibi).
            // Şimdilik HUD tarafında float kullanmayalım.
            HUD.render(drawContext);
            EVENT_BUS.postRender2D(drawContext);
        });

        UpdateManager.checkAndDownloadAsync();

        System.out.println("[" + CLIENT_NAME + "] Initialized v" + VERSION);
    }
}
