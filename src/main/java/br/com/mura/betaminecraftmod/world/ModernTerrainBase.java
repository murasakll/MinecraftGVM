package br.com.mura.betaminecraftmod.world;

import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Detached modern terrain generation: no campaign chunks requested, saved or unlocked. */
public final class ModernTerrainBase implements GenerationBases.ModernSampler {
    private final ServerLevel level;
    private final NoiseBasedChunkGenerator nativeGenerator;
    private final RandomState random;
    public ModernTerrainBase(ServerLevel level,long seed){
        this.level=level;
        nativeGenerator=(NoiseBasedChunkGenerator)WorldPresets.createNormalWorldDimensions(level.registryAccess()).overworld();
        random=RandomState.create(level.registryAccess().lookupOrThrow(Registries.NOISE),seed,nativeGenerator.generatorSettings().value());
    }
    public CompletableFuture<ChunkAccess> chunk(int era,int x,int z){
        if(era<13||era>19)throw new IllegalArgumentException("Not a modern era");
        BiomeSource source=era==19?nativeGenerator.getBiomeSource():new FilteredSource(nativeGenerator.getBiomeSource(),era,level.registryAccess());
        var generator=new NoiseBasedChunkGenerator(source,nativeGenerator.generatorSettings());
        var chunk=new ProtoChunk(new ChunkPos(x,z),UpgradeData.EMPTY,LevelHeightAccessor.create(-64,384),level.palettedContainerFactory(),null);
        var resolver=source.createUncachedResolver(random);
        chunk.fillBiomesFromNoise(resolver);
        var manager=new BiomeManager(resolver,BiomeManager.obfuscateSeed(random.seed()));
        return generator.buildTerrain(chunk,Blender.empty(),random,level.structureManager(),manager,null,source.possibleBiomes());
    }
    @Override public TerrainVolume generate(int era,int x,int z){
        return volume(chunk(era,x,z).join());
    }
    public static TerrainVolume volume(ChunkAccess chunk){
        var palette=new ArrayList<String>();var lookup=new HashMap<String,Short>();short[] blocks=new short[256*384];var pos=new BlockPos.MutableBlockPos();
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=-64;y<320;y++){
            String name=BuiltInRegistries.BLOCK.getKey(chunk.getBlockState(pos.set(x,y,z)).getBlock()).toString();
            Short id=lookup.get(name);if(id==null){id=(short)palette.size();lookup.put(name,id);palette.add(name);}blocks[(x*16+z)*384+y+64]=id;
        }
        return new TerrainVolume(-64,384,palette,blocks);
    }
    private static final class FilteredSource extends BiomeSource {
        private final BiomeSource parent;
        private final Map<Holder<Biome>,Holder<Biome>> replacements=new HashMap<>();
        FilteredSource(BiomeSource parent,int era,HolderLookup.Provider registries){
            this.parent=parent;var registry=registries.lookupOrThrow(Registries.BIOME);
            for(var biome:parent.possibleBiomes()){
                String key=biome.unwrapKey().orElseThrow().identifier().toString();
                String replacement=ModernEraBiomePolicy.replacement(era,key);
                replacements.put(biome,key.equals(replacement)?biome:registry.getOrThrow(ResourceKey.create(Registries.BIOME,Identifier.parse(replacement))));
            }
        }
        @Override protected MapCodec<? extends BiomeSource> codec(){throw new UnsupportedOperationException("Detached preview source");}
        @Override protected Stream<Holder<Biome>> collectPossibleBiomes(){return replacements.values().stream();}
        @Override public BiomeResolver createResolver(Climate.Sampler sampler){
            var resolver=parent.createResolver(sampler);return(x,y,z)->replacements.get(resolver.getNoiseBiome(x,y,z));
        }
    }
}
