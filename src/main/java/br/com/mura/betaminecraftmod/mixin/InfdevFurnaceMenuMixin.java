package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceMenu.class)
public abstract class InfdevFurnaceMenuMixin implements HistoricalFurnaceMenu {
    @Shadow @Final protected Level level;
    @Override public int campaign$furnaceEra(){return CampaignRules.era(level);}
    @Override public boolean campaign$historicalFurnace(){return (Object)this instanceof FurnaceMenu&&CampaignRules.historicalFurnace(level);}
    @Inject(method="canSmelt",at=@At("HEAD"),cancellable=true)
    private void campaign$input(ItemStack stack,CallbackInfoReturnable<Boolean> cir){
        if(campaign$historicalFurnace())cir.setReturnValue(InfdevFurnaceRules.result(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),CampaignRules.era(level))!=null);
    }
    @Inject(method="isFuel",at=@At("HEAD"),cancellable=true)
    private void campaign$fuel(ItemStack stack,CallbackInfoReturnable<Boolean> cir){
        if(campaign$historicalFurnace())cir.setReturnValue(InfdevFurnaceRules.fuelTicks(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),CampaignRules.era(level))>0);
    }
}
