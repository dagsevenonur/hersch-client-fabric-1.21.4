package com.herschclient.core.settings;

import java.util.Arrays;
import java.util.List;

public final class ModeSetting extends Setting<String> {
    private final List<String> modes;
    private int index;

    public ModeSetting(String key, String displayName, String defaultMode, String... modes) {
        super(key, displayName, defaultMode);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(defaultMode);
    }

    public void cycle() {
        index++;
        if (index >= modes.size()) index = 0;
        set(modes.get(index));
    }

    public List<String> getModes() {
        return modes;
    }
}
