package br.com.mura.betaminecraftmod.world;

import java.util.Random;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;

/** Candidate Alpha Halloween population, separate from the verified terrain sampler.
 * Uses reference attempt counts and climate rules; placement RNG is not the original full pipeline.
 */
final class HalloweenPopulation {
    private final long seed;
    private final int band;
    private final HalloweenClimate climate;
    private final InfdevNoise trees;
    HalloweenPopulation(long seed) {this(seed,3);}
    HalloweenPopulation(long seed,int band) {
        if(band!=3&&band!=4)throw new IllegalArgumentException("Not a legacy climate population band");
        this.seed=seed;this.band=band;climate=new HalloweenClimate(seed);
        trees=new InfdevTerrain(seed,2).trees;
    }
    void populate(WorldGenLevel level,int cx,int cz) {
        if(br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(cx,cz)!=band)return;
        InfdevPopulation.inBand(cx,cz,()->decorate(level,cx,cz));
    }
    private Random random(int cx,int cz,int feature){return HalloweenPopulationSettings.stream(seed,cx,cz,feature);}
    private void decorate(WorldGenLevel level,int cx,int cz) {
        int x=cx*16,z=cz*16,index=0;
        var water=random(cx,cz,30);
        if(water.nextInt(4)==0)HistoricalLakes.place(level,water,x+water.nextInt(16)+8,water.nextInt(128),z+water.nextInt(16)+8,Blocks.WATER,band,128);
        var lava=random(cx,cz,31);
        if(lava.nextInt(8)==0) {
            int lx=x+lava.nextInt(16)+8,ly=lava.nextInt(lava.nextInt(120)+8),lz=z+lava.nextInt(16)+8;
            if(ly<64||lava.nextInt(10)==0)HistoricalLakes.place(level,lava,lx,ly,lz,Blocks.LAVA,band,128);
        }
        var rooms=random(cx,cz,32);
        for(int i=0;i<8;i++)InfdevPopulation.dungeon(level,rooms,x+rooms.nextInt(16)+8,rooms.nextInt(128),z+rooms.nextInt(16)+8,true);
        for(var ore:HalloweenPopulationSettings.ORES) {
            Block block=BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(ore.block()));
            InfdevPopulation.ores(level,random(cx,cz,index++),x,z,block,ore.attempts(),ore.size(),ore.ceiling());
        }
        if(band==4) {
            var lapis=random(cx,cz,33);
            InfdevPopulation.vein(level,lapis,x+lapis.nextInt(16),lapis.nextInt(16)+lapis.nextInt(16),z+lapis.nextInt(16),Blocks.LAPIS_ORE,6);
        }
        var biome=climate.sample(x+16,z+16,1,1).biomes()[0];
        var forest=random(cx,cz,20);
        int noise=(int)((trees.sample(x*.5,z*.5,0)/8+forest.nextDouble()*4+4)/3);
        int count=HalloweenPopulationSettings.trees(biome,noise,forest.nextInt(10)==0);
        boolean large=band==3&&forest.nextInt(10)==0;
        if(band==3&&biome==HalloweenClimate.Biome.RAINFOREST&&forest.nextInt(3)==0)large=true;
        var largeOak=new HistoricalLargeOak();
        // Alpha 1.2.6 taiga still uses oak, not the later spruce/birch population.
        for(int i=0;i<count;i++) {
            int tx=x+forest.nextInt(16)+8,tz=z+forest.nextInt(16)+8;
            int ty=level.getHeight(Heightmap.Types.MOTION_BLOCKING,tx,tz);
            if(band==4) {
                var kind=HistoricalSmallTree.Kind.OAK;boolean big;
                if(biome==HalloweenClimate.Biome.TAIGA){kind=forest.nextInt(3)==0?HistoricalSmallTree.Kind.PINE:HistoricalSmallTree.Kind.SPRUCE;big=false;}
                else if(biome==HalloweenClimate.Biome.FOREST&&forest.nextInt(5)==0){kind=HistoricalSmallTree.Kind.BIRCH;big=false;}
                else big=forest.nextInt(biome==HalloweenClimate.Biome.FOREST||biome==HalloweenClimate.Biome.RAINFOREST?3:10)==0;
                if(big)HistoricalLargeOakPlacement.place(level,new HistoricalLargeOak(),forest,tx,ty,tz,band);
                else HistoricalSmallTreePlacement.place(level,forest,tx,ty,tz,band,kind);
            }else if(large)HistoricalLargeOakPlacement.place(level,largeOak,forest,tx,ty,tz,band);
            else InfdevPopulation.tree(level,forest,tx,ty,tz);
        }
        var flowers=random(cx,cz,21);
        int flowerCount=band==3?2:switch(biome){case FOREST,TAIGA->2;case SEASONAL_FOREST->4;case PLAINS->3;default->0;};
        for(int i=0;i<flowerCount;i++)InfdevPopulation.flowers(level,flowers,x,z,Blocks.DANDELION);
        if(band==4) {
            var grass=random(cx,cz,34);int grassCount=switch(biome){case FOREST,SEASONAL_FOREST->2;case RAINFOREST,PLAINS->10;case TAIGA->1;default->0;};
            for(int i=0;i<grassCount;i++)scatterPlant(level,grass,x+grass.nextInt(16)+8,grass.nextInt(128),z+grass.nextInt(16)+8,
                biome==HalloweenClimate.Biome.RAINFOREST&&grass.nextInt(3)!=0?Blocks.FERN:Blocks.SHORT_GRASS,128);
            if(biome==HalloweenClimate.Biome.DESERT)for(int i=0;i<2;i++)scatterPlant(level,grass,x+grass.nextInt(16)+8,grass.nextInt(128),z+grass.nextInt(16)+8,Blocks.DEAD_BUSH,4);
        }
        if(flowers.nextInt(2)==0)InfdevPopulation.flowers(level,flowers,x,z,Blocks.POPPY);
        if(flowers.nextInt(4)==0)InfdevPopulation.flowers(level,flowers,x,z,Blocks.BROWN_MUSHROOM);
        if(flowers.nextInt(8)==0)InfdevPopulation.flowers(level,flowers,x,z,Blocks.RED_MUSHROOM);
        var reeds=random(cx,cz,22);
        for(int i=0;i<10;i++)InfdevPopulation.reeds(level,reeds,x+reeds.nextInt(16)+8,reeds.nextInt(128),z+reeds.nextInt(16)+8);
        var cactus=random(cx,cz,23);
        if(biome==HalloweenClimate.Biome.DESERT)for(int i=0;i<10;i++)cacti(level,cactus,x+cactus.nextInt(16)+8,cactus.nextInt(128),z+cactus.nextInt(16)+8);
        var pumpkin=random(cx,cz,24);
        if(pumpkin.nextInt(32)==0)pumpkins(level,pumpkin,x+pumpkin.nextInt(16)+8,pumpkin.nextInt(128),z+pumpkin.nextInt(16)+8);
        var springs=random(cx,cz,25);
        for(int i=0;i<50;i++)InfdevPopulation.spring(level,springs,x+springs.nextInt(16)+8,springs.nextInt(springs.nextInt(120)+8),z+springs.nextInt(16)+8,Blocks.WATER);
        for(int i=0;i<20;i++)InfdevPopulation.spring(level,springs,x+springs.nextInt(16)+8,springs.nextInt(springs.nextInt(springs.nextInt(112)+8)+8),z+springs.nextInt(16)+8,Blocks.LAVA);
        var weather=climate.sample(x+8,z+8,16,16);
        for(int bx=0;bx<16;bx++)for(int bz=0;bz<16;bz++) {
            int tx=x+8+bx,tz=z+8+bz,y=level.getHeight(Heightmap.Types.MOTION_BLOCKING,tx,tz);
            var pos=new BlockPos(tx,y,tz);var below=level.getBlockState(pos.below());
            if(HalloweenPopulationSettings.snow(weather.temperature()[bx*16+bz],y)&&level.isEmptyBlock(pos)
                    &&(below.isSolidRender()||below.getBlock() instanceof LeavesBlock||below.is(Blocks.CACTUS))&&!below.is(Blocks.ICE))
                InfdevPopulation.setBlock(level,pos,Blocks.SNOW.defaultBlockState(),2);
        }
    }
    private void cacti(WorldGenLevel level,Random random,int x,int y,int z) {
        for(int attempt=0;attempt<10;attempt++) {
            int bx=x+random.nextInt(8)-random.nextInt(8),by=y+random.nextInt(4)-random.nextInt(4),bz=z+random.nextInt(8)-random.nextInt(8);
            if(by<=0||by>=128)continue;
            int height=1+random.nextInt(random.nextInt(3)+1);
            for(int dy=0;dy<height&&by+dy<128;dy++) {
                var pos=new BlockPos(bx,by+dy,bz);var state=Blocks.CACTUS.defaultBlockState();
                if(level.isEmptyBlock(pos)&&state.canSurvive(level,pos))InfdevPopulation.setBlock(level,pos,state,2);
            }
        }
    }
    static void scatterPlant(WorldGenLevel level,Random random,int x,int y,int z,Block block,int attempts) {
        while(y>0&&(level.isEmptyBlock(new BlockPos(x,y,z))||level.getBlockState(new BlockPos(x,y,z)).getBlock() instanceof LeavesBlock))y--;
        for(int i=0;i<attempts;i++) {
            var pos=new BlockPos(x+random.nextInt(8)-random.nextInt(8),y+random.nextInt(4)-random.nextInt(4),z+random.nextInt(8)-random.nextInt(8));
            var state=block.defaultBlockState();
            if(pos.getY()>0&&pos.getY()<128&&level.isEmptyBlock(pos)&&state.canSurvive(level,pos))InfdevPopulation.setBlock(level,pos,state,2);
        }
    }
    private void pumpkins(WorldGenLevel level,Random random,int x,int y,int z) {
        var directions=new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST};
        for(int attempt=0;attempt<64;attempt++) {
            var pos=new BlockPos(x+random.nextInt(8)-random.nextInt(8),y+random.nextInt(4)-random.nextInt(4),z+random.nextInt(8)-random.nextInt(8));
            if(pos.getY()>0&&pos.getY()<128&&level.isEmptyBlock(pos)&&level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK))
                InfdevPopulation.setBlock(level,pos,Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,directions[random.nextInt(4)]),2);
        }
    }
}
