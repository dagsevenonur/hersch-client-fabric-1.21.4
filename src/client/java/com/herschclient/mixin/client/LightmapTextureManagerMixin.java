package com.herschclient.mixin.client;

import com.herschclient.HerschClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    @Redirect(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"
        )
    )
    private Object onGetGamma(SimpleOption<Double> option) {
        if (option == MinecraftClient.getInstance().options.getGamma()) {
            if (HerschClient.FULLBRIGHT.isEnabled()) {
                return 1000.0;
            }
        }
        return option.getValue();
    }
}
