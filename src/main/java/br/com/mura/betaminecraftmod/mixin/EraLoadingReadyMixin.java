package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.client.EraTextures;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public abstract class EraLoadingReadyMixin {
    @Inject(method="onClose",at=@At("HEAD"),cancellable=true)
    private void campaign$awaitResources(CallbackInfo ci) {
        if (EraTextures.blockWorldRendering()) ci.cancel();
    }
}
