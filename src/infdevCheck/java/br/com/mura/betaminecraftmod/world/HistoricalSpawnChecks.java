package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.HistoricalSpawnTables;
import java.util.*;

final class HistoricalSpawnChecks {
    static void run() {
        for(int era=3;era<=6;era++)for(String dimension:List.of("minecraft:overworld","minecraft:the_nether","minecraft:the_end"))for(String biome:List.of("halloween/forest","halloween/taiga","desert","mushroom_fields"))for(String category:List.of("monster","creature","water_creature","ambient")) {
            var pool=HistoricalSpawnTables.pool(era,dimension,biome,category);var unique=new HashSet<String>();
            for(var entry:pool)if(entry.weight()<=0||entry.min()<1||entry.max()<entry.min()||!unique.add(entry.entity()))throw new AssertionError("Invalid historical spawn pool");
            if(category.equals("ambient")&&!pool.isEmpty())throw new AssertionError("Bats before 1.4");
            if(dimension.equals("minecraft:the_end")&&era<6&&!pool.isEmpty())throw new AssertionError("End before release");
            if(era<6&&pool.stream().anyMatch(e->e.entity().equals("magma_cube")||e.entity().equals("mooshroom")))throw new AssertionError("Release mob before release");
        }
        var hell=HistoricalSpawnTables.pool(6,"minecraft:the_nether","nether_wastes","monster");
        if(hell.size()!=3||hell.get(0).weight()!=50||hell.get(1).weight()!=100||hell.get(2).weight()!=1)throw new AssertionError("Release Nether weights changed");
        if(HistoricalSpawnTables.pool(3,"minecraft:overworld","halloween/forest","monster").size()!=4)throw new AssertionError("Halloween monster pool changed");
        System.out.println("HISTORICAL_SPAWNS SUCCESS: era/dimension/biome pools, original weight boundaries and no future ambient mobs");
    }
}
