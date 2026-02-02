package com.herschclient.mixin.client;

import com.herschclient.ui.ModSettingsScreen;
import net.minecraft.client.MinecraftClient;
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
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("MOD AYARLARI"),
                btn -> MinecraftClient.getInstance().setScreen(new ModSettingsScreen(this))
        ).dimensions(this.width / 2 - 100, this.height / 4 + 120, 200, 20).build());
    }
}

