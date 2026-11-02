package br.com.mura.betaminecraftmod.world;

import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.core.*;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/** Shared bounded adapter for 128-high historical dimension terrain. */
public abstract class HistoricalDimensionGenerator extends ChunkGenerator implements CampaignGenerationGate {
    protected final Holder<Biome> biome;protected final long seed;protected volatile int era;
    private final BiFunction<Integer,Integer,byte[]> terrain;
    private final Map<Long,byte[]> cache=Collections.synchronizedMap(new LinkedHashMap<>(16,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<Long,byte[]> entry){return size()>16;}});
    protected HistoricalDimensionGenerator(Holder<Biome> biome,long seed,BiFunction<Integer,Integer,byte[]> terrain){super(new FixedBiomeSource(biome),ignored->BiomeGenerationSettings.EMPTY);this.biome=biome;this.seed=seed;this.terrain=terrain;}
    @Override public void unlockThrough(int era){this.era=era;}
    @Override public int getMinY(){return 0;}
    @Override public int getGenDepth(){return 128;}
    @Override public int getSpawnHeight(LevelHeightAccessor level){return 64;}
    @Override public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structures,RandomState random,long seed){return ChunkGeneratorStructureState.createForFlat(random,seed,getOrigin(random),biomeSource,java.util.stream.Stream.empty());}
    private byte[] sample(int x,int z){long key=(x&0xffffffffL)|((long)z<<32);return cache.computeIfAbsent(key,k->terrain.apply(x,z));}
    private static BlockState block(byte id){return switch(Byte.toUnsignedInt(id)){case 7->Blocks.BEDROCK.defaultBlockState();case 10,11->Blocks.LAVA.defaultBlockState();case 13->Blocks.GRAVEL.defaultBlockState();case 87->Blocks.NETHERRACK.defaultBlockState();case 88->Blocks.SOUL_SAND.defaultBlockState();case 121->Blocks.END_STONE.defaultBlockState();default->Blocks.AIR.defaultBlockState();};}
    @Override public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk,Blender blender,RandomState random,StructureManager structures,BiomeManager biomes,WorldGenRegion region,Set<Holder<Biome>> possibleBiomes) {
        int cx=chunk.getPos().x(),cz=chunk.getPos().z();if(locked(cx,cz))return CompletableFuture.completedFuture(chunk);
        var blocks=sample(cx,cz);var position=new BlockPos.MutableBlockPos();
        var ocean=chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);var surface=chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++) {
            var state=block(blocks[InfdevTerrain.index(x,y,z)]);if(state.isAir())continue;
            chunk.setBlockState(position.set(x,y,z),state);ocean.update(x,y,z,state);surface.update(x,y,z,state);
        }
        return CompletableFuture.completedFuture(chunk);
    }
    @Override public void applyBiomeDecoration(WorldGenLevel level,ChunkAccess chunk,StructureManager structures) { }
    @Override public void spawnOriginalMobs(WorldGenRegion region) { }
    @Override public NoiseColumn getBaseColumn(int x,int z,LevelHeightAccessor level,RandomState random) {
        var column=new BlockState[128];Arrays.fill(column,Blocks.AIR.defaultBlockState());
        if(!locked(Math.floorDiv(x,16),Math.floorDiv(z,16))){var blocks=sample(Math.floorDiv(x,16),Math.floorDiv(z,16));for(int y=0;y<128;y++)column[y]=block(blocks[InfdevTerrain.index(x&15,y,z&15)]);}
        return new NoiseColumn(0,column);
    }
    @Override public int getBaseHeight(int x,int z,Heightmap.Types type,LevelHeightAccessor level,RandomState random){var column=getBaseColumn(x,z,level,random);for(int y=127;y>=0;y--)if(type.isOpaque().test(column.getBlock(y)))return y+1;return 0;}
    @Override public void addDebugScreenInfo(List<String> lines,RandomState random,BlockPos pos,SamplerContext context){lines.add(getClass().getSimpleName()+" / Y 0..127 / sea "+getSeaLevel());}
}
