package br.com.mura.betaminecraftmod.world;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

/** Original central-island terrain adapter. Fight/structures are audited separately. */
public final class HistoricalEndChunkGenerator extends HistoricalDimensionGenerator {
    public static final MapCodec<HistoricalEndChunkGenerator> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
        Biome.CODEC.fieldOf("biome").forGetter(g->g.biome),Codec.LONG.fieldOf("seed").forGetter(g->g.seed)).apply(i,HistoricalEndChunkGenerator::new));
    public HistoricalEndChunkGenerator(Holder<Biome> biome,long seed){super(biome,seed,new HistoricalEndTerrain(seed)::generate);}
    @Override protected MapCodec<? extends net.minecraft.world.level.chunk.ChunkGenerator> codec(){return CODEC;}
    @Override public int getSeaLevel(){return 0;}
    @Override public boolean locked(int x,int z){return era<6||x< -32||x>=32||z< -32||z>=32;}
    @Override public void applyBiomeDecoration(net.minecraft.world.level.WorldGenLevel level,net.minecraft.world.level.chunk.ChunkAccess chunk,net.minecraft.world.level.StructureManager structures){new HistoricalEndPopulation(seed,this).populate(level,chunk.getPos().x(),chunk.getPos().z());}
}
