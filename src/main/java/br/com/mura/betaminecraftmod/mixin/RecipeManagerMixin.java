package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.InfdevContent;
import java.util.Optional;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private void campaign$historicalCrafting(RecipeType type, RecipeInput input, Level level, CallbackInfoReturnable<Optional<RecipeHolder>> cir) {
        if(type==RecipeType.CRAFTING&&input instanceof CraftingInput crafting&&br.com.mura.betaminecraftmod.progression.HistoricalContent.active(level))
            cir.setReturnValue((Optional)br.com.mura.betaminecraftmod.progression.HistoricalCrafting.find(br.com.mura.betaminecraftmod.progression.CampaignRules.era(level),crafting,level));
    }
    @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private void campaign$historicalCraftingHint(RecipeType type, RecipeInput input, Level level, RecipeHolder hint, CallbackInfoReturnable<Optional<RecipeHolder>> cir) {
        campaign$historicalCrafting(type,input,level,cir);
    }
    @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void campaign$filter(RecipeType type, RecipeInput input, Level level, CallbackInfoReturnable<Optional<RecipeHolder>> cir) {
        if (InfdevContent.legacy(level) && cir.getReturnValue().isPresent() && !InfdevContent.recipeAllowed(cir.getReturnValue().get().value(), input, br.com.mura.betaminecraftmod.progression.CampaignProgress.get((net.minecraft.server.level.ServerLevel)level).era())) cir.setReturnValue(Optional.empty());
    }
    @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void campaign$filterHint(RecipeType type, RecipeInput input, Level level, RecipeHolder hint, CallbackInfoReturnable<Optional<RecipeHolder>> cir) {
        campaign$filter(type, input, level, cir);
    }
}
