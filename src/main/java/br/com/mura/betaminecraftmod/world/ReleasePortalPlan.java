package br.com.mura.betaminecraftmod.world;

import java.util.*;

/** Campaign landmark, not a claim to reproduce the original seed's natural stronghold layout. */
public record ReleasePortalPlan(int chunkX,int chunkZ) {
    public static final int FLOOR=38,PORTAL_Y=39,CEILING=44;
    public enum Kind { AIR,BRICK,LADDER_EAST,FRAME_NORTH,FRAME_SOUTH,FRAME_EAST,FRAME_WEST,TORCH }
    public record Pos(int x,int y,int z){}
    public static ReleasePortalPlan forSeed(long seed){var r=new Random(seed^0x454E445F524F4F4DL);int major=200+r.nextInt(16),minor=r.nextInt(201)-100;if(r.nextBoolean())major=-major-1;return r.nextBoolean()?new ReleasePortalPlan(major,minor):new ReleasePortalPlan(minor,major);}
    public int x(){return chunkX*16+8;}
    public int z(){return chunkZ*16+8;}
    public boolean owns(int cx,int cz){return cx==chunkX&&cz==chunkZ;}
    public boolean protectedCell(int wx,int y,int wz){int dx=wx-x(),dz=wz-z();return y>=FLOOR&&y<128&&((Math.abs(dx)<=6&&Math.abs(dz)<=6&&y<=CEILING)||(dx>=-5&&dx<=-1&&dz>=-5&&dz<=-3));}
    public List<Map.Entry<Pos,Kind>> layout(int surface){
        int entry=Math.clamp(surface,48,120);var cells=new LinkedHashMap<Pos,Kind>();
        for(int dx=-6;dx<=6;dx++)for(int dz=-6;dz<=6;dz++)for(int y=FLOOR;y<=CEILING;y++)put(cells,dx,y,dz,y==FLOOR||y==CEILING||Math.abs(dx)==6||Math.abs(dz)==6?Kind.BRICK:Kind.AIR);
        // Sealed ladder shaft with an opening into the room and a small surface entrance.
        for(int dx=-5;dx<=-3;dx++)for(int dz=-5;dz<=-3;dz++)for(int y=PORTAL_Y;y<=entry+2;y++)put(cells,dx,y,dz,y==entry+2?Kind.BRICK:dx==-4&&dz==-4?Kind.LADDER_EAST:Kind.BRICK);
        for(int y:new int[]{PORTAL_Y,PORTAL_Y+1,entry,entry+1})put(cells,-3,y,-4,Kind.AIR);
        for(int dx=-2;dx<=-1;dx++)for(int dz=-5;dz<=-3;dz++){put(cells,dx,entry-1,dz,Kind.BRICK);put(cells,dx,entry,dz,Kind.AIR);put(cells,dx,entry+1,dz,Kind.AIR);}
        for(int offset=-1;offset<=1;offset++){put(cells,offset,PORTAL_Y,-2,Kind.FRAME_SOUTH);put(cells,offset,PORTAL_Y,2,Kind.FRAME_NORTH);put(cells,-2,PORTAL_Y,offset,Kind.FRAME_EAST);put(cells,2,PORTAL_Y,offset,Kind.FRAME_WEST);}
        for(int[] p:new int[][]{{4,4},{4,-4},{-4,4}})put(cells,p[0],PORTAL_Y,p[1],Kind.TORCH);
        put(cells,-2,entry,-3,Kind.TORCH);
        return List.copyOf(cells.entrySet());
    }
    private void put(Map<Pos,Kind> cells,int dx,int y,int dz,Kind kind){cells.put(new Pos(x()+dx,y,z()+dz),kind);}
}
