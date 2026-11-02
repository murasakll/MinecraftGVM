package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.ExplorationBands;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;

final class HistoricalLargeOakPlacement {
    static boolean place(WorldGenLevel level,HistoricalLargeOak oak,Random random,int x,int y,int z,int band) {
        boolean[] outside={false};
        var plan=oak.plan(p->{
            if(p.y()<0||p.y()>=128||ExplorationBands.chunkBand(Math.floorDiv(p.x(),16),Math.floorDiv(p.z(),16))!=band){outside[0]=true;return HistoricalLargeOak.Cell.SOLID;}
            var state=level.getBlockState(new BlockPos(p.x(),p.y(),p.z()));
            return state.isAir()?HistoricalLargeOak.Cell.AIR:state.is(Blocks.OAK_LEAVES)?HistoricalLargeOak.Cell.LEAVES:
                state.is(Blocks.GRASS_BLOCK)?HistoricalLargeOak.Cell.GRASS:state.is(Blocks.DIRT)?HistoricalLargeOak.Cell.DIRT:HistoricalLargeOak.Cell.SOLID;
        },random,x,y,z);
        if(plan.isEmpty()||outside[0])return false;
        for(var p:plan.keySet())if(p.y()<1||p.y()>=128||ExplorationBands.chunkBand(Math.floorDiv(p.x(),16),Math.floorDiv(p.z(),16))!=band
            ||level.getBlockEntity(new BlockPos(p.x(),p.y(),p.z()))!=null)return false;
        for(var entry:plan.entrySet()) {
            var p=entry.getKey();var block=entry.getValue()==HistoricalLargeOak.Part.LOG?Blocks.OAK_LOG.defaultBlockState():Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE,1);
            InfdevPopulation.setBlock(level,new BlockPos(p.x(),p.y(),p.z()),block,2);
        }
        return true;
    }
    private HistoricalLargeOakPlacement() { }
}
