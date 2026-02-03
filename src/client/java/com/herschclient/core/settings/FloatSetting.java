package com.herschclient.core.settings;

public final class FloatSetting extends Setting<Float> {
    private final float min;
    private final float max;

    public FloatSetting(String key, String displayName, float def, float min, float max) {
        super(key, displayName, def);
        this.min = min;
        this.max = max;
    }

    public float min() { return min; }
    public float max() { return max; }

    public void setClamped(float v) {
        if (v < min) v = min;
        if (v > max) v = max;
        set(v);
    }
}
