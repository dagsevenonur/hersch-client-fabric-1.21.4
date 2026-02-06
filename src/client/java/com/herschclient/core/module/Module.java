package com.herschclient.core.module;

public abstract class Module {
    private final String name;
    private final ModuleCategory category;
    private boolean enabled;
    
    // Add settings list support
    protected final java.util.List<com.herschclient.core.settings.Setting<?>> settings = new java.util.ArrayList<>();

    protected Module(String name, ModuleCategory category) {
        this.name = name;
        this.category = category;
        this.enabled = false;
    }

    public final String getName() { return name; }
    public final ModuleCategory getCategory() { return category; }
    public final boolean isEnabled() { return enabled; }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    protected void onEnable() {}
    protected void onDisable() {}
    
    public java.util.List<com.herschclient.core.settings.Setting<?>> getSettings() {
        return java.util.Collections.unmodifiableList(settings);
    }
}
