package br.com.mura.betaminecraftmod.world;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.ToIntFunction;

/** Release 1.0 spike geometry. Placement scheduling is a separate concern. */
public final class HistoricalEndSpike {
    public record Pos(int x,int y,int z) { }
    public record Plan(Map<Pos,Integer> blocks,double crystalX,double crystalY,double crystalZ,float yaw) { }
    private HistoricalEndSpike() { }

    public static Plan plan(ToIntFunction<Pos> terrain,Random random,int x,int y,int z) {
        if(terrain.applyAsInt(new Pos(x,y,z))!=0||terrain.applyAsInt(new Pos(x,y-1,z))!=121)return null;
        int height=6+random.nextInt(32),radius=1+random.nextInt(4);
        var footprint=new java.util.ArrayList<Pos>();
        for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++) {
            if(dx*dx+dz*dz>radius*radius+1)continue;
            if(terrain.applyAsInt(new Pos(x+dx,y-1,z+dz))!=121)return null;
            footprint.add(new Pos(x+dx,y,z+dz));
        }
        var blocks=new LinkedHashMap<Pos,Integer>();
        for(int level=y;level<Math.min(128,y+height);level++)for(var column:footprint)blocks.put(new Pos(column.x(),level,column.z()),49);
        if(y+height<128)blocks.put(new Pos(x,y+height,z),7);
        return new Plan(Map.copyOf(blocks),(float)x+.5f,y+height,(float)z+.5f,random.nextFloat()*360.0f);
    }
}
