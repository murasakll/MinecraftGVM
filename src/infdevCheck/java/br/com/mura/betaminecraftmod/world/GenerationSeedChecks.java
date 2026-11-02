package br.com.mura.betaminecraftmod.world;

import java.util.*;
import java.util.concurrent.*;

/** Exercises the signed long seed API, shared instances and independent requests. */
final class GenerationSeedChecks {
    static void run() throws Exception {
        long[] seeds={0,1,-1,Long.MIN_VALUE,Long.MAX_VALUE};
        var distinct=new ArrayList<Set<String>>();for(int era=0;era<13;era++)distinct.add(new HashSet<>());
        try(var pool=Executors.newFixedThreadPool(4)){
            for(long seed:seeds){
                var shared=new GenerationBases(seed);
                for(int era=0;era<13;era++){
                    final int id=era;
                    TerrainVolume expected=shared.generate(id,-7,12,null);
                    String hash=ReleaseBaseChecks.hash(expected);distinct.get(id).add(hash);
                    var first=pool.submit(()->ReleaseBaseChecks.hash(shared.generate(id,-7,12,null)));
                    var other=pool.submit(()->shared.generate(id,1,-1,null));
                    var second=pool.submit(()->ReleaseBaseChecks.hash(shared.generate(id,-7,12,null)));
                    if(!hash.equals(first.get())||!hash.equals(second.get()))throw new AssertionError("Concurrent generation changes era "+id+", seed "+seed);
                    other.get();
                    var definition=GenerationBases.definition(id);
                    if(expected.minY()!=definition.minY()||expected.height()!=definition.height())throw new AssertionError("Seed changed era height");
                }
            }
        }
        for(int era=0;era<13;era++)if(distinct.get(era).size()<2)throw new AssertionError("Era ignores seed: "+era);
        System.out.println("GENERATION_SEEDS SUCCESS: 13 historical bases, five signed/extreme seeds, concurrent repeatability and independent chunk requests");
    }
}
