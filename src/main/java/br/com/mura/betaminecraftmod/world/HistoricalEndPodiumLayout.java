package br.com.mura.betaminecraftmod.world;

import java.util.*;

/** Original fixed-height portal footprint, independent of Minecraft runtime. */
public final class HistoricalEndPodiumLayout {
    private HistoricalEndPodiumLayout() { }
    public static Map<HistoricalEndSpike.Pos,Integer> plan(int x,int z) {
        var result=new LinkedHashMap<HistoricalEndSpike.Pos,Integer>();
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++) {
            int distance=dx*dx+dz*dz;if(distance>12)continue;
            if(distance<=6)result.put(new HistoricalEndSpike.Pos(x+dx,63,z+dz),7);
            result.put(new HistoricalEndSpike.Pos(x+dx,64,z+dz),distance<=6?119:7);
            for(int y=65;y<=96;y++)result.put(new HistoricalEndSpike.Pos(x+dx,y,z+dz),0);
        }
        for(int y=64;y<=67;y++)result.put(new HistoricalEndSpike.Pos(x,y,z),7);
        result.put(new HistoricalEndSpike.Pos(x,68,z),122);
        for(int[] offset:new int[][]{{-1,0},{1,0},{0,-1},{0,1}})result.put(new HistoricalEndSpike.Pos(x+offset[0],66,z+offset[1]),50);
        return Collections.unmodifiableMap(result);
    }
}
