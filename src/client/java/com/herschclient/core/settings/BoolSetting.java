package com.herschclient.core.settings;

public final class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String key, String displayName, boolean def) {
        super(key, displayName, def);
    }

    public void toggle() { set(!get()); }
}
