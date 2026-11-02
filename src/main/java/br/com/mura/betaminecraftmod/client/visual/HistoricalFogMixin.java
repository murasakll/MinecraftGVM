package br.com.mura.betaminecraftmod.client.visual;

import net.minecraft.client.Camera;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import br.com.mura.betaminecraftmod.client.EraVisuals;

@Mixin(FogRenderer.class)
public abstract class HistoricalFogMixin {
    // Before the final hook/RETURN: Sodium caches FogData at RETURN.
    @Inject(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;onSetupFog(Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;Lnet/minecraft/world/level/material/FogType;Lnet/minecraft/client/Camera;FFLnet/minecraft/client/renderer/fog/FogData;)V"))
    private void campaign$fog(Camera camera, int chunks, DeltaTracker delta, float darken, ClientLevel level, CallbackInfoReturnable<FogData> cir, @Local FogData fog) {
        var profile = EraVisuals.active();
        if (!profile.historical()) return;
        float partial = delta.getGameTimeDeltaPartialTick(false);
        float distance = profile.viewDistance(chunks);
        // Finish fog before the nearest side of the unlocked square, in every viewing direction.
        var border = level.getWorldBorder();
        var eye = camera.position();
        distance = br.com.mura.betaminecraftmod.client.BoundaryFog.end(distance,eye.x,eye.z,
                border.getMinX(),border.getMaxX(),border.getMinZ(),border.getMaxZ());
        if (br.com.mura.betaminecraftmod.client.ShaderCompatibility.overridesLighting()) {
            // Preserve pack lighting/color. Packs that consume vanilla fog bounds can honor the edge limit.
            fog.renderDistanceEnd = Math.min(fog.renderDistanceEnd,distance);
            fog.renderDistanceStart = Math.min(fog.renderDistanceStart,distance * profile.fogStartFraction());
            fog.skyEnd = Math.min(fog.skyEnd,distance);
            fog.cloudEnd = Math.min(fog.cloudEnd,distance);
            return;
        }
        float day = profile.daylight(level.getOverworldClockTime(), partial);
        Vector3f color = EraVisuals.rgb(profile.fogColor()).mul(day * .94f + .06f, day * .94f + .06f, day * .91f + .09f);
        float option = (float)(Math.log(256 / distance) / Math.log(2));
        float mix = 1 - (float)Math.pow(1 / (4 - Math.clamp(option, 0, 3)), .25);
        color.lerp(EraVisuals.sky(level.getOverworldClockTime(), partial), mix);
        fog.environmentalStart = Float.MAX_VALUE;
        fog.environmentalEnd = Float.MAX_VALUE;
        fog.renderDistanceStart = distance * profile.fogStartFraction();
        fog.renderDistanceEnd = fog.skyEnd = fog.cloudEnd = distance;
        FogType type = camera.getFluidInCamera();
        if (type == FogType.WATER || type == FogType.LAVA) {
            color.set(type == FogType.WATER ? .02f : .6f, type == FogType.WATER ? .02f : .1f, type == FogType.WATER ? .2f : 0);
            // Negative start identifies historical exponential fog in this era's fog.glsl.
            fog.environmentalStart = -(type == FogType.WATER ? profile.waterDensity() : profile.lavaDensity());
        }
        fog.color.set(color.mul(EraVisuals.fogBrightness(partial)), 1);
    }
}
