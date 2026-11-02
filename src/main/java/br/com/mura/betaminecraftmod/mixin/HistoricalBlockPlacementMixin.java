package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class HistoricalBlockPlacementMixin {
    @Inject(method="getPlacementState",at=@At("RETURN"),cancellable=true)
    private void campaign$noWaterlogging(BlockPlaceContext context,CallbackInfoReturnable<BlockState> cir) {
        var state=cir.getReturnValue();
        if(state!=null&&HistoricalConstruction.dryPlacement(CampaignRules.era(context.getLevel()))) {
            if(state.hasProperty(BlockStateProperties.WATERLOGGED))state=state.setValue(BlockStateProperties.WATERLOGGED,false);
            if(state.getBlock() instanceof net.minecraft.world.level.block.RotatedPillarBlock)state=state.setValue(BlockStateProperties.AXIS,net.minecraft.core.Direction.Axis.Y);
            cir.setReturnValue(state);
        }
    }
}
