package com.herschclient.core.config;

import com.google.gson.*;
import com.herschclient.HerschClient;
import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import com.herschclient.core.settings.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "herschclient.json";

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    /** Oyuncu oyuna girince çağır: config oku -> widget + setting uygula */
    public static void load() {
        Path p = path();
        if (!Files.exists(p)) return;

        try {
            String json = Files.readString(p, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            JsonObject widgetsObj = root.has("widgets") ? root.getAsJsonObject("widgets") : null;
            if (widgetsObj == null) return;

            for (Widget w : HerschClient.HUD.getWidgets()) {
                String key = widgetKey(w);

                if (!widgetsObj.has(key)) continue;
                JsonObject wj = widgetsObj.getAsJsonObject(key);

                // enabled
                if (wj.has("enabled")) {
                    w.setEnabled(wj.get("enabled").getAsBoolean());
                }

                // position
                if (wj.has("x") && wj.has("y")) {
                    w.setPos(wj.get("x").getAsInt(), wj.get("y").getAsInt());
                }

                // settings
                if (wj.has("settings")) {
                    JsonObject sj = wj.getAsJsonObject("settings");
                    applySettings(w, sj);
                }
            }

            System.out.println("[HerschClient] Config loaded: " + p);

        } catch (Exception e) {
            System.err.println("[HerschClient] Config load failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Oyuncu çıkarken çağır: widget + setting oku -> config yaz */
    public static void save() {
        Path p = path();

        try {
            JsonObject root = new JsonObject();
            root.addProperty("version", HerschClient.VERSION);

            JsonObject widgetsObj = new JsonObject();

            for (Widget w : HerschClient.HUD.getWidgets()) {
                JsonObject wj = new JsonObject();

                wj.addProperty("enabled", w.isEnabled());
                wj.addProperty("x", w.getX());
                wj.addProperty("y", w.getY());

                JsonObject sj = new JsonObject();
                for (Setting<?> s : w.getSettings()) {
                    if (s instanceof BoolSetting bs) {
                        sj.addProperty(bs.getKey(), bs.get());
                    } else if (s instanceof FloatSetting fs) {
                        sj.addProperty(fs.getKey(), fs.get());
                    } else {
                        // ileride yeni setting tipi ekleyince burayı genişletirsin
                        // şimdilik yok sayıyoruz
                    }
                }
                wj.add("settings", sj);

                widgetsObj.add(widgetKey(w), wj);
            }

            root.add("widgets", widgetsObj);

            Files.createDirectories(p.getParent());
            Files.writeString(p, GSON.toJson(root), StandardCharsets.UTF_8);

            System.out.println("[HerschClient] Config saved: " + p);

        } catch (IOException e) {
            System.err.println("[HerschClient] Config save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void applySettings(Widget w, JsonObject sj) {
        for (Setting<?> s : w.getSettings()) {
            String k = s.getKey();
            if (!sj.has(k)) continue;

            try {
                if (s instanceof BoolSetting bs) {
                    bs.set(sj.get(k).getAsBoolean());
                } else if (s instanceof FloatSetting fs) {
                    fs.setClamped(sj.get(k).getAsFloat());
                }
            } catch (Exception ignored) {
                // tek bir setting bozuksa tüm load’u patlatmayalım
            }
        }
    }

    private static String widgetKey(Widget w) {
        // Şimdilik: widget adı
        // (istersen sonra Widget'a "id" alanı ekleyip burayı id yaparız)
        return w.getName().toLowerCase();
    }
}
