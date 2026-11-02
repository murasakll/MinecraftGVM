package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookMixin {
    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void campaign$hidden(CallbackInfoReturnable<Boolean> cir) {
        var player = Minecraft.getInstance().player;
        if (player != null && CampaignRules.legacyFood(player)) cir.setReturnValue(false);
    }
    @Inject(method = "toggleVisibility", at = @At("HEAD"), cancellable = true)
    private void campaign$noRecipeBook(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player != null && CampaignRules.legacyFood(player)) ci.cancel();
    }
}
