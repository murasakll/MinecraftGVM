package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.ExplorationBands;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** Shared candidate lake placement. A crossing of an era boundary rejects the whole lake. */
final class HistoricalLakes {
    private HistoricalLakes() { }
    static boolean place(WorldGenLevel level,Random random,int x,int y,int z,Block fluid,int band,int ceiling) {
        if(fluid!=Blocks.WATER&&fluid!=Blocks.LAVA)throw new IllegalArgumentException("Not a lake fluid");
        int left=x-8,back=z-8;
        // Preflight the footprint: never leave a truncated lake leaking into the next/previous era.
        for(int bx=0;bx<16;bx++)for(int bz=0;bz<16;bz++)
            if(ExplorationBands.chunkBand(Math.floorDiv(left+bx,16),Math.floorDiv(back+bz,16))!=band)return false;
        while(y>0&&level.isEmptyBlock(new BlockPos(left,y,back)))y--;
        int bottom=y-4;
        if(bottom<1||bottom+8>ceiling)return false;
        boolean[] mask=HistoricalLakeShape.create(random);
        for(int dy=0;dy<8;dy++)for(int bz=0;bz<16;bz++)for(int bx=0;bx<16;bx++) {
            var pos=new BlockPos(left+bx,bottom+dy,back+bz);var state=level.getBlockState(pos);
            if(mask[HistoricalLakeShape.index(bx,dy,bz)]&&level.getBlockEntity(pos)!=null)return false;
            if(!HistoricalLakeShape.boundary(mask,bx,dy,bz))continue;
            if(dy>=4&&!state.getFluidState().isEmpty())return false;
            if(dy<4&&!solid(state)&&!state.is(fluid))return false;
        }
        for(int dy=0;dy<8;dy++)for(int bz=0;bz<16;bz++)for(int bx=0;bx<16;bx++)if(mask[HistoricalLakeShape.index(bx,dy,bz)])
            InfdevPopulation.setBlock(level,new BlockPos(left+bx,bottom+dy,back+bz),(dy<4?fluid:Blocks.AIR).defaultBlockState(),2);
        for(int dy=4;dy<8;dy++)for(int bz=0;bz<16;bz++)for(int bx=0;bx<16;bx++)if(mask[HistoricalLakeShape.index(bx,dy,bz)]) {
            var pos=new BlockPos(left+bx,bottom+dy,back+bz);
            if(level.getBlockState(pos.below()).is(Blocks.DIRT)&&level.getBrightness(LightLayer.SKY,pos)>0)
                InfdevPopulation.setBlock(level,pos.below(),Blocks.GRASS_BLOCK.defaultBlockState(),2);
        }
        return true;
    }
    private static boolean solid(BlockState state){return state.isSolidRender()||state.getBlock() instanceof LeavesBlock||state.is(Blocks.GLASS)||state.is(Blocks.ICE)||state.is(Blocks.CACTUS);}
}
