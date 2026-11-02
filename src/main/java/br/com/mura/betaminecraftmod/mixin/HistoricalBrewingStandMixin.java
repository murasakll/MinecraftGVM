package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import br.com.mura.betaminecraftmod.progression.ReleaseBrewing;
import br.com.mura.betaminecraftmod.progression.ReleaseBrewingCatalog;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fuel-free campaign adapter; the verified 1.0 reference additionally uses its original chemistry/countdown. */
@Mixin(BrewingStandBlockEntity.class)
public abstract class HistoricalBrewingStandMixin {
    @Shadow private int fuel;
    @Shadow private int totalFuel;
    @Shadow private float speedMultiplier;
    @Shadow private int brewTime;
    @Shadow private int totalBrewTime;
    @Shadow private net.minecraft.core.NonNullList<ItemStack> items;
    @WrapMethod(method="serverTick")
    private static void campaign$withoutFuel(ServerLevel level,BlockPos pos,BlockState state,BrewingStandBlockEntity entity,Operation<Void> original) {
        if(CampaignRules.era(level)!=6){original.call(level,pos,state,entity);return;}
        var self=(HistoricalBrewingStandMixin)(Object)entity;
        int fuel=self.fuel,total=self.totalFuel;float speed=self.speedMultiplier;
        try {
            self.fuel=1;self.totalFuel=1;self.speedMultiplier=1;
            int before=self.brewTime;original.call(level,pos,state,entity);
            if(ReleaseBrewing.active(level)&&before<=0&&self.brewTime==400){self.brewTime=ReleaseBrewingCatalog.BREW_TICKS;self.totalBrewTime=self.brewTime;}
        }finally {self.fuel=fuel;self.totalFuel=total;self.speedMultiplier=speed;}
    }
    @Inject(method="canPlaceItem",at=@At("HEAD"),cancellable=true)
    private void campaign$noFuelSlot(int slot,ItemStack stack,CallbackInfoReturnable<Boolean> cir) {
        var entity=(BrewingStandBlockEntity)(Object)this;
        if(slot==4&&CampaignRules.era(entity.getLevel())==6){cir.setReturnValue(false);return;}
        if(ReleaseBrewing.active(entity.getLevel()))cir.setReturnValue(slot==3?ReleaseBrewing.ingredient(stack):slot>=0&&slot<3&&entity.getItem(slot).isEmpty()&&(stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE)||ReleaseBrewing.metadata(stack)>=0));
    }
    @Inject(method="isBrewable",at=@At("HEAD"),cancellable=true)
    private static void campaign$chemistry(ServerLevel level,BrewingStandBlockEntity stand,CallbackInfoReturnable<Boolean> result){if(ReleaseBrewing.active(level))result.setReturnValue(ReleaseBrewing.canBrew(stand));}
    @Inject(method="doBrew",at=@At("HEAD"),cancellable=true)
    private static void campaign$brew(ServerLevel level,BlockPos pos,BrewingStandBlockEntity stand,CallbackInfo ci){
        if(!ReleaseBrewing.active(level))return;ci.cancel();if(!ReleaseBrewing.canBrew(stand))return;
        var items=((HistoricalBrewingStandMixin)(Object)stand).items;
        if(net.neoforged.neoforge.event.EventHooks.onPotionAttemptBrew(items))return;
        for(int slot=0;slot<3;slot++){var input=items.get(slot);int before=ReleaseBrewing.metadata(input),after=ReleaseBrewing.result(input,items.get(3));if(before>=0&&before!=after)items.set(slot,ReleaseBrewing.create(input,after));}
        net.neoforged.neoforge.event.EventHooks.onPotionBrewed(items);items.get(3).shrink(1);
        level.levelEvent(1035,pos,0); // Preserve the existing audio path while its historical audit is on hold.
    }
}
