package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.EraCatalog;
import br.com.mura.betaminecraftmod.progression.GenerationRouter;
import java.util.*;

/** Generation backends are independent of chapter quests, content, sound and visual readiness. */
public final class TerrainBackends {
    public enum State { VERIFIED_BASE, PLANNED }
    public enum Climate { NONE, ALPHA_SIMPLEX, INTEGER_LAYERS, PENDING }
    public record Bounds(int minY,int height,int seaLevel) { }
    public record Definition(int id,GenerationRouter.Era era,String reference,State state,Climate climate,Optional<Bounds> bounds) { }
    @FunctionalInterface public interface Sampler { byte[] generate(int chunkX,int chunkZ); }
    private static final List<Definition> DEFINITIONS=EraCatalog.all().stream().map(era->new Definition(
        era.id(),era.era(),era.id()==3?"Alpha 1.2.6":era.id()==4?"Beta 1.7.3":era.id()==5?"Beta 1.8.1":era.reference(),era.id()<=5?State.VERIFIED_BASE:State.PLANNED,
        era.id()<=2?Climate.NONE:era.id()<=4?Climate.ALPHA_SIMPLEX:era.id()==5?Climate.INTEGER_LAYERS:Climate.PENDING,
        era.id()<=5?Optional.of(new Bounds(0,128,era.id()==5?63:64)):Optional.empty())).toList();
    public static List<Definition> all(){return DEFINITIONS;}
    public static Definition definition(int era){
        if(era<0||era>=DEFINITIONS.size())throw new IllegalArgumentException("Unknown terrain era: "+era);
        return DEFINITIONS.get(era);
    }
    private final long seed;
    private final Map<Integer,Sampler> samplers=new HashMap<>();
    private final InfdevTerrain[] legacy=new InfdevTerrain[3];
    public TerrainBackends(long seed){this.seed=seed;}
    public synchronized Sampler sampler(int era){
        if(definition(era).state()!=State.VERIFIED_BASE)throw new IllegalStateException("Historical terrain base is pending: "+definition(era).reference());
        return samplers.computeIfAbsent(era,id->id<=2?legacy(id)::generate:id==3?new HalloweenTerrain(seed)::generate:id==4?new BetaTerrain(seed)::generate:new AdventureTerrain(seed)::generate);
    }
    synchronized InfdevTerrain legacy(int era){
        if(era<0||era>=legacy.length)throw new IllegalArgumentException("No Infdev decorator for era "+era);
        if(legacy[era]==null)legacy[era]=new InfdevTerrain(seed,era);
        return legacy[era];
    }
}
