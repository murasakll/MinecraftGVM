package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import br.com.mura.betaminecraftmod.world.*;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin(PortalShape.class)
public abstract class HistoricalPortalShapeMixin {
    @Unique private static PortalShape campaign$shape(HistoricalNetherPortalLayout.Frame frame){
        boolean x=frame.dx()!=0;var origin=frame.origin();
        return HistoricalPortalShapeAccessor.campaign$create(x?Direction.Axis.X:Direction.Axis.Z,frame.portalBlocks(),x?Direction.WEST:Direction.SOUTH,
            new BlockPos(origin.x()+(x?1:0),origin.y(),origin.z()),2,3);
    }
    @Inject(method="findEmptyPortalShape",at=@At("HEAD"),cancellable=true)
    private static void campaign$ignite(LevelAccessor level,BlockPos pos,Direction.Axis axis,CallbackInfoReturnable<Optional<PortalShape>> result){
        int era=CampaignRules.era(level);if(era>=0&&era<3){result.setReturnValue(Optional.empty());return;}
        if(!HistoricalPortalView.historical(level))return;
        var frame=HistoricalNetherPortalLayout.ignite(new HistoricalPortalView(level),new HistoricalNetherPortalLayout.Pos(pos.getX(),pos.getY(),pos.getZ()));
        result.setReturnValue(frame==null?Optional.empty():Optional.of(campaign$shape(frame)));
    }
    @Inject(method="findAnyShape",at=@At("HEAD"),cancellable=true)
    private static void campaign$complete(BlockGetter level,BlockPos pos,Direction.Axis axis,CallbackInfoReturnable<PortalShape> result){
        if(!HistoricalPortalView.historical(level))return;
        var frame=HistoricalNetherPortalLayout.complete(new HistoricalPortalView(level),new HistoricalNetherPortalLayout.Pos(pos.getX(),pos.getY(),pos.getZ()),axis==Direction.Axis.X);
        result.setReturnValue(frame==null?HistoricalPortalShapeAccessor.campaign$create(axis,0,axis==Direction.Axis.X?Direction.WEST:Direction.SOUTH,pos,0,0):campaign$shape(frame));
    }
}
