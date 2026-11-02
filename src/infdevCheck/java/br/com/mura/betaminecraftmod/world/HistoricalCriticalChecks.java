package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.HistoricalCriticalRules;
import java.nio.file.*;
import java.util.Random;

final class HistoricalCriticalChecks {
    static void run() throws Exception {
        int cases = 0;
        for (String line : Files.readAllLines(Path.of("docs/reference/critical_damage.tsv"))) {
            if (line.isBlank() || line.startsWith("#")) continue;
            var fields = line.split(" ");
            int era=Integer.parseInt(fields[0]), base=Integer.parseInt(fields[1]), flags=Integer.parseInt(fields[3]);
            var random=new Random(Long.parseLong(fields[2]));
            int damage=HistoricalCriticalRules.critical(era,flags)?HistoricalCriticalRules.damage(era,base,random::nextInt):base;
            if (damage!=Integer.parseInt(fields[4]) || random.nextLong()!=Long.parseLong(fields[5]))
                throw new AssertionError("Original critical/RNG differs: "+line);
            cases++;
        }
        if(cases!=16384)throw new AssertionError("Incomplete original critical matrix");
        for(int era=0;era<5;era++)for(int flags=0;flags<512;flags++)
            if(HistoricalCriticalRules.critical(era,flags))throw new AssertionError("Pre-Adventure critical");
        System.out.println("HISTORICAL_CRITICAL SUCCESS: 16384 original attack prefixes, conditions and RNG consumption");
    }
}
