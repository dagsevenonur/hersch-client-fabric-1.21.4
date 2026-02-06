package com.herschclient.mixin.client;

import com.herschclient.HerschClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockShouldDrawSideMixin {

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void hersch$xray$shouldDrawSide(BlockState state,
                                                  BlockState neighborState,
                                                  Direction direction,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (net.minecraft.client.MinecraftClient.getInstance() == null) return;
        if (HerschClient.XRAY == null || !HerschClient.XRAY.isEnabled()) return;

        // Whitelist (maden vs): her yüz çizilsin
        if (HerschClient.XRAY.isWhitelisted(state.getBlock())) {
            cir.setReturnValue(true);
            return;
        }

        // İsteğe bağlı: Whitelist dışı blokları invisible yapıyorsan,
        // neighbor görünmezse de yüzü çizdirmek isteyebilirsin (genelde gerek kalmıyor)
        // if (!HerschClient.XRAY.isWhitelisted(neighborState.getBlock())) {
        //     cir.setReturnValue(true);
        // }
    }
}
