package br.com.mura.betaminecraftmod.world;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

final class HistoricalSmallTreeChecks {
    static void run()throws Exception {
        int count=0;
        for(var line:Files.readAllLines(Path.of("docs/reference/small_trees.tsv"))) {
            if(line.startsWith("#")||line.isBlank())continue;
            var fields=line.split(" ");var kind=HistoricalSmallTree.Kind.valueOf(fields[0]);long seed=Long.parseLong(fields[1]);int coordinate=Integer.parseInt(fields[2]);
            var plan=HistoricalSmallTree.plan(p->p.y()<63?HistoricalLargeOak.Cell.SOLID:p.y()==63?HistoricalLargeOak.Cell.GRASS:HistoricalLargeOak.Cell.AIR,new Random(seed),coordinate,64,coordinate,kind);
            int meta=kind==HistoricalSmallTree.Kind.BIRCH?2:kind==HistoricalSmallTree.Kind.OAK?0:1;
            var sorted=new TreeMap<String,Integer>();plan.forEach((p,part)->sorted.put(p.x()+","+p.y()+","+p.z(),part==HistoricalSmallTree.Part.DIRT?48:(part==HistoricalSmallTree.Part.LOG?17:18)*16+meta));
            String serialized=sorted.entrySet().stream().map(e->e.getKey()+"="+e.getValue()+"\n").collect(java.util.stream.Collectors.joining());
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(serialized.getBytes(StandardCharsets.UTF_8)));
            if(!hash.equals(fields[3]))throw new AssertionError("Historical small tree differs: "+kind+" "+seed+" at "+coordinate);
            count++;
        }
        if(count!=60)throw new AssertionError("Missing original Beta tree layouts");
        System.out.println("HISTORICAL_SMALL_TREES SUCCESS: 60 original Beta block layouts, four forms and signed/extreme seeds");
    }
}
