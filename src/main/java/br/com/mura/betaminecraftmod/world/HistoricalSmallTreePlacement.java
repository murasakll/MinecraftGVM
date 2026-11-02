package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.ExplorationBands;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;

final class HistoricalSmallTreePlacement {
    static boolean place(WorldGenLevel level,Random random,int x,int y,int z,int band,HistoricalSmallTree.Kind kind) {
        boolean[] outside={false};
        var plan=HistoricalSmallTree.plan(p->{
            if(p.y()<0||p.y()>=128||ExplorationBands.chunkBand(Math.floorDiv(p.x(),16),Math.floorDiv(p.z(),16))!=band){outside[0]=true;return HistoricalLargeOak.Cell.SOLID;}
            var state=level.getBlockState(new BlockPos(p.x(),p.y(),p.z()));
            return state.isAir()?HistoricalLargeOak.Cell.AIR:state.getBlock() instanceof LeavesBlock?HistoricalLargeOak.Cell.LEAVES:state.is(Blocks.GRASS_BLOCK)?HistoricalLargeOak.Cell.GRASS:state.is(Blocks.DIRT)?HistoricalLargeOak.Cell.DIRT:HistoricalLargeOak.Cell.SOLID;
        },random,x,y,z,kind);
        if(plan.isEmpty()||outside[0])return false;
        for(var p:plan.keySet())if(level.getBlockEntity(new BlockPos(p.x(),p.y(),p.z()))!=null)return false;
        boolean spruce=kind==HistoricalSmallTree.Kind.PINE||kind==HistoricalSmallTree.Kind.SPRUCE;
        var log=spruce?Blocks.SPRUCE_LOG:kind==HistoricalSmallTree.Kind.BIRCH?Blocks.BIRCH_LOG:Blocks.OAK_LOG;
        var leaves=spruce?Blocks.SPRUCE_LEAVES:kind==HistoricalSmallTree.Kind.BIRCH?Blocks.BIRCH_LEAVES:Blocks.OAK_LEAVES;
        for(var entry:plan.entrySet()) {
            var p=entry.getKey();var state=switch(entry.getValue()){case DIRT->Blocks.DIRT.defaultBlockState();case LOG->log.defaultBlockState();case LEAVES->leaves.defaultBlockState().setValue(LeavesBlock.DISTANCE,1);};
            InfdevPopulation.setBlock(level,new BlockPos(p.x(),p.y(),p.z()),state,2);
        }
        return true;
    }
    private HistoricalSmallTreePlacement() { }
}
