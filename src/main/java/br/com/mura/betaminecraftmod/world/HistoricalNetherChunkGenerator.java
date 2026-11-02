package br.com.mura.betaminecraftmod.world;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

/** Coordinate-selected Alpha/Beta/release 1.0 terrain; future milestones remain closed. */
public final class HistoricalNetherChunkGenerator extends HistoricalDimensionGenerator {
    public static final MapCodec<HistoricalNetherChunkGenerator> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
        Biome.CODEC.fieldOf("biome").forGetter(g->g.biome),Codec.LONG.fieldOf("seed").forGetter(g->g.seed),
        Codec.INT.validate(value->value==1||value==8?DataResult.success(value):DataResult.error(()->"Nether ring scale must be 1 or 8"))
            .optionalFieldOf("ring_scale",8).forGetter(g->g.ringScale),
        Codec.BOOL.optionalFieldOf("resource_guarantee",false).forGetter(g->g.resourceGuarantee)).apply(i,HistoricalNetherChunkGenerator::new));
    private final int ringScale;
    private final boolean resourceGuarantee;
    public boolean resourceGuarantee(){return resourceGuarantee;}
    private final HistoricalFortresses fortresses;
    public HistoricalFortresses fortresses(){return fortresses;}
    public int ringScale(){return ringScale;}
    public HistoricalNetherChunkGenerator(Holder<Biome> biome,long seed){this(biome,seed,8);}
    public HistoricalNetherChunkGenerator(Holder<Biome> biome,long seed,int ringScale){this(biome,seed,ringScale,false);}
    public HistoricalNetherChunkGenerator(Holder<Biome> biome,long seed,int ringScale,boolean resourceGuarantee){super(biome,seed,sampler(seed,ringScale));this.ringScale=ringScale;this.resourceGuarantee=resourceGuarantee;this.fortresses=new HistoricalFortresses(seed,this);}
    private static java.util.function.BiFunction<Integer,Integer,byte[]> sampler(long seed,int ringScale) {
        if(ringScale!=1&&ringScale!=8)throw new IllegalArgumentException("Nether ring scale must be 1 or 8");
        var samplers=new HistoricalNetherTerrain[4];for(int i=0;i<4;i++)samplers[i]=new HistoricalNetherTerrain(seed,i+3);
        return (x,z)->{int era=band(x,z,ringScale);if(era>6)throw new IllegalStateException("Nether reference pending for era "+era);return samplers[era-3].generate(x,z);};
    }
    @Override protected MapCodec<? extends net.minecraft.world.level.chunk.ChunkGenerator> codec(){return CODEC;}
    private static int band(int x,int z,int scale){return Math.max(3,br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(Math.multiplyExact(x,scale),Math.multiplyExact(z,scale)));}
    public int bandAt(int x,int z){return band(x,z,ringScale);}
    @Override public boolean locked(int x,int z){int band=bandAt(x,z);return era<3||band>era||band>6;}
    @Override public int getSeaLevel(){return 32;}
    @Override public void applyBiomeDecoration(net.minecraft.world.level.WorldGenLevel level,net.minecraft.world.level.chunk.ChunkAccess chunk,net.minecraft.world.level.StructureManager structures){fortresses.populate(level,chunk,structures);new HistoricalNetherPopulation(seed,this,bandAt(chunk.getPos().x(),chunk.getPos().z())).populate(level,chunk.getPos().x(),chunk.getPos().z());}
}
