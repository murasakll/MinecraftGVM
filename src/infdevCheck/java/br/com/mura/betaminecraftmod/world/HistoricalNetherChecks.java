package br.com.mura.betaminecraftmod.world;

import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

final class HistoricalNetherChecks {
    static void run()throws Exception {
        int count=0;
        for(int era=3;era<=6;era++) for(var line:Files.readAllLines(Path.of(era==3?"docs/reference/nether_alpha.tsv":"docs/reference/nether_era_"+era+".tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;
            var fields=line.split(" ");long seed=Long.parseLong(fields[0]);int x=Integer.parseInt(fields[1]),z=Integer.parseInt(fields[2]);
            var terrain=new HistoricalNetherTerrain(seed,era);
            byte[][] samples={terrain.generateBase(x,z),terrain.generateSurface(x,z),terrain.generate(x,z)};
            for(int phase=0;phase<3;phase++)if(!hash(samples[phase]).equals(fields[3+phase]))throw new AssertionError("Nether differs from original phase "+phase+" seed "+seed);
            terrain.generate(x+1,z-1);if(!Arrays.equals(samples[2],terrain.generate(x,z)))throw new AssertionError("Nether depends on request order");
            for(int bx=0;bx<16;bx++)for(int bz=0;bz<16;bz++)if(samples[2][InfdevTerrain.index(bx,0,bz)]!=7||samples[2][InfdevTerrain.index(bx,127,bz)]!=7)throw new AssertionError("Missing Nether roof/floor");
            for(byte block:samples[2])if(!Set.of(0,7,11,13,87,88).contains(Byte.toUnsignedInt(block)))throw new AssertionError("Future block in Halloween Nether");
            count++;
        }
        if(count!=80)throw new AssertionError("Missing Nether reference samples");
        System.out.println("HISTORICAL_NETHER SUCCESS: 80 original Alpha/Beta/1.0 density/surface/cave snapshots, bedrock roof/floor and seed/order independence");
    }
    private static String hash(byte[] data)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));}
}
