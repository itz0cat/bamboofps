package dev.itz0cat.bamboofps.mixin;

import dev.itz0cat.bamboofps.config.ConfigManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
    private void bamboofps$cancelBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (ConfigManager.shouldCancelParticles(state)) {
            ci.cancel();
        }
    }

    @Inject(method = "addBlockBreakingParticles", at = @At("HEAD"), cancellable = true)
    private void bamboofps$cancelBlockBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.world != null) {
            BlockState state = client.world.getBlockState(pos);
            if (ConfigManager.shouldCancelParticles(state)) {
                ci.cancel();
            }
        }
    }
}
