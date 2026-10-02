package dev.itz0cat.bamboofps.mixin;

import dev.itz0cat.bamboofps.config.ConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "processWorldEvent", at = @At("HEAD"), cancellable = true)
    private void bamboofps$cancelWorldEvents(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        // Cancel event 2001 (BLOCK_BROKEN) only when the block state is in the hidden blocks list
        if (eventId == WorldEvents.BLOCK_BROKEN && data > 0) {
            try {
                BlockState state = Block.getStateFromRawId(data);
                if (state != null && ConfigManager.shouldCancelParticles(state)) {
                    ci.cancel();
                }
            } catch (Exception ignored) {
            }
            return;
        }

        if (ConfigManager.shouldCancelWorldEvent(eventId)) {
            ci.cancel();
        }
    }
}
