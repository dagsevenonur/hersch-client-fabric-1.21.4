package com.herschclient.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herschclient.HerschClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public final class UpdateManager {

    // Repo bilgini buraya yaz
    private static final String OWNER = "YOUR_GITHUB_USERNAME_OR_ORG";
    private static final String REPO  = "YOUR_REPO_NAME";

    // Release asset seçimi: istersen jar adını sabitle (örn: herschclient.jar) daha sağlam olur
    private static final String ASSET_SUFFIX = ".jar";

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    private UpdateManager() {}

    public static void checkAndDownloadAsync() {
        // Client thread’i bloklamayalım
        Thread t = new Thread(UpdateManager::checkAndDownload, "herschclient-update");
        t.setDaemon(true);
        t.start();
    }

    private static void checkAndDownload() {
        try {
            JsonObject latest = fetchLatestRelease();
            if (latest == null) return;

            String tag = latest.has("tag_name") ? latest.get("tag_name").getAsString() : null;
            if (tag == null || tag.isBlank()) return;

            // Basit karşılaştırma: tag != current
            // (Daha iyi: semver compare. Şimdilik kolay yol.)
            if (normalize(tag).equals(normalize(HerschClient.VERSION))) return;

            String url = pickJarAssetUrl(latest);
            if (url == null) return;

            Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
            Files.createDirectories(modsDir);

            Path target = modsDir.resolve("herschclient-update.jar");
            download(url, target);

            // Flag dosyası: restart sonrası “apply” edebilmek için
            Path flag = FabricLoader.getInstance().getConfigDir().resolve("herschclient.update.flag");
            Files.writeString(flag, tag);

            notifyInGame("Yeni sürüm indirildi (" + tag + "). Uygulamak için oyunu yeniden başlat.");
        } catch (Exception ignored) {
            // Sessiz geç (istersen debug log açarız)
        }
    }

    private static JsonObject fetchLatestRelease() throws Exception {
        String api = "https://api.github.com/repos/" + OWNER + "/" + REPO + "/releases/latest";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(api))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "HerschClient/" + HerschClient.VERSION)
                .GET()
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) return null;

        return JsonParser.parseString(res.body()).getAsJsonObject();
    }

    private static String pickJarAssetUrl(JsonObject latest) {
        if (!latest.has("assets")) return null;
        JsonArray assets = latest.getAsJsonArray("assets");

        for (int i = 0; i < assets.size(); i++) {
            JsonObject a = assets.get(i).getAsJsonObject();
            String name = a.has("name") ? a.get("name").getAsString() : "";
            if (name.endsWith(ASSET_SUFFIX) && a.has("browser_download_url")) {
                return a.get("browser_download_url").getAsString();
            }
        }
        return null;
    }

    private static void download(String url, Path target) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "HerschClient/" + HerschClient.VERSION)
                .GET()
                .build();

        HttpResponse<InputStream> res = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (res.statusCode() != 200) return;

        try (InputStream in = res.body()) {
            Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void notifyInGame(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("§b[HerschClient] §f" + msg), false);
            }
        });
    }

    private static String normalize(String v) {
        // v0.1.0 -> 0.1.0
        return v == null ? "" : v.trim().toLowerCase().replace("v", "");
    }
}
