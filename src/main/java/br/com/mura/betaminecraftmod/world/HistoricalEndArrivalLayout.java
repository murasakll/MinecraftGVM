package br.com.mura.betaminecraftmod.world;

import java.util.*;

/** Teleporter 1.0 End branch: floor of incoming position, lower by one, reset pitch/speed. */
public final class HistoricalEndArrivalLayout {
    public record Plan(Map<HistoricalEndSpike.Pos,Integer> blocks,int x,int y,int z) { }
    private HistoricalEndArrivalLayout() { }
    public static Plan plan(double x,double y,double z) {
        int px=(int)Math.floor(x),py=(int)Math.floor(y)-1,pz=(int)Math.floor(z);var blocks=new LinkedHashMap<HistoricalEndSpike.Pos,Integer>();
        // The reference walks Z from +2 to -2, X from -2 to +2, then the four vertical cells.
        for(int dz=2;dz>=-2;dz--)for(int dx=-2;dx<=2;dx++)for(int dy=-1;dy<=2;dy++)blocks.put(new HistoricalEndSpike.Pos(px+dx,py+dy,pz+dz),dy==-1?49:0);
        return new Plan(Collections.unmodifiableMap(blocks),px,py,pz);
    }
    public static Plan spawn(){return plan(100,50,0);}
}
