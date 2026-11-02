package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets="net.minecraft.world.inventory.BrewingStandMenu$FuelSlot")
public abstract class HistoricalBrewingFuelSlotMixin extends Slot {
    protected HistoricalBrewingFuelSlotMixin(Container container,int slot,int x,int y){super(container,slot,x,y);}
    private boolean campaign$historical(){return container instanceof BlockEntity be?ReleaseBrewing.active(be.getLevel()):ReleaseBrewing.clientActive();}
    @Inject(method="mayPlace",at=@At("HEAD"),cancellable=true)
    private void campaign$reject(ItemStack stack,CallbackInfoReturnable<Boolean> result){if(campaign$historical())result.setReturnValue(false);}
    @Override public boolean isActive(){return !campaign$historical()&&super.isActive();}
}
