package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StairBlock.class)
public abstract class InfdevStairsMixin {
    @Inject(method="getShape",at=@At("HEAD"),cancellable=true)
    private void campaign$oldSelectionBox(BlockState state,BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context,CallbackInfoReturnable<net.minecraft.world.phys.shapes.VoxelShape> cir){if(br.com.mura.betaminecraftmod.progression.HistoricalConstruction.straightStairs(CampaignRules.era(level)))cir.setReturnValue(net.minecraft.world.phys.shapes.Shapes.block());}
    @Inject(method = "getStairsShape", at = @At("HEAD"), cancellable = true)
    private static void campaign$straight(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<StairsShape> cir) {
        if (br.com.mura.betaminecraftmod.progression.HistoricalConstruction.straightStairs(CampaignRules.era(level))) cir.setReturnValue(StairsShape.STRAIGHT);
    }
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void campaign$bottom(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        int era=CampaignRules.era(context.getLevel());
        if (br.com.mura.betaminecraftmod.progression.HistoricalConstruction.dryPlacement(era) && cir.getReturnValue() != null) {
            var state=cir.getReturnValue().setValue(BlockStateProperties.WATERLOGGED,false);
            if(br.com.mura.betaminecraftmod.progression.HistoricalConstruction.bottomStairs(era,CampaignRules.firstRelease(context.getLevel())))state=state.setValue(BlockStateProperties.HALF,Half.BOTTOM);
            cir.setReturnValue(state);
        }
    }
}
