package br.com.mura.betaminecraftmod.world;

import java.util.*;
import static br.com.mura.betaminecraftmod.world.HistoricalLargeOak.*;

/** Small-tree shapes from the Beta 1.7.3 reference, separated from world mutation. */
public final class HistoricalSmallTree {
    public enum Kind { OAK, BIRCH, PINE, SPRUCE }
    public enum Part { DIRT, LEAVES, LOG }
    public static Map<Point,Part> plan(Ground ground,Random random,int x,int y,int z,Kind kind) {
        boolean conifer=kind==Kind.PINE||kind==Kind.SPRUCE;
        int height,stem=0,radius=2;
        if(kind==Kind.PINE){height=7+random.nextInt(5);stem=height-random.nextInt(2)-3;radius=1+random.nextInt(height-stem+1);}
        else if(kind==Kind.SPRUCE){height=6+random.nextInt(4);stem=1+random.nextInt(2);radius=2+random.nextInt(2);}
        else height=(kind==Kind.BIRCH?5:4)+random.nextInt(3);
        if(y<1||y>=128-height-1)return Map.of();
        var soil=ground.at(new Point(x,y-1,z));if(soil!=Cell.GRASS&&soil!=Cell.DIRT)return Map.of();
        for(int level=0;level<=height+1;level++) {
            int width=conifer?(level<stem?0:radius):level==0?0:level>=height-1?2:1;
            for(int dx=-width;dx<=width;dx++)for(int dz=-width;dz<=width;dz++) {
                var cell=ground.at(new Point(x+dx,y+level,z+dz));if(cell!=Cell.AIR&&cell!=Cell.LEAVES)return Map.of();
            }
        }
        var result=new LinkedHashMap<Point,Part>();result.put(new Point(x,y-1,z),Part.DIRT);
        if(!conifer) {
            for(int level=height-3;level<=height;level++) {
                int offset=level-height,width=1-offset/2;
                for(int dx=-width;dx<=width;dx++)for(int dz=-width;dz<=width;dz++) {
                    boolean corner=Math.abs(dx)==width&&Math.abs(dz)==width;
                    if(corner&&!(random.nextInt(2)!=0&&offset!=0))continue;
                    leaves(result,ground,new Point(x+dx,y+level,z+dz));
                }
            }
        }else if(kind==Kind.PINE) {
            int width=0;
            for(int level=height;level>=stem;level--) {
                disc(result,ground,x,y+level,z,width);
                if(width>=1&&level==stem+1)width--;else if(width<radius)width++;
            }
        }else {
            int width=random.nextInt(2),threshold=1,reset=0;
            for(int offset=0;offset<=height-stem;offset++) {
                disc(result,ground,x,y+height-offset,z,width);
                if(width>=threshold){width=reset;reset=1;threshold=Math.min(radius,threshold+1);}else width++;
            }
        }
        int trunk=kind==Kind.PINE?height-1:kind==Kind.SPRUCE?height-random.nextInt(3):height;
        for(int level=0;level<trunk;level++)result.put(new Point(x,y+level,z),Part.LOG);
        return Collections.unmodifiableMap(result);
    }
    private static void disc(Map<Point,Part> plan,Ground ground,int x,int y,int z,int radius) {
        for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)
            if(radius==0||Math.abs(dx)!=radius||Math.abs(dz)!=radius)leaves(plan,ground,new Point(x+dx,y,z+dz));
    }
    private static void leaves(Map<Point,Part> plan,Ground ground,Point point) {
        var cell=ground.at(point);if(cell==Cell.AIR||cell==Cell.LEAVES)plan.put(point,Part.LEAVES);
    }
    private HistoricalSmallTree() { }
}
