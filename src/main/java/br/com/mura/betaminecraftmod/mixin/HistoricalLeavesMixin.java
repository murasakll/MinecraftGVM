package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.client.EraVisuals;
import net.minecraft.world.level.block.FallingParticlesLeavesBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingParticlesLeavesBlock.class)
public abstract class HistoricalLeavesMixin {
    @Inject(method = "makeFallingLeavesParticles", at = @At("HEAD"), cancellable = true)
    private void campaign$noParticles(CallbackInfo ci) { if (EraVisuals.legacyLeaves()) ci.cancel(); }
}
