package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(BucketItem.class)
public abstract class HistoricalBucketMixin {
    @Redirect(method={"emptyContents(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z","canBlockContainFluid"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/LiquidBlockContainer;canPlaceLiquid(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z"))
    private boolean campaign$noContainerWaterlogging(LiquidBlockContainer container,LivingEntity user,BlockGetter level,BlockPos pos,BlockState state,Fluid fluid){return !HistoricalConstruction.dryPlacement(CampaignRules.era(level))&&container.canPlaceLiquid(user,level,pos,state,fluid);}
}
