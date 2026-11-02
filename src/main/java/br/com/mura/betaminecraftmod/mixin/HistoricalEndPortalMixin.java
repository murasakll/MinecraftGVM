package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import br.com.mura.betaminecraftmod.world.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(EndPortalBlock.class)
public abstract class HistoricalEndPortalMixin {
    @Unique private static boolean campaign$release(Level level){return CampaignRules.era(level)==6&&CampaignRules.firstRelease(level).equals("1.0");}
    @Inject(method="getPortalDestination",at=@At("HEAD"),cancellable=true)
    private void campaign$destination(ServerLevel source,Entity entity,BlockPos entry,CallbackInfoReturnable<TeleportTransition> result) {
        int era=CampaignRules.era(source);
        // Native code creates the destination platform before the travel event. Refuse before that side effect.
        if(era>=0&&era<6){result.setReturnValue(null);return;}
        if(!campaign$release(source))return;
        if(!(entity instanceof ServerPlayer)||entity.isPassenger()||entity.isVehicle()){result.setReturnValue(null);return;}
        if(source.dimension()==Level.END)return; // Credits and campaign origin-respawn policy retain their own path.
        if(source.dimension()!=Level.OVERWORLD){result.setReturnValue(null);return;}
        var end=source.getServer().getLevel(Level.END);
        if(end==null){result.setReturnValue(null);return;}
        if(!(end.getChunkSource().getGenerator() instanceof HistoricalEndChunkGenerator gate))return;
        if(!HistoricalEndArrival.place(end,gate)){result.setReturnValue(null);return;}
        var spawn=HistoricalEndArrivalLayout.spawn();
        result.setReturnValue(new TeleportTransition(end,new Vec3(spawn.x(),spawn.y(),spawn.z()),Vec3.ZERO,90,0,
            TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)));
    }
    @Inject(method="entityInside",at=@At("HEAD"),cancellable=true)
    private void campaign$inside(BlockState state,Level level,BlockPos pos,Entity entity,InsideBlockEffectApplier effects,boolean precise,CallbackInfo ci) {
        int era=CampaignRules.era(level);
        if(era>=0&&era<6){ci.cancel();return;}
        if(!campaign$release(level))return;
        if(!(entity instanceof Player)||entity.isPassenger()||entity.isVehicle()){ci.cancel();return;}
        // The original 1.0 local-player exit opens the poem on every return, with no seenCredits bypass.
        if(level.dimension()==Level.END&&entity instanceof ServerPlayer player&&entity.canUsePortal(false)){player.showEndCredits();ci.cancel();}
    }
    @Inject(method="getShape",at=@At("HEAD"),cancellable=true)
    private void campaign$shape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context,CallbackInfoReturnable<VoxelShape> result) {
        if(CampaignRules.era(level)==6&&CampaignRules.firstRelease(level).equals("1.0"))result.setReturnValue(Block.box(0,0,0,16,1,16));
    }
    @Inject(method="getEntityInsideCollisionShape",at=@At("HEAD"),cancellable=true)
    private void campaign$contact(BlockState state,BlockGetter level,BlockPos pos,Entity entity,CallbackInfoReturnable<VoxelShape> result) {
        // The original world tested the occupied block cell, independently of its thin selection outline.
        if(CampaignRules.era(level)==6&&CampaignRules.firstRelease(level).equals("1.0"))result.setReturnValue(Shapes.block());
    }
}
