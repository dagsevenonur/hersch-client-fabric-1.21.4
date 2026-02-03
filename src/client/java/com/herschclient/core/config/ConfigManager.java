package com.herschclient.core.config;

import com.google.gson.*;
import com.herschclient.HerschClient;
import com.herschclient.core.hud.Widget;
import com.herschclient.core.module.Module;
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

    /** Oyuncu oyuna girince çağır: config oku -> widget + module + setting uygula */
    public static void load() {
        Path p = path();
        if (!Files.exists(p)) return;

        try {
            String json = Files.readString(p, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            // ---------- WIDGETS ----------
            JsonObject widgetsObj = root.has("widgets") ? root.getAsJsonObject("widgets") : null;
            if (widgetsObj != null) {
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
            }

            // ---------- MODULES ----------
            JsonObject modulesObj = root.has("modules") ? root.getAsJsonObject("modules") : null;
            if (modulesObj != null) {
                for (Module m : HerschClient.MODULES.all()) {
                    String key = moduleKey(m);
                    if (!modulesObj.has(key)) continue;

                    boolean enabled = modulesObj.get(key).getAsBoolean();
                    m.setEnabled(enabled);
                }
            }

            System.out.println("[HerschClient] Config loaded: " + p);

        } catch (Exception e) {
            System.err.println("[HerschClient] Config load failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Oyuncu çıkarken çağır: widget + module + setting oku -> config yaz */
    public static void save() {
        Path p = path();

        try {
            JsonObject root = new JsonObject();
            root.addProperty("version", HerschClient.VERSION);

            // ---------- WIDGETS ----------
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
                        // yeni setting tipi eklenirse burayı genişlet
                    }
                }
                wj.add("settings", sj);

                widgetsObj.add(widgetKey(w), wj);
            }

            root.add("widgets", widgetsObj);

            // ---------- MODULES ----------
            JsonObject modulesObj = new JsonObject();
            for (Module m : HerschClient.MODULES.all()) {
                modulesObj.addProperty(moduleKey(m), m.isEnabled());
            }
            root.add("modules", modulesObj);

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
                // tek setting bozuksa tüm load’u patlatmayalım
            }
        }
    }

    private static String widgetKey(Widget w) {
        return w.getName().toLowerCase();
    }

    private static String moduleKey(Module m) {
        return m.getName().toLowerCase();
    }
}
