package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.ReleaseEnchanting;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets="net.minecraft.world.inventory.EnchantmentMenu$3")
public abstract class HistoricalEnchantingLapisMixin extends Slot {
    protected HistoricalEnchantingLapisMixin(Container container,int slot,int x,int y){super(container,slot,x,y);}
    @Override public boolean isActive(){return !ReleaseEnchanting.historical(container)&&super.isActive();}
    @Inject(method="mayPlace",at=@At("HEAD"),cancellable=true)
    private void campaign$noLapis(ItemStack stack,CallbackInfoReturnable<Boolean> result){if(ReleaseEnchanting.historical(container))result.setReturnValue(false);}
}
