package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import br.com.mura.betaminecraftmod.world.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(NetherPortalBlock.class)
public abstract class HistoricalNetherPortalMixin {
    @Inject(method="getPortalDestination",at=@At("HEAD"),cancellable=true)
    private void campaign$locked(ServerLevel level,Entity entity,BlockPos pos,CallbackInfoReturnable<TeleportTransition> result){
        int era=CampaignRules.era(level);if(era>=0&&era<3){result.setReturnValue(null);return;}
        if(HistoricalPortalView.historical(level))result.setReturnValue(HistoricalNetherTravel.destination(level,entity));
    }
    @Inject(method="entityInside",at=@At("HEAD"),cancellable=true)
    private void campaign$contact(BlockState state,Level level,BlockPos pos,Entity entity,InsideBlockEffectApplier effects,boolean precise,CallbackInfo ci){
        int era=CampaignRules.era(level);
        if(era>=0&&era<3||HistoricalPortalView.historical(level)&&(!(entity instanceof Player)||entity.isPassenger()||entity.isVehicle()))ci.cancel();
    }
    @Inject(method="randomTick",at=@At("HEAD"),cancellable=true)
    private void campaign$noPortalMobs(BlockState state,ServerLevel level,BlockPos pos,RandomSource random,CallbackInfo ci){
        int era=CampaignRules.era(level);if(era>=0&&era<=6)ci.cancel();
    }
    @Inject(method="getPortalTransitionTime",at=@At("HEAD"),cancellable=true)
    private void campaign$wait(ServerLevel level,Entity entity,CallbackInfoReturnable<Integer> result){if(HistoricalPortalView.historical(level))result.setReturnValue(80);}
    @Inject(method="updateShape",at=@At("HEAD"),cancellable=true)
    private void campaign$integrity(BlockState state,LevelReader level,ScheduledTickAccess ticks,BlockPos pos,Direction direction,BlockPos neighbour,BlockState neighbourState,RandomSource random,CallbackInfoReturnable<BlockState> result){
        if(HistoricalPortalView.historical(level))result.setReturnValue(HistoricalNetherPortalLayout.survives(new HistoricalPortalView(level),new HistoricalNetherPortalLayout.Pos(pos.getX(),pos.getY(),pos.getZ()))?state:Blocks.AIR.defaultBlockState());
    }
}
