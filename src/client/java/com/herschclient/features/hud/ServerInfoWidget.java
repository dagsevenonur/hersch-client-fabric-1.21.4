package com.herschclient.features.hud;

import com.herschclient.core.hud.HudDraw;
import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class ServerInfoWidget extends Widget {

    public final FloatSetting scale = new FloatSetting("scale", "Scale", 1.0f, 0.5f, 3.0f);
    public final BoolSetting background = new BoolSetting("background", "Background", true);
    public final FloatSetting bgOpacity = new FloatSetting("bg_opacity", "BG Opacity", 0.55f, 0.0f, 1.0f);
    public final FloatSetting padding = new FloatSetting("padding", "Padding", 4.0f, 0.0f, 12.0f);
    public final BoolSetting textShadow = new BoolSetting("text_shadow", "Text Shadow", true);

    public final BoolSetting showAddress = new BoolSetting("address", "Show Address", true);
    public final BoolSetting showPlayers = new BoolSetting("players", "Show Players", true);
    public final BoolSetting showPing = new BoolSetting("ping", "Show Ping", true);

    public ServerInfoWidget() {
        super("SERVER INFO", 6, 62);
        settings.add(scale);
        settings.add(background);
        settings.add(bgOpacity);
        settings.add(padding);
        settings.add(textShadow);
        settings.add(showAddress);
        settings.add(showPlayers);
        settings.add(showPing);
    }

    @Override
    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        boolean sp = mc.isInSingleplayer();
        String addr = sp ? "Singleplayer" : safeServerAddress(mc.getCurrentServerEntry());

        int players = -1;
        int ping = -1;

        if (mc.getNetworkHandler() != null) {
            players = mc.getNetworkHandler().getPlayerList().size();
            PlayerListEntry me = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (me != null) ping = me.getLatency();
        }

        StringBuilder sb = new StringBuilder();
        if (showAddress.get()) sb.append(addr);
        if (showPlayers.get() && players >= 0) {
            if (!sb.isEmpty()) sb.append(" | ");
            sb.append("Players: ").append(players);
        }
        if (showPing.get() && ping >= 0) {
            if (!sb.isEmpty()) sb.append(" | ");
            sb.append("Ping: ").append(ping).append("ms");
        }

        String text = sb.isEmpty() ? "Server" : sb.toString();

        HudDraw.drawBoxedText(
                ctx, x, y,
                scale.get(),
                background.get(),
                bgOpacity.get(),
                Math.round(padding.get()),
                textShadow.get(),
                text
        );
    }

    private String safeServerAddress(ServerInfo info) {
        if (info == null) return "Multiplayer";
        if (info.address == null || info.address.isBlank()) return "Multiplayer";
        return info.address;
    }

    @Override
    public Identifier getIcon() {
        return Identifier.of("herschclient", "textures/gui/icons/server.png");
    }

    @Override
    public int getIconTextureSize() { return 64; }
}
