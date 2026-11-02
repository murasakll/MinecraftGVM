package br.com.mura.betaminecraftmod.world;

/** Alpha 1.2.6 climate foundation, kept independent of Minecraft registries and live world state. */
public final class HalloweenClimate {
    public enum Biome {RAINFOREST,SWAMPLAND,SEASONAL_FOREST,FOREST,SAVANNA,SHRUBLAND,TAIGA,DESERT,PLAINS,TUNDRA}
    public record Grid(int width,int depth,double[] temperature,double[] rainfall,Biome[] biomes) { }
    private final LegacySimplex.Octaves temperature,rainfall,variation;
    public HalloweenClimate(long seed){
        temperature=new LegacySimplex.Octaves(seed*9871,4);
        rainfall=new LegacySimplex.Octaves(seed*39811,4);
        variation=new LegacySimplex.Octaves(seed*543321,2);
    }
    public Grid sample(int x,int z,int width,int depth){
        if(width<1||depth<1)throw new IllegalArgumentException("Climate grid must have positive dimensions");
        double[] t=temperature.grid(x,z,width,depth,.025F,.25),r=rainfall.grid(x,z,width,depth,.05F,1.0/3),v=variation.grid(x,z,width,depth,.25,1/1.7);
        Biome[] biomes=new Biome[t.length];
        for(int i=0;i<t.length;i++){
            double detail=v[i]*1.1+.5;
            double heat=(t[i]*.15+.7)*.99+detail*.01;
            t[i]=Math.clamp(1-(1-heat)*(1-heat),0,1);
            r[i]=Math.clamp((r[i]*.15+.5)*.998+detail*.002,0,1);
            // Original climate selection quantizes through a 64x64 float lookup table.
            biomes[i]=classify((int)(t[i]*63)/63f,(int)(r[i]*63)/63f);
        }
        return new Grid(width,depth,t,r,biomes);
    }
    private static Biome classify(float heat,float wet){
        wet*=heat;
        if(heat<.1F)return Biome.TUNDRA;
        if(wet<.2F)return heat<.5F?Biome.TUNDRA:heat<.95F?Biome.SAVANNA:Biome.DESERT;
        if(wet>.5F&&heat<.7F)return Biome.SWAMPLAND;
        if(heat<.5F)return Biome.TAIGA;
        if(heat<.97F)return wet<.35F?Biome.SHRUBLAND:Biome.FOREST;
        return wet<.45F?Biome.PLAINS:wet<.9F?Biome.SEASONAL_FOREST:Biome.RAINFOREST;
    }
}
