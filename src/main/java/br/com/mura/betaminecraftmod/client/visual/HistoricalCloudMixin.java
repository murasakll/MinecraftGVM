package br.com.mura.betaminecraftmod.client.visual;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import br.com.mura.betaminecraftmod.client.EraVisuals;

@Mixin(LevelExtractor.class)
public abstract class HistoricalCloudMixin {
    @Shadow private ClientLevel level;
    @Shadow @Final private LevelRenderState levelRenderState;
    @Inject(method = "extract", at = @At("TAIL"))
    private void campaign$clouds(DeltaTracker delta, Camera camera, float partial, CallbackInfo ci) {
        var profile = EraVisuals.active();
        if (br.com.mura.betaminecraftmod.client.ShaderCompatibility.overridesLighting() || !profile.historical() || level == null) return;
        float day = profile.daylight(level.getOverworldClockTime(), partial);
        int rg = (int)((day * .9f + .1f) * 255);
        int b = (int)((day * .85f + .15f) * 255);
        levelRenderState.cloudColor = 0xCC000000 | rg << 16 | rg << 8 | b;
        levelRenderState.cloudHeight = 108;
    }
}
