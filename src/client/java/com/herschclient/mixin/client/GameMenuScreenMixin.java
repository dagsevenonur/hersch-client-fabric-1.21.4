package com.herschclient.mixin.client;

import com.herschclient.ui.ModSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void herschclient$addButton(CallbackInfo ci) {
        int w = 204;
        int h = 20;
        int x = this.width / 2 - w / 2;

        // Senin mevcut yerin aynı kalsın:
        int y = this.height / 4 + 120 + 8;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("MOD AYARLARI"),
                btn -> MinecraftClient.getInstance().setScreen(new ModSettingsScreen(this))
        ).dimensions(x, y, w, h).build());
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void herschclient$noBlurBackground(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Blur'u tamamen iptal et
        ci.cancel();

        // İstersen hafif karartma overlay'i bırak (blur yok)
        ctx.fill(0, 0, this.width, this.height, 0xAA000000);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void herschclient$disableBlur(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.gameRenderer != null) {
            mc.gameRenderer.clearPostProcessor();
        }
    }
}
