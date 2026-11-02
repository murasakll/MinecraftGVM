package br.com.mura.betaminecraftmod.world;

import java.util.List;
import java.util.Random;

/** Reference counts and climate rules. Candidate placement streams are explicitly mod-defined. */
public final class HalloweenPopulationSettings {
    public record Ore(String block,int attempts,int size,int ceiling) { }
    public static final List<Ore> ORES=List.of(
        new Ore("clay",10,32,128),new Ore("dirt",20,32,128),new Ore("gravel",10,32,128),
        new Ore("coal_ore",20,16,128),new Ore("iron_ore",20,8,64),new Ore("gold_ore",2,8,32),
        new Ore("redstone_ore",8,7,16),new Ore("diamond_ore",1,7,16));
    private HalloweenPopulationSettings() { }
    public static int trees(HalloweenClimate.Biome biome,int noiseCount,boolean extra) {
        int count=extra?1:0;
        count+=switch(biome){case FOREST,RAINFOREST,TAIGA->noiseCount+5;case SEASONAL_FOREST->noiseCount+2;
            case DESERT,TUNDRA,PLAINS->-20;default->0;};
        return Math.max(0,count);
    }
    public static boolean snow(double temperature,int surfaceY) {
        return surfaceY>0&&surfaceY<128&&temperature-(surfaceY-64)/64.0*.3<.5;
    }
    public static Random stream(long seed,int cx,int cz,int feature) {
        var random=new Random(seed);
        long x=random.nextLong()/2*2+1,z=random.nextLong()/2*2+1;
        // Independent channels keep unfinished features from shifting completed ones.
        return new Random(((long)cx*x+(long)cz*z^seed)^0x9E3779B97F4A7C15L*(feature+1L));
    }
}
