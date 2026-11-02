package br.com.mura.betaminecraftmod.client.visual;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.MoonPhase;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import br.com.mura.betaminecraftmod.client.EraVisuals;

@Mixin(SkyRenderer.class)
public abstract class HistoricalSkyMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void campaign$sky(ClientLevel level, float partial, Camera camera, SkyRenderState state, CallbackInfo ci) {
        var profile = EraVisuals.active();
        if (br.com.mura.betaminecraftmod.client.ShaderCompatibility.overridesLighting() || !profile.historical()) return;
        long time = level.getOverworldClockTime();
        
        state.skyColor = EraVisuals.sky(time, partial);
        state.sunAngle = state.moonAngle = state.starAngle = profile.sunAngle(time, partial) * (float)(Math.PI * 2);
        
        float stars = Math.clamp(1 - ((float)Math.cos(state.sunAngle) * 2 + .75f), 0, 1);
        state.starBrightness = stars * stars * .5f;
        
        // Zero out sunrise/sunset gradients so they don't paint a dark band
        state.sunriseAndSunsetColor = new Vector4f(0, 0, 0, 0);
        
        state.moonPhase = MoonPhase.FULL_MOON;
        state.rainBrightness = 1;
        
        // Force the dark disc toggle off if available, 
        // or ensure safety with the zeroed sunrise color vector
        state.shouldRenderDarkDisc = false;
    }
}