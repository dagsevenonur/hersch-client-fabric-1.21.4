package com.herschclient.core.settings;

public abstract class Setting<T> {
    private final String key;
    private final String displayName;
    protected T value;

    protected Setting(String key, String displayName, T defaultValue) {
        this.key = key;
        this.displayName = displayName;
        this.value = defaultValue;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public T get() { return value; }
    public void set(T v) { this.value = v; }
}
