package com.herschclient.mixin.client;

import com.herschclient.HerschClient;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void hersch$xray$getRenderType(CallbackInfoReturnable<BlockRenderType> cir) {
        if (net.minecraft.client.MinecraftClient.getInstance() == null) return;
        if (HerschClient.XRAY == null || !HerschClient.XRAY.isEnabled()) return;

        AbstractBlock.AbstractBlockState self = (AbstractBlock.AbstractBlockState) (Object) this;

        // whitelist dışı her şey invisible
        if (!HerschClient.XRAY.isWhitelisted(self.getBlock())) {
            cir.setReturnValue(BlockRenderType.INVISIBLE);
        }
    }

    // KRİTİK: whitelist dışı bloklar artık cull/occlude etmesin
    @Inject(method = "getCullingShape", at = @At("HEAD"), cancellable = true)
    private void hersch$xray$getCullingShape(CallbackInfoReturnable<VoxelShape> cir) {
        if (net.minecraft.client.MinecraftClient.getInstance() == null) return;
        if (HerschClient.XRAY == null || !HerschClient.XRAY.isEnabled()) return;

        AbstractBlock.AbstractBlockState self = (AbstractBlock.AbstractBlockState) (Object) this;

        if (!HerschClient.XRAY.isWhitelisted(self.getBlock())) {
            cir.setReturnValue(VoxelShapes.empty());
        }
    }

    @Inject(method = "getOcclusionShape", at = @At("HEAD"), cancellable = true)
    private void hersch$xray$getOcclusionShape(CallbackInfoReturnable<VoxelShape> cir) {
        if (net.minecraft.client.MinecraftClient.getInstance() == null) return;
        if (HerschClient.XRAY == null || !HerschClient.XRAY.isEnabled()) return;

        AbstractBlock.AbstractBlockState self = (AbstractBlock.AbstractBlockState) (Object) this;

        if (!HerschClient.XRAY.isWhitelisted(self.getBlock())) {
            cir.setReturnValue(VoxelShapes.empty());
        }
    }
}
