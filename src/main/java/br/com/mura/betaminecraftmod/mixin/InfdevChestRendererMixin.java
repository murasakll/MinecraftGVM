package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.client.EraVisuals;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestRenderer.class)
public abstract class InfdevChestRendererMixin {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void campaign$staticBlock(ChestRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (br.com.mura.betaminecraftmod.progression.CampaignRules.cubeChest(net.minecraft.client.Minecraft.getInstance().level) && (state.material == ChestRenderState.ChestMaterialType.REGULAR
                || state.material == ChestRenderState.ChestMaterialType.CHRISTMAS)) ci.cancel();
    }
}
