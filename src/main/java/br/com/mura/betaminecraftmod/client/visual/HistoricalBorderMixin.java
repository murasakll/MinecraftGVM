package br.com.mura.betaminecraftmod.client.visual;

import br.com.mura.betaminecraftmod.client.EraVisuals;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.client.renderer.state.level.WorldBorderRenderState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorderRenderer.class)
public abstract class HistoricalBorderMixin {
    @Inject(method = "extract", at = @At("TAIL"))
    private void campaign$hide(WorldBorder border, float partial, Vec3 camera, double distance, WorldBorderRenderState state, CallbackInfo ci) {
        if (EraVisuals.active().historical()) state.alpha = 0;
    }
}
