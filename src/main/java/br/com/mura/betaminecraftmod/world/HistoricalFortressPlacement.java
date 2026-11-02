package br.com.mura.betaminecraftmod.world;

import java.util.Random;

/** Original 1.0 region test: one attempt per 16x16 chunks, with a one-in-three region chance. */
public final class HistoricalFortressPlacement {
    public record Start(int x,int z,long regionSeed) { }
    private HistoricalFortressPlacement() { }
    public static Start inRegion(long seed,int rx,int rz) {
        long regionSeed=(long)(rx^(rz<<4))^seed;
        var random=new Random(regionSeed);random.nextInt();
        if(random.nextInt(3)!=0)return null;
        return new Start((rx<<4)+4+random.nextInt(8),(rz<<4)+4+random.nextInt(8),regionSeed);
    }
}
