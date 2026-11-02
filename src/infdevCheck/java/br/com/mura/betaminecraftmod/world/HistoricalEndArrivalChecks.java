package br.com.mura.betaminecraftmod.world;

import java.nio.file.*;
import java.util.*;

final class HistoricalEndArrivalChecks {
    static void run()throws Exception {
        int count=0;
        for(String row:Files.readAllLines(Path.of("docs/reference/end_arrival.tsv"))) {
            if(row.startsWith("#")||row.isBlank())continue;var f=row.split(" ");var plan=HistoricalEndArrivalLayout.plan(Double.parseDouble(f[0]),Double.parseDouble(f[1]),Double.parseDouble(f[2]));
            if(plan.blocks().size()!=100||plan.blocks().values().stream().filter(id->id==49).count()!=25||plan.x()!=Integer.parseInt(f[4])||plan.y()!=Integer.parseInt(f[5])||plan.z()!=Integer.parseInt(f[6]))throw new AssertionError("Historical arrival footprint/position");
            var blocks=new TreeMap<String,Integer>();plan.blocks().forEach((p,id)->blocks.put(p.x()+","+p.y()+","+p.z(),id));String text=blocks.entrySet().stream().map(e->e.getKey()+"="+e.getValue()+"\n").collect(java.util.stream.Collectors.joining());String hash=HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)));if(!hash.equals(f[3]))throw new AssertionError("Original arrival geometry changed");count++;
            String ordered=plan.blocks().entrySet().stream().map(e->e.getKey().x()+","+e.getKey().y()+","+e.getKey().z()+"="+e.getValue()+"\n").collect(java.util.stream.Collectors.joining());String orderHash=HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(ordered.getBytes(java.nio.charset.StandardCharsets.UTF_8)));if(!orderHash.equals(f[7]))throw new AssertionError("Original platform write order changed");
        }
        var spawn=HistoricalEndArrivalLayout.spawn();if(count!=5||spawn.x()!=100||spawn.y()!=49||spawn.z()!=0)throw new AssertionError("Missing End arrival references");
        System.out.println("HISTORICAL_END_ARRIVAL SUCCESS: five original 100-block plans, floor/clearance and arrival transform");
    }
}
