package br.com.mura.betaminecraftmod.world;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import java.util.stream.Stream;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.level.biome.*;
import br.com.mura.betaminecraftmod.progression.GenerationRouter;

/** Coordinate-stable biome identities. Unlocking never replaces biomes in earlier rings. */
public final class HistoricalBiomeSource extends BiomeSource {
    public static final MapCodec<HistoricalBiomeSource> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
        Biome.CODEC.fieldOf("biome").forGetter(s->s.initial),
        Codec.LONG.fieldOf("seed").forGetter(s->s.seed),
        RegistryOps.retrieveGetter(Registries.BIOME),
        ModernClimateContext.CODEC.forGetter(s->s.modernContext),
        Codec.STRING.optionalFieldOf("first_release","1.2.5").forGetter(s->s.firstRelease)
    ).apply(i,HistoricalBiomeSource::new));
    private final Holder<Biome> initial;
    private final long seed;
    private final String firstRelease;
    private final HalloweenClimate climate;
    private final Map<HalloweenClimate.Biome,Holder<Biome>> halloween;
    private final Map<String,Holder<Biome>> release;
    private final AdventureBiomes adventure;
    private final ModernClimateContext modernContext;
    private final BiomeResolver modern;
    private final Map<String,Holder<Biome>> modernBiomes;
    private record ReleaseSample(int x,int z,int era,Holder<Biome>[] biomes){}
    private final ThreadLocal<ReleaseSample> lastRelease=new ThreadLocal<>();
    private record Sample(int x,int z,HalloweenClimate.Grid grid){}
    private final ThreadLocal<Sample> last=new ThreadLocal<>();
    public HistoricalBiomeSource(Holder<Biome> initial,long seed,HolderGetter<Biome> registry,ModernClimateContext context){
        this(initial,seed,registry,context,"1.2.5");
    }
    public HistoricalBiomeSource(Holder<Biome> initial,long seed,HolderGetter<Biome> registry,ModernClimateContext context,String firstRelease){
        GenerationBases.validateFirstRelease(firstRelease);this.firstRelease=firstRelease;
        this.initial=initial;this.seed=seed;climate=new HalloweenClimate(seed);
        modernContext=context;modern=context.resolver(seed);
        var modernMap=new HashMap<String,Holder<Biome>>();
        for(var b:context.source().possibleBiomes()){
            String name=b.unwrapKey().orElseThrow().identifier().toString();modernMap.put(name,b);
            for(int era=13;era<20;era++){
                String replacement=ModernEraBiomePolicy.replacement(era,name);
                modernMap.computeIfAbsent(replacement,key->registry.getOrThrow(ResourceKey.create(Registries.BIOME,Identifier.parse(key))));
            }
        }
        modernBiomes=Map.copyOf(modernMap);
        var map=new EnumMap<HalloweenClimate.Biome,Holder<Biome>>(HalloweenClimate.Biome.class);
        for(var biome:HalloweenClimate.Biome.values())map.put(biome,registry.getOrThrow(ResourceKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath("betaminecraftmod","halloween/"+biome.name().toLowerCase(Locale.ROOT)))));
        halloween=Map.copyOf(map);
        adventure=new AdventureBiomes(seed);
        var releases=new HashMap<String,Holder<Biome>>();
        for(var b:com.seedfinding.mcbiome.biome.Biomes.REGISTRY.values()){
            if(b.getDimension()!=com.seedfinding.mccore.state.Dimension.OVERWORLD)continue;
            for(int era=5;era<=12;era++){
                String id=HistoricalBiomeNames.current(b.getName(),era);
                releases.computeIfAbsent(id,key->registry.getOrThrow(ResourceKey.create(Registries.BIOME,Identifier.parse(key))));
            }
        }
        release=Map.copyOf(releases);
    }
    @Override protected MapCodec<? extends BiomeSource> codec(){return CODEC;}
    @Override protected Stream<Holder<Biome>> collectPossibleBiomes(){return Stream.of(Stream.of(initial),halloween.values().stream(),release.values().stream(),modernBiomes.values().stream()).flatMap(s->s).distinct();}
    @Override public BiomeResolver createResolver(Climate.Sampler ignored){
        return (x,y,z)->{
            int blockX=x*4+2,blockZ=z*4+2,cx=Math.floorDiv(blockX,16),cz=Math.floorDiv(blockZ,16);
            int era=GenerationRouter.plannedEra(cx,cz).ordinal();
            if(era>=13){
                var b=modern.getNoiseBiome(x,y,z);
                String name=b.unwrapKey().orElseThrow().identifier().toString();
                return modernBiomes.get(ModernEraBiomePolicy.replacement(era,name));
            }
            if(era<3)return initial;
            if(era>=5)return releaseBiome(era,blockX,blockZ,cx,cz);
            Sample sample=last.get();
            if(sample==null||sample.x()!=cx||sample.z()!=cz){sample=new Sample(cx,cz,climate.sample(cx*16,cz*16,16,16));last.set(sample);}
            return halloween.get(sample.grid().biomes()[(blockX&15)*16+(blockZ&15)]);
        };
    }
    @SuppressWarnings("unchecked")
    private Holder<Biome> releaseBiome(int era,int x,int z,int cx,int cz){
        ReleaseSample sample=lastRelease.get();
        if(sample==null||sample.x()!=cx||sample.z()!=cz||sample.era()!=era){
            Holder<Biome>[] grid=(Holder<Biome>[])new Holder<?>[16];
            var old=era==5?null:new com.seedfinding.mcbiome.source.OverworldBiomeSource(
                com.seedfinding.mccore.version.MCVersion.fromString(era==6?firstRelease:GenerationBases.definition(era).reference()),seed);
            for(int bx=0;bx<4;bx++)for(int bz=0;bz<4;bz++){
                int px=cx*16+bx*4+2,pz=cz*16+bz*4+2;
                String name=era==5?com.seedfinding.mcbiome.biome.Biomes.REGISTRY.get(adventure.blocks(px,pz,1,1)[0]).getName():old.getBiome(px,0,pz).getName();
                String key=HistoricalBiomeNames.current(name,era);
                grid[bx*4+bz]=Objects.requireNonNull(release.get(key),"Unregistered historical biome "+key);
            }
            sample=new ReleaseSample(cx,cz,era,grid);lastRelease.set(sample);
        }
        return sample.biomes()[((x&15)/4)*4+(z&15)/4];
    }
}
