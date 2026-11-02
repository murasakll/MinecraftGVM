package br.com.mura.betaminecraftmod.world;

import java.nio.file.*;

final class HistoricalFortressChecks {
    static void run()throws Exception {
        int count=0;
        for(var line:Files.readAllLines(Path.of("docs/reference/fortress_positions.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;
            var f=line.split(" ");var start=HistoricalFortressPlacement.inRegion(Long.parseLong(f[0]),Integer.parseInt(f[1]),Integer.parseInt(f[2]));
            if(f[3].equals("none")?start!=null:start==null||start.x()!=Integer.parseInt(f[3])||start.z()!=Integer.parseInt(f[4]))throw new AssertionError("Fortress start changed: "+line);count++;
        }
        if(count!=125)throw new AssertionError("Missing fortress start fixtures");
        System.out.println("HISTORICAL_FORTRESS_POSITIONS SUCCESS: 125 original regions, positive/negative coordinates and signed/extreme seeds");
    }
}
