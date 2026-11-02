package br.com.mura.betaminecraftmod.world;

import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** First terrain construction only. Never repairs or replaces blocks when loading an existing chunk. */
final class ReleasePortalPlacement {
    static void build(ChunkAccess chunk,ReleasePortalPlan plan){
        if(!plan.owns(chunk.getPos().x(),chunk.getPos().z()))return;
        int surface=chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG,plan.x()-4,plan.z()-4)+1;
        var ocean=chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);var top=chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for(var entry:plan.layout(surface)){var p=entry.getKey();var state=state(entry.getValue());chunk.setBlockState(new BlockPos(p.x(),p.y(),p.z()),state);ocean.update(p.x()&15,p.y(),p.z()&15,state);top.update(p.x()&15,p.y(),p.z()&15,state);}
    }
    private static BlockState state(ReleasePortalPlan.Kind kind){return switch(kind){
        case AIR->Blocks.AIR.defaultBlockState();case BRICK->Blocks.STONE_BRICKS.defaultBlockState();case TORCH->Blocks.TORCH.defaultBlockState();
        case LADDER_EAST->Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST);
        case FRAME_NORTH->frame(Direction.NORTH);case FRAME_SOUTH->frame(Direction.SOUTH);case FRAME_EAST->frame(Direction.EAST);case FRAME_WEST->frame(Direction.WEST);
    };}
    private static BlockState frame(Direction facing){return Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING,facing).setValue(EndPortalFrameBlock.HAS_EYE,false);}
    private ReleasePortalPlacement(){}
}
