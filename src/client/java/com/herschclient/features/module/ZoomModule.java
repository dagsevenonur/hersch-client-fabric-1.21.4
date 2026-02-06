package com.herschclient.features.module;

import com.herschclient.core.module.Module;
import com.herschclient.core.module.ModuleCategory;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import net.minecraft.util.math.MathHelper;

public final class ZoomModule extends Module {

    public static KeyBinding ZOOM_KEY;
    
    // Smooth zoom state
    private double currentLevel = 1.0;
    private double prevLevel = 1.0;
    
    private final double targetZoomFovMultiplier = 0.23; // Target FOV multiplier
    private final double noZoomMultiplier = 1.0;

    public ZoomModule() {
        super("Zoom", ModuleCategory.VISUAL);
        
        ZOOM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.herschclient.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.herschclient"
        ));
        setEnabled(true); // Enable by default so keybinding works immediately
    }

    public void onTick(MinecraftClient mc) {
        prevLevel = currentLevel;
        
        boolean key = ZOOM_KEY.isPressed();
        boolean enabled = isEnabled();
        // System.out.println("Zoom Tick: Enabled=" + enabled + " Key=" + key + " Current=" + currentLevel);

        // Target is determined by: Module Enabled AND Key Pressed
        double target = (enabled && key) ? targetZoomFovMultiplier : noZoomMultiplier;
        
        // Smooth asymptotic approach
        // 0.2 factor gives a nice weighty smooth feel (5 ticks to ~60% diff, etc)
        // Adjust this value for "modern" feel (usually 0.1 to 0.5)
        currentLevel += (target - currentLevel) * 0.5;
        
        // Snap to target if very close to avoid micro-values
        if (Math.abs(target - currentLevel) < 0.001) {
            currentLevel = target;
        }
    }
    
    // Helper to get the interpolated modifier
    // Called by GameRendererMixin each frame
    public double getFovMultiplier(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevLevel, currentLevel);
    }
}
