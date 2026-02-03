package com.herschclient.update;

public record UpdateInfo(
        String version,
        String mc,
        String channel,
        String url,
        String sha256,
        String notes
) {}
