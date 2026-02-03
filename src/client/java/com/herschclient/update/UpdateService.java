package com.herschclient.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herschclient.HerschClient;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public final class UpdateService {

    // İstersen bunu config’e taşı
    public static final String MANIFEST_URL = "https://example.com/herschclient/update.json";

    private static volatile UpdateInfo latest;
    private static volatile boolean checked;

    public static boolean isChecked() { return checked; }
    public static UpdateInfo latest() { return latest; }

    public static void checkAsync() {
        if (checked) return;

        Thread t = new Thread(UpdateService::checkNow, "herschclient-update-check");
        t.setDaemon(true);
        t.start();
    }

    private static void checkNow() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(4))
                    .build();

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(MANIFEST_URL))
                    .timeout(Duration.ofSeconds(6))
                    .header("User-Agent", "HerschClient/" + HerschClient.VERSION)
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                checked = true;
                return;
            }

            JsonObject o = JsonParser.parseString(res.body()).getAsJsonObject();
            UpdateInfo info = new UpdateInfo(
                    o.get("version").getAsString(),
                    o.get("mc").getAsString(),
                    o.get("channel").getAsString(),
                    o.get("url").getAsString(),
                    o.get("sha256").getAsString(),
                    o.has("notes") ? o.get("notes").getAsString() : ""
            );

            // MC sürümü uyuşmuyorsa ignore edebilirsin
            if (!"1.21.4".equals(info.mc())) {
                checked = true;
                return;
            }

            latest = info;
            checked = true;

        } catch (Exception ignored) {
            checked = true;
        }
    }

    // Çok basit semver compare: 0.1.10 > 0.1.2
    public static boolean isNewer(String remote, String local) {
        int[] r = parse(remote);
        int[] l = parse(local);
        for (int i = 0; i < 3; i++) {
            if (r[i] != l[i]) return r[i] > l[i];
        }
        return false;
    }

    private static int[] parse(String v) {
        String[] p = v.split("\\.");
        int[] out = new int[]{0,0,0};
        for (int i = 0; i < Math.min(3, p.length); i++) {
            try { out[i] = Integer.parseInt(p[i]); } catch (Exception ignored) {}
        }
        return out;
    }
}
