package br.com.mura.betaminecraftmod.client.visual;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModelBlockRenderer.class)
public abstract class FlatLightingMixin {
    @org.spongepowered.asm.mixin.Shadow @org.spongepowered.asm.mixin.Final private boolean ambientOcclusion;
    @Redirect(method = "tesselateBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;ambientOcclusion:Z"))
    private boolean campaign$flat(ModelBlockRenderer renderer) {
        if(br.com.mura.betaminecraftmod.client.ShaderCompatibility.overridesLighting())return ambientOcclusion;
        return br.com.mura.betaminecraftmod.client.HistoricalLighting.ambientOcclusion(br.com.mura.betaminecraftmod.progression.CampaignRules.clientEra())&&ambientOcclusion;
    }
}
