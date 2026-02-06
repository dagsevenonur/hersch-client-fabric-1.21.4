package com.herschclient.mixin.client;

import com.herschclient.HerschClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (HerschClient.ZOOM != null) {
            float originalFov = cir.getReturnValue();
            // Pass tickDelta for smooth interpolation
            double multiplier = HerschClient.ZOOM.getFovMultiplier(tickDelta);
            
            // if (multiplier != 1.0) System.out.println("Applying FOV mult: " + multiplier);

            if (multiplier != 1.0) {
                cir.setReturnValue((float) (originalFov * multiplier));
            }
        }
    }
}
