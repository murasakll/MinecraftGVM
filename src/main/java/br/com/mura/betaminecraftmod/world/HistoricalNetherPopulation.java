package br.com.mura.betaminecraftmod.world;

import java.util.Random;
import net.minecraft.core.*;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;

/** Halloween attempt budgets and feature geometry, with independent campaign RNG channels. */
final class HistoricalNetherPopulation {
    private final long seed;private final CampaignGenerationGate gate;
    private final int sourceBand;
    HistoricalNetherPopulation(long seed,CampaignGenerationGate gate){this(seed,gate,3);}
    HistoricalNetherPopulation(long seed,CampaignGenerationGate gate,int sourceBand){this.seed=seed;this.gate=gate;this.sourceBand=sourceBand;}
    void populate(WorldGenLevel level,int cx,int cz) {
        if(gate.locked(cx,cz)||gate instanceof HistoricalNetherChunkGenerator generator&&generator.bandAt(cx,cz)!=sourceBand)return;
        int x=cx*16,z=cz*16;var lava=stream(cx,cz,0);
        for(int i=0;i<8;i++)spring(level,new BlockPos(x+lava.nextInt(16)+8,lava.nextInt(120)+4,z+lava.nextInt(16)+8));
        var fire=stream(cx,cz,1);int fires=fire.nextInt(fire.nextInt(10)+1)+1;
        for(int i=0;i<fires;i++)patch(level,fire,x+fire.nextInt(16)+8,fire.nextInt(120)+4,z+fire.nextInt(16)+8,Blocks.FIRE);
        var clusters=stream(cx,cz,2);int count=clusters.nextInt(clusters.nextInt(10)+1);
        for(int i=0;i<count;i++)glowstone(level,clusters,new BlockPos(x+clusters.nextInt(16)+8,clusters.nextInt(120)+4,z+clusters.nextInt(16)+8));
        for(int i=0;i<10;i++)glowstone(level,clusters,new BlockPos(x+clusters.nextInt(16)+8,clusters.nextInt(128),z+clusters.nextInt(16)+8));
        var fungi=stream(cx,cz,3);
        patch(level,fungi,x+fungi.nextInt(16)+8,fungi.nextInt(128),z+fungi.nextInt(16)+8,Blocks.BROWN_MUSHROOM);
        patch(level,fungi,x+fungi.nextInt(16)+8,fungi.nextInt(128),z+fungi.nextInt(16)+8,Blocks.RED_MUSHROOM);
    }
    private Random stream(int x,int z,int feature){return HalloweenPopulationSettings.stream(seed,x,z,300+feature);}
    private boolean set(WorldGenLevel level,BlockPos pos,Block block) {
        if(pos.getY()<1||pos.getY()>=127||gate.locked(Math.floorDiv(pos.getX(),16),Math.floorDiv(pos.getZ(),16))||level.getBlockEntity(pos)!=null)return false;
        if(gate instanceof HistoricalNetherChunkGenerator generator&&generator.bandAt(Math.floorDiv(pos.getX(),16),Math.floorDiv(pos.getZ(),16))!=sourceBand)return false;
        return level.setBlock(pos,block.defaultBlockState(),2);
    }
    void spring(WorldGenLevel level,BlockPos pos) {
        if(!level.getBlockState(pos.above()).is(Blocks.NETHERRACK)||!level.isEmptyBlock(pos)&&!level.getBlockState(pos).is(Blocks.NETHERRACK))return;
        int rock=0,air=0;
        for(var direction:new Direction[]{Direction.WEST,Direction.EAST,Direction.NORTH,Direction.SOUTH,Direction.DOWN}) {
            var state=level.getBlockState(pos.relative(direction));if(state.is(Blocks.NETHERRACK))rock++;if(state.isAir())air++;
        }
        if(rock==4&&air==1&&set(level,pos,Blocks.LAVA))level.scheduleTick(pos,net.minecraft.world.level.material.Fluids.LAVA,1);
    }
    void patch(WorldGenLevel level,Random random,int x,int y,int z,Block block) {
        for(int i=0;i<64;i++) {
            var pos=new BlockPos(x+random.nextInt(8)-random.nextInt(8),y+random.nextInt(4)-random.nextInt(4),z+random.nextInt(8)-random.nextInt(8));
            if(pos.getY()<1||pos.getY()>=127||!level.isEmptyBlock(pos))continue;
            var below=level.getBlockState(pos.below());
            if(block==Blocks.FIRE?below.is(Blocks.NETHERRACK):below.isSolidRender()&&level.getMaxLocalRawBrightness(pos)<13)set(level,pos,block);
        }
    }
    void glowstone(WorldGenLevel level,Random random,BlockPos origin) {
        if(!level.isEmptyBlock(origin)||!level.getBlockState(origin.above()).is(Blocks.NETHERRACK)||!set(level,origin,Blocks.GLOWSTONE))return;
        for(int i=0;i<1500;i++) {
            var pos=origin.offset(random.nextInt(8)-random.nextInt(8),-random.nextInt(12),random.nextInt(8)-random.nextInt(8));
            if(!level.isEmptyBlock(pos))continue;
            int neighbors=0;for(var direction:Direction.values())if(level.getBlockState(pos.relative(direction)).is(Blocks.GLOWSTONE))neighbors++;
            if(neighbors==1)set(level,pos,Blocks.GLOWSTONE);
        }
    }
}
