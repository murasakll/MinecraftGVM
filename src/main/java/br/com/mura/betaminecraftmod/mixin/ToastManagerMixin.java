package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public abstract class ToastManagerMixin {
    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void campaign$noModernToasts(Toast toast, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player != null && CampaignRules.legacyFood(player) && (toast instanceof AdvancementToast || toast instanceof RecipeToast || toast instanceof TutorialToast)) ci.cancel();
    }
}
