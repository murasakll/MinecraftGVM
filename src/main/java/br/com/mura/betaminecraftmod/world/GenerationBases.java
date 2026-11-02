package br.com.mura.betaminecraftmod.world;

import com.seedfinding.mccore.version.MCVersion;
import java.util.*;

/** All campaign terrain bases. Fidelity is explicit and independent of chapter readiness. */
public final class GenerationBases {
    public enum Fidelity { ORIGINAL_SAMPLES, HISTORICAL_CANDIDATE, MODERN_COMPATIBILITY, NATIVE_CURRENT }
    public record Definition(int era,String reference,int minY,int height,int seaLevel,Fidelity fidelity){}
    @FunctionalInterface public interface ModernSampler { TerrainVolume generate(int era,int chunkX,int chunkZ); }
    private static final String[] RELEASES={"1.2.5","1.7.10","1.9.4","1.12.2","1.13.2","1.15.2","1.16.5"};
    private static final List<Definition> ALL=java.util.stream.IntStream.range(0,20).mapToObj(id->{
        String reference=id<6?TerrainBackends.definition(id).reference():id<13?RELEASES[id-6]:new String[]{"1.18.2","1.19.4","1.20.6","1.21.3","1.21.8","1.21.11","26.3"}[id-13];
        return new Definition(id,reference,id>=13?-64:0,id>=13?384:id>=6?256:128,id<=4?64:63,
            id<=5?Fidelity.ORIGINAL_SAMPLES:id<=12?Fidelity.HISTORICAL_CANDIDATE:id<19?Fidelity.MODERN_COMPATIBILITY:Fidelity.NATIVE_CURRENT);
    }).toList();
    public static List<Definition> all(){return ALL;}
    public static Definition definition(int era){if(era<0||era>=ALL.size())throw new IllegalArgumentException("Unknown generation era");return ALL.get(era);}
    private final long seed;
    private final String firstRelease;
    private final TerrainBackends legacy;
    public GenerationBases(long seed){this(seed,"1.2.5");}
    public GenerationBases(long seed,String firstRelease){
        validateFirstRelease(firstRelease);this.seed=seed;this.firstRelease=firstRelease;legacy=new TerrainBackends(seed);
    }
    public static void validateFirstRelease(String version){if(!java.util.Set.of("1.0","1.1","1.2.5").contains(version))throw new IllegalArgumentException("Unsupported first release milestone "+version);}
    public TerrainVolume generate(int era,int x,int z,ModernSampler modern){
        definition(era);
        if(era<=5)return TerrainVolume.legacy(legacy.sampler(era).generate(x,z),128);
        if(era<=12)return release(era==6?firstRelease:RELEASES[era-6],x,z);
        if(modern==null)throw new IllegalStateException("Modern terrain requires initialized Minecraft registries");
        return modern.generate(era,x,z);
    }
    /** Additional generation milestones inside the broader campaign chapters. */
    public TerrainVolume release(String version,int x,int z){
        MCVersion reference=MCVersion.fromString(version);
        if(reference==null)throw new IllegalArgumentException("Unknown release: "+version);
        return new ReleaseTerrainBase(seed,reference).generate(x,z);
    }
}
