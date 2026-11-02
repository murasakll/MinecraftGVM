package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.client.EraVisuals;
import net.minecraft.world.level.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public abstract class HistoricalLeafSoundsMixin {
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void campaign$quietLeaves(CallbackInfo ci) { if (EraVisuals.legacyLeaves()) ci.cancel(); }
}
