package com.herschclient.update;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;

public final class UpdateDownloader {

    public static Path modsDir() {
        return FabricLoader.getInstance().getGameDir().resolve("mods");
    }

    public static void downloadToMods(UpdateInfo info) throws Exception {
        Files.createDirectories(modsDir());

        String fileName = "herschclient-" + info.version() + ".jar";
        Path tmp = modsDir().resolve(fileName + ".download");
        Path out = modsDir().resolve(fileName);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(info.url()))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<InputStream> res = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (res.statusCode() != 200) throw new IOException("HTTP " + res.statusCode());

        // download
        try (InputStream in = res.body();
             OutputStream os = Files.newOutputStream(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            in.transferTo(os);
        }

        // sha256 verify
        String sha = sha256(tmp);
        if (!sha.equalsIgnoreCase(info.sha256())) {
            Files.deleteIfExists(tmp);
            throw new SecurityException("SHA256 mismatch");
        }

        // move to final
        Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private static String sha256(Path p) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(p)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
        }
        byte[] dig = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : dig) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
