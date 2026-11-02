package br.com.mura.betaminecraftmod.world;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

final class HistoricalLargeOakChecks {
    static void run()throws Exception {
        int count=0;
        for(var line:Files.readAllLines(Path.of("docs/reference/large_oak.tsv"))) {
            if(line.startsWith("#")||line.isBlank())continue;
            var fields=line.split(" ");long seed=Long.parseLong(fields[0]);int coordinate=Integer.parseInt(fields[1]);
            var plan=new HistoricalLargeOak().plan(p->p.y()<63?HistoricalLargeOak.Cell.SOLID:p.y()==63?HistoricalLargeOak.Cell.GRASS:HistoricalLargeOak.Cell.AIR,new Random(seed),coordinate,64,coordinate);
            var sorted=new TreeMap<String,Integer>();plan.forEach((p,part)->sorted.put(p.x()+","+p.y()+","+p.z(),part==HistoricalLargeOak.Part.LOG?17:18));
            String serialized=sorted.entrySet().stream().map(e->e.getKey()+"="+e.getValue()+"\n").collect(java.util.stream.Collectors.joining());
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(serialized.getBytes(StandardCharsets.UTF_8)));
            if(!hash.equals(fields[2]))throw new AssertionError("Historical large oak differs: "+seed+" at "+coordinate);
            if(plan.values().stream().noneMatch(p->p==HistoricalLargeOak.Part.LOG)||plan.values().stream().noneMatch(p->p==HistoricalLargeOak.Part.LEAVES))throw new AssertionError("Incomplete large oak");
            count++;
        }
        if(count!=15)throw new AssertionError("Missing original large oak layouts");
        var blocked=new HistoricalLargeOak().plan(p->HistoricalLargeOak.Cell.SOLID,new Random(0),0,64,0);
        if(!blocked.isEmpty())throw new AssertionError("Large oak grew on stone");
        System.out.println("HISTORICAL_LARGE_OAK SUCCESS: 15 original block layouts, negative coordinates, signed/extreme seeds and substrate rejection");
    }
}
