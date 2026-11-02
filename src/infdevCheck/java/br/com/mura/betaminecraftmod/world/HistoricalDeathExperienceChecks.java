package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.HistoricalDeathExperienceRules;
import java.nio.file.*;

final class HistoricalDeathExperienceChecks {
    static void run()throws Exception {
        int rewards=0,values=0;
        for(String line:Files.readAllLines(Path.of("docs/reference/death_experience.tsv"))){if(line.startsWith("#")||line.isBlank())continue;var f=line.split(" ");if(HistoricalDeathExperienceRules.reward(Integer.parseInt(f[0]),Integer.parseInt(f[1]),Integer.parseInt(f[2]))!=Integer.parseInt(f[3]))throw new AssertionError("Original death reward differs: "+line);rewards++;}
        var denominations=new java.util.HashMap<Integer,Integer>();
        for(String line:Files.readAllLines(Path.of("docs/reference/experience_orb_values.tsv"))){if(line.startsWith("#")||line.isBlank())continue;var f=line.split(" ");int remaining=Integer.parseInt(f[1]),value=Integer.parseInt(f[2]);var previous=denominations.putIfAbsent(remaining,value);if(previous!=null&&previous!=value)throw new AssertionError("Historical orb values changed between references: "+line);values++;}
        if(rewards!=1620||values!=10002||HistoricalDeathExperienceRules.ticks(5)!=21||HistoricalDeathExperienceRules.ticks(6)!=20||HistoricalDeathExperienceRules.audited(6,"1.2.5"))throw new AssertionError("Death XP scope/timing/reference count");
        System.out.println("HISTORICAL_DEATH_EXPERIENCE SUCCESS: 1620 original rewards, 10002 historical orb values, Beta/release clocks and scope");
    }
}
