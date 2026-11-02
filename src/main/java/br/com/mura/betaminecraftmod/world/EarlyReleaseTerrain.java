package br.com.mura.betaminecraftmod.world;

import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.version.MCVersion;

/** 1.0--1.6 family: old 128-high density in a version-appropriate build container. */
public final class EarlyReleaseTerrain extends AdventureTerrain {
    private final OverworldBiomeSource source;
    public EarlyReleaseTerrain(long seed, MCVersion version) {
        super(seed);
        if(version.isOlderThan(MCVersion.v1_0)||version.isNewerThan(MCVersion.v1_6_4))throw new IllegalArgumentException("Not early release");
        source=new OverworldBiomeSource(version,seed);
    }
    @Override protected int[] biomeGrid(int x,int z,int w,int h,boolean coarse){
        int[] out=new int[w*h];
        for(int dz=0;dz<h;dz++)for(int dx=0;dx<w;dx++)out[dx+dz*w]=coarse?source.full.get(x+dx,0,z+dz):source.voronoi.get(x+dx,0,z+dz);
        return out;
    }
    @Override protected float base(int id){return switch(id){case 0,10 -> -1;case 6 -> -.2f;case 7,11 -> -.5f;case 3 -> .2f;case 13 -> .2f;case 14 -> .2f;case 15 -> -1;case 17,18,19 -> .2f;case 21 -> .2f;case 22 -> 1.8f;case 16 -> 0;default -> .1f;};}
    @Override protected float scale(int id){return switch(id){case 0,10 -> .4f;case 3 -> 1.3f;case 6 -> .1f;case 7,11 -> 0;case 15,16 -> .1f;case 2 -> .2f;case 4 -> .3f;case 5 -> .4f;case 13 -> 1.2f;case 14 -> 1;case 18,19 -> .7f;case 21 -> .4f;case 22 -> .2f;default -> .3f;};}
    @Override protected int top(int id){return id==2||id==16||id==17?12:id==14||id==15?110:2;}
    @Override protected int filler(int id){return id==2||id==16||id==17?12:3;}
    @Override protected int fluid(int id){return id==10||id==11||id==12||id==13?79:9;}
}
