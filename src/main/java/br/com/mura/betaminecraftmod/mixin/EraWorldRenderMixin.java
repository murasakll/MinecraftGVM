package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.client.EraTextures;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class EraWorldRenderMixin {
    @Inject(method="renderLevel",at=@At("HEAD"),cancellable=true)
    private void campaign$waitForResources(CallbackInfo ci) {
        if (EraTextures.blockWorldRendering()) ci.cancel();
    }
}
