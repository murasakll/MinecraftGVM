package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.ExplorationBands;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.version.MCVersion;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.Heightmap;

/** Candidate common population for Adventure and early release; structures remain separate. */
final class AdventurePopulation {
    private final long seed;private final int band;private final String release;
    private final AdventureBiomes adventure;
    AdventurePopulation(long seed,int band,String release) {
        if(band!=5&&band!=6)throw new IllegalArgumentException("Not an Adventure/release population");
        GenerationBases.validateFirstRelease(release);this.seed=seed;this.band=band;this.release=release;adventure=new AdventureBiomes(seed);
    }
    void populate(WorldGenLevel level,int cx,int cz) {
        if(ExplorationBands.chunkBand(cx,cz)!=band)return;
        InfdevPopulation.inBand(cx,cz,()->decorate(level,cx,cz));
    }
    private Random random(int cx,int cz,int channel){return HalloweenPopulationSettings.stream(seed,cx,cz,100+channel+band*50);}
    private void decorate(WorldGenLevel level,int cx,int cz) {
        int x=cx*16,z=cz*16;
        int biome=band==5?adventure.blocks(x+16,z+16,1,1)[0]:new OverworldBiomeSource(MCVersion.fromString(release),seed).getBiome(x+16,0,z+16).getId();
        var water=random(cx,cz,0);if(water.nextInt(4)==0)HistoricalLakes.place(level,water,x+water.nextInt(16)+8,water.nextInt(128),z+water.nextInt(16)+8,Blocks.WATER,band,128);
        var lava=random(cx,cz,1);if(lava.nextInt(8)==0) {
            int lx=x+lava.nextInt(16)+8,ly=lava.nextInt(lava.nextInt(120)+8),lz=z+lava.nextInt(16)+8;
            if(ly<63||lava.nextInt(10)==0)HistoricalLakes.place(level,lava,lx,ly,lz,Blocks.LAVA,band,128);
        }
        var rooms=random(cx,cz,2);for(int i=0;i<8;i++)InfdevPopulation.dungeon(level,rooms,x+rooms.nextInt(16)+8,rooms.nextInt(128),z+rooms.nextInt(16)+8,true);
        int channel=3;
        for(var ore:HalloweenPopulationSettings.ORES) {
            if(ore.block().equals("clay"))continue;
            var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(ore.block()));
            InfdevPopulation.ores(level,random(cx,cz,channel++),x,z,block,ore.attempts(),ore.size(),ore.ceiling());
        }
        var lapis=random(cx,cz,12);InfdevPopulation.vein(level,lapis,x+lapis.nextInt(16),lapis.nextInt(16)+lapis.nextInt(16),z+lapis.nextInt(16),Blocks.LAPIS_ORE,6);
        var forest=random(cx,cz,20);int trees=biome==4||biome==5?10:biome==6?2:biome==1||biome==2||biome==14||biome==15?-100:0;
        if(forest.nextInt(10)==0)trees++;
        for(int i=0;i<trees;i++) {
            int tx=x+forest.nextInt(16)+8,tz=z+forest.nextInt(16)+8,ty=level.getHeight(Heightmap.Types.MOTION_BLOCKING,tx,tz);
            if(biome==5)HistoricalSmallTreePlacement.place(level,forest,tx,ty,tz,band,forest.nextInt(3)==0?HistoricalSmallTree.Kind.PINE:HistoricalSmallTree.Kind.SPRUCE);
            else if(biome==4&&forest.nextInt(5)==0)HistoricalSmallTreePlacement.place(level,forest,tx,ty,tz,band,HistoricalSmallTree.Kind.BIRCH);
            else if(forest.nextInt(10)==0)HistoricalLargeOakPlacement.place(level,new HistoricalLargeOak(),forest,tx,ty,tz,band);
            else HistoricalSmallTreePlacement.place(level,forest,tx,ty,tz,band,HistoricalSmallTree.Kind.OAK);
        }
        var plants=random(cx,cz,21);int flowers=biome==6?-999:biome==1?4:biome==14||biome==15?-100:2;
        for(int i=0;i<flowers;i++)InfdevPopulation.flowers(level,plants,x,z,Blocks.DANDELION);
        if(plants.nextInt(2)==0)InfdevPopulation.flowers(level,plants,x,z,Blocks.POPPY);
        int grass=biome==1?10:biome==4?2:biome==14||biome==15?-100:1;
        for(int i=0;i<grass;i++)HalloweenPopulation.scatterPlant(level,plants,x+plants.nextInt(16)+8,plants.nextInt(128),z+plants.nextInt(16)+8,Blocks.SHORT_GRASS,128);
        if(biome==2)for(int i=0;i<2;i++)HalloweenPopulation.scatterPlant(level,plants,x+plants.nextInt(16)+8,plants.nextInt(128),z+plants.nextInt(16)+8,Blocks.DEAD_BUSH,4);
        var reeds=random(cx,cz,22);for(int i=0;i<(biome==2?60:biome==6?20:10);i++)InfdevPopulation.reeds(level,reeds,x+reeds.nextInt(16)+8,reeds.nextInt(128),z+reeds.nextInt(16)+8);
        var springs=random(cx,cz,23);
        for(int i=0;i<50;i++)InfdevPopulation.spring(level,springs,x+springs.nextInt(16)+8,springs.nextInt(springs.nextInt(120)+8),z+springs.nextInt(16)+8,Blocks.WATER);
        for(int i=0;i<20;i++)InfdevPopulation.spring(level,springs,x+springs.nextInt(16)+8,springs.nextInt(springs.nextInt(springs.nextInt(112)+8)+8),z+springs.nextInt(16)+8,Blocks.LAVA);
        // Snow only for the cold identities present in the selected reference.
        if(biome==5||biome==12||biome==13)for(int bx=8;bx<24;bx++)for(int bz=8;bz<24;bz++) {
            var pos=new BlockPos(x+bx,level.getHeight(Heightmap.Types.MOTION_BLOCKING,x+bx,z+bz),z+bz);
            var below=level.getBlockState(pos.below());
            if(pos.getY()>0&&pos.getY()<128&&level.isEmptyBlock(pos)&&(below.isSolidRender()||below.getBlock() instanceof LeavesBlock)&&!below.is(Blocks.ICE))InfdevPopulation.setBlock(level,pos,Blocks.SNOW.defaultBlockState(),2);
        }
    }
}
