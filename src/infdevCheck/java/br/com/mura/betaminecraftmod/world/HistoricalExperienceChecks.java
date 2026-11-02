package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.HistoricalExperienceRules;
import java.nio.file.*;

final class HistoricalExperienceChecks {
    static void run()throws Exception {
        int cases=0;
        for(String line:Files.readAllLines(Path.of("docs/reference/historical_experience.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;var f=line.split(" ");int era=Integer.parseInt(f[0]),level=Integer.parseInt(f[1]);
            int points=era==5?Integer.parseInt(f[2]):0;float progress=era==6?Float.valueOf(f[2]):points/(float)HistoricalExperienceRules.needed(5,level);
            var after=HistoricalExperienceRules.gain(era,new HistoricalExperienceRules.State(level,points,progress,0,0),Integer.parseInt(f[3]));
            boolean remainder=era==5?after.points()==Integer.parseInt(f[5]):Float.floatToIntBits(after.progress())==Float.floatToIntBits(Float.valueOf(f[5]));
            if(after.level()!=Integer.parseInt(f[4])||!remainder||after.total()!=Integer.parseInt(f[6])||after.score()!=Integer.parseInt(f[7]))throw new AssertionError("Original XP differs: "+line);cases++;
        }
        if(cases!=4860)throw new AssertionError("Incomplete XP reference");
        for(String line:Files.readAllLines(Path.of("docs/reference/historical_experience_needs.tsv"))){if(line.startsWith("#")||line.isBlank())continue;var f=line.split(" ");if(HistoricalExperienceRules.needed(Integer.parseInt(f[0]),Integer.parseInt(f[1]))!=Integer.parseInt(f[2]))throw new AssertionError("Original XP threshold differs");}
        int previousEra=-1,previousSequence=-1,steps=0;var state=new HistoricalExperienceRules.State(0,0,0,0,0);
        for(String line:Files.readAllLines(Path.of("docs/reference/historical_experience_sequences.tsv"))){if(line.startsWith("#")||line.isBlank())continue;var f=line.split(" ");int era=Integer.parseInt(f[0]),sequence=Integer.parseInt(f[1]);if(era!=previousEra||sequence!=previousSequence){state=new HistoricalExperienceRules.State(0,0,0,0,0);previousEra=era;previousSequence=sequence;}state=HistoricalExperienceRules.gain(era,state,Integer.parseInt(f[3]));boolean remainder=era==5?state.points()==Integer.parseInt(f[5]):Float.floatToIntBits(state.progress())==Float.floatToIntBits(Float.valueOf(f[5]));if(state.level()!=Integer.parseInt(f[4])||!remainder||state.total()!=Integer.parseInt(f[6])||state.score()!=Integer.parseInt(f[7]))throw new AssertionError("Original XP sequence drift: "+line);steps++;}
        if(steps!=1056||HistoricalExperienceRules.audited(6,"1.2.5")||HistoricalExperienceRules.audited(7,"1.0"))throw new AssertionError("XP sequence/scope mismatch");
        System.out.println("HISTORICAL_EXPERIENCE SUCCESS: 4860 original Beta/1.0 XP grants, levels, exact remainders, totals and score");
        System.out.println("HISTORICAL_EXPERIENCE_SEQUENCES SUCCESS: 1056 chained grants and 162 original thresholds");
    }
}
