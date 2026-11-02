package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class InfdevFurnaceMixin {
    @Shadow protected NonNullList<ItemStack> items;
    @Shadow private int litTimeRemaining;
    @Shadow private int litTotalTime;
    @Shadow private int cookingTimer;
    @Shadow private int cookingTotalTime;
    @Unique private static boolean campaign$opening(AbstractFurnaceBlockEntity furnace){
        return CampaignRules.historicalFurnace(furnace.getLevel())&&furnace.getBlockState().is(Blocks.FURNACE);
    }
    @Unique private static String campaign$id(ItemStack stack){return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();}

    @Inject(method="serverTick",at=@At("HEAD"),cancellable=true)
    private static void campaign$historicalTick(ServerLevel level,BlockPos pos,BlockState state,AbstractFurnaceBlockEntity furnace,CallbackInfo ci){
        if(!campaign$opening(furnace))return;
        ((InfdevFurnaceMixin)(Object)furnace).campaign$cook(level,pos,state,furnace);
        ci.cancel();
    }
    @Unique private void campaign$cook(ServerLevel level,BlockPos pos,BlockState state,AbstractFurnaceBlockEntity furnace){
        boolean wasLit=litTimeRemaining>0;
        int oldTimer=cookingTimer;
        if(litTimeRemaining>0)litTimeRemaining--;
        cookingTotalTime=InfdevFurnaceRules.COOK_TICKS;
        ItemStack input=items.get(0),output=items.get(2),fuel=items.get(1);
        String resultName=input.isEmpty()?null:InfdevFurnaceRules.result(campaign$id(input),CampaignRules.era(level));
        ItemStack result=resultName==null?ItemStack.EMPTY:new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(resultName)));
        HistoricalItemLimits.apply(result,CampaignRules.era(level));
        HistoricalItemLimits.apply(output,CampaignRules.era(level));
        boolean canCook=!result.isEmpty()&&(output.isEmpty()||ItemStack.isSameItemSameComponents(output,result)
            &&output.getCount()<Math.min(64,output.getMaxStackSize()));
        boolean inventoryChanged=false;
        if(litTimeRemaining==0&&canCook){
            litTotalTime=litTimeRemaining=fuel.isEmpty()?0:InfdevFurnaceRules.fuelTicks(campaign$id(fuel),CampaignRules.era(level));
            if(litTimeRemaining>0){fuel.shrink(1);inventoryChanged=true;}
        }
        if(litTimeRemaining>0&&canCook){
            if(++cookingTimer==InfdevFurnaceRules.COOK_TICKS){
                cookingTimer=0;input.shrink(1);
                if(output.isEmpty())items.set(2,result);else output.grow(1);
                inventoryChanged=true;
            }
        }else cookingTimer=0;
        if(wasLit!=(litTimeRemaining>0))level.setBlock(pos,state.setValue(BlockStateProperties.LIT,litTimeRemaining>0),3);
        if(inventoryChanged||wasLit!=(litTimeRemaining>0)||oldTimer!=cookingTimer)furnace.setChanged();
    }
    @Inject(method="canPlaceItem",at=@At("HEAD"),cancellable=true)
    private void campaign$fuelSlot(int slot,ItemStack stack,CallbackInfoReturnable<Boolean> cir){
        if(slot==1&&campaign$opening((AbstractFurnaceBlockEntity)(Object)this))cir.setReturnValue(InfdevFurnaceRules.fuelTicks(campaign$id(stack),CampaignRules.era(((AbstractFurnaceBlockEntity)(Object)this).getLevel()))>0);
    }
    @Inject(method="getRecipesToAwardAndPopExperience",at=@At("HEAD"),cancellable=true)
    private void campaign$noExperience(ServerLevel level,net.minecraft.world.phys.Vec3 position,
            CallbackInfoReturnable<java.util.List<net.minecraft.world.item.crafting.RecipeHolder<?>>> cir){
        if(campaign$opening((AbstractFurnaceBlockEntity)(Object)this))cir.setReturnValue(java.util.List.of());
    }
}
