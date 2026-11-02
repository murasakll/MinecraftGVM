package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FurnaceFuelSlot.class)
public abstract class InfdevFurnaceFuelSlotMixin {
    @Shadow @Final private AbstractFurnaceMenu menu;
    @Inject(method="mayPlace",at=@At("HEAD"),cancellable=true)
    private void campaign$rejectModernFuel(ItemStack stack,CallbackInfoReturnable<Boolean> cir){
        if(((HistoricalFurnaceMenu)menu).campaign$historicalFurnace())
            cir.setReturnValue(InfdevFurnaceRules.fuelTicks(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),((HistoricalFurnaceMenu)menu).campaign$furnaceEra())>0);
    }
}
