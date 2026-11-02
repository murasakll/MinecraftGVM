package br.com.mura.betaminecraftmod.world;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

/** Original Material solidity; modern collision geometry is not a substitute. */
public final class HistoricalPortalMaterials {
    private static final Map<String,Boolean> FACTS=load();
    private HistoricalPortalMaterials(){}
    public static boolean solid(int era,BlockState state){return FACTS.getOrDefault(era+"/"+BuiltInRegistries.BLOCK.getKey(state.getBlock()),false);}
    private static Map<String,Boolean> load(){
        var result=new HashMap<String,Boolean>();
        try(var input=HistoricalPortalMaterials.class.getResourceAsStream("/data/betaminecraftmod/historical_portals/materials.tsv")){
            if(input==null)throw new IllegalStateException("Missing original portal materials");
            try(var reader=new BufferedReader(new InputStreamReader(input,StandardCharsets.UTF_8))){
                for(String line;(line=reader.readLine())!=null;){if(line.startsWith("#")||line.isBlank())continue;var f=line.split(" ");if(f.length!=4||!Set.of("true","false").contains(f[3]))throw new IllegalStateException("Invalid portal material row");if(result.putIfAbsent(f[0]+"/"+f[2],Boolean.parseBoolean(f[3]))!=null)throw new IllegalStateException("Duplicate portal material");}
            }
        }catch(IOException error){throw new IllegalStateException("Cannot read original portal materials",error);}
        if(result.size()!=484)throw new IllegalStateException("Incomplete original portal materials");return Map.copyOf(result);
    }
}
