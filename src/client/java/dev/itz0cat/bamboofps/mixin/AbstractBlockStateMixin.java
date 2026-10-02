package dev.itz0cat.bamboofps.mixin;

import dev.itz0cat.bamboofps.config.ConfigManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void bamboofps$getRenderType(CallbackInfoReturnable<BlockRenderType> cir) {
        if (ConfigManager.shouldHideBlock((BlockState) (Object) this)) {
            cir.setReturnValue(BlockRenderType.INVISIBLE);
        }
    }
}
