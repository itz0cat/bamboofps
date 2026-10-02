package dev.itz0cat.bamboofps.mixin;

import dev.itz0cat.bamboofps.config.ConfigManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.command.ModelCommandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderManager.class)
public class BlockEntityRenderManagerMixin {

    @Inject(method = "getRenderState", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void bamboofps$skipBlockEntityRenderState(E blockEntity, float tickProgress, ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay, CallbackInfoReturnable<?> cir) {
        if (ConfigManager.shouldHideBlockEntity(blockEntity)) {
            cir.setReturnValue(null);
        }
    }
}
