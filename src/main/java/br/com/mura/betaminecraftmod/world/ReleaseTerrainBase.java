package br.com.mura.betaminecraftmod.world;

import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.terrain.OverworldTerrainGenerator;
import java.util.*;

/** Bounded, per-request historical engine. No engine cache survives a chunk request. */
public final class ReleaseTerrainBase {
    private final long seed;
    private final MCVersion version;
    public ReleaseTerrainBase(long seed,MCVersion version){
        if(version.isOlderThan(MCVersion.v1_0)||version.isNewerThan(MCVersion.v1_17_1))throw new IllegalArgumentException("Unsupported historical release");
        this.seed=seed;this.version=version;
    }
    public TerrainVolume generate(int cx,int cz){
        if(version.isOlderThan(MCVersion.v1_7_2)){
            return TerrainVolume.legacy(new EarlyReleaseTerrain(seed,version).generate(cx,cz),version.isOlderThan(MCVersion.v1_2_1)?128:256);
        }
        var generator=new OverworldTerrainGenerator(new OverworldBiomeSource(version,seed));
        // The upstream bedrock pass mutates a whole chunk; call it once, then read its cache.
        generator.getBedrockColumnAt(cx*16+15,cz*16+15);
        var palette=new ArrayList<String>();var lookup=new LinkedHashMap<String,Short>();short[] blocks=new short[65536];
        for(int x=0;x<16;x++)for(int z=0;z<16;z++){
            var column=generator.getBiomeColumnAt(cx*16+x,cz*16+z);
            for(int y=0;y<256;y++){
                String name=column[y].getRegistryName();if(!name.contains(":"))name="minecraft:"+name;
                Short id=lookup.get(name);if(id==null){id=(short)palette.size();palette.add(name);lookup.put(name,id);}blocks[(x*16+z)*256+y]=id;
            }
        }
        return ReleaseCarvers.carve(new TerrainVolume(0,256,palette,blocks),seed,version,cx,cz);
    }
    public String biome(int x,int z){return new OverworldBiomeSource(version,seed).getBiome(x,0,z).getName();}
}
