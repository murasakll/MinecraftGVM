package br.com.mura.betaminecraftmod.world;

import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

final class HistoricalEndChecks {
    static void run()throws Exception {
        int count=0;
        for(var line:Files.readAllLines(Path.of("docs/reference/end_release_1_0.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;
            var fields=line.split(" ");long seed=Long.parseLong(fields[0]);int x=Integer.parseInt(fields[1]),z=Integer.parseInt(fields[2]);
            var terrain=new HistoricalEndTerrain(seed);var blocks=terrain.generate(x,z);
            var hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(blocks));
            if(!hash.equals(fields[3]))throw new AssertionError("End differs from original "+seed+" at "+x+","+z);
            terrain.generate(x+1,z-1);if(!Arrays.equals(blocks,terrain.generate(x,z)))throw new AssertionError("End request order changed blocks");
            for(byte block:blocks)if(block!=0&&Byte.toUnsignedInt(block)!=121)throw new AssertionError("Future block in release End");
            count++;
        }
        if(count!=20)throw new AssertionError("Missing release End references");
        System.out.println("HISTORICAL_END SUCCESS: 20 original release 1.0 central-island snapshots, no later blocks and request-order independence");
        int spikes=0;
        for(var line:Files.readAllLines(Path.of("docs/reference/end_spikes.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;
            var fields=line.split(" ");long seed=Long.parseLong(fields[0]);int coordinate=Integer.parseInt(fields[1]),ground=Integer.parseInt(fields[2]);
            var plan=HistoricalEndSpike.plan(p->p.y()<=ground?121:0,new Random(seed),coordinate,ground+1,coordinate);
            if(plan==null)throw new AssertionError("Supported spike rejected");
            var blocks=new TreeMap<String,Integer>();plan.blocks().forEach((p,id)->{if(p.y()<0||p.y()>=128||id!=49&&id!=7)throw new AssertionError("Future spike palette/height");blocks.put(p.x()+","+p.y()+","+p.z(),id);});
            var serialized=blocks.entrySet().stream().map(e->e.getKey()+"="+e.getValue()+"\n").collect(java.util.stream.Collectors.joining())+plan.crystalX()+","+plan.crystalY()+","+plan.crystalZ()+","+plan.yaw();
            var hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(serialized.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            if(!hash.equals(fields[3]))throw new AssertionError("Original End spike/crystal differs");spikes++;
        }
        if(spikes!=30)throw new AssertionError("Missing original spike fixtures");
        if(HistoricalEndSpike.plan(p->0,new Random(0),0,64,0)!=null||HistoricalEndSpike.plan(p->p.y()==63&&p.x()==0&&p.z()==0?121:0,new Random(0),0,64,0)!=null)throw new AssertionError("Unsupported spike accepted");
        System.out.println("HISTORICAL_END_SPIKES SUCCESS: 30 original layouts/crystals, clipped height and unsupported-ground rejection");
        int podiums=0;
        for(var line:Files.readAllLines(Path.of("docs/reference/end_podium.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;
            var fields=line.split(" ");var blocks=new TreeMap<String,Integer>();
            HistoricalEndPodiumLayout.plan(Integer.parseInt(fields[0]),Integer.parseInt(fields[1])).forEach((p,id)->blocks.put(p.x()+","+p.y()+","+p.z(),id));
            var serialized=blocks.entrySet().stream().map(e->e.getKey()+"="+e.getValue()+"\n").collect(java.util.stream.Collectors.joining());
            var hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(serialized.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            if(!hash.equals(fields[2]))throw new AssertionError("Original death portal differs");podiums++;
        }
        if(podiums!=4)throw new AssertionError("Missing original death portal references");
        System.out.println("HISTORICAL_END_PODIUM SUCCESS: four original death portals, fixed height, egg, torches and air clearance");
    }
}
