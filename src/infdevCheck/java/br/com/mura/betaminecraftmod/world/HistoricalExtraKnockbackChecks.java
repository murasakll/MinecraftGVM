package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.HistoricalExtraKnockback;
import java.nio.file.*;

final class HistoricalExtraKnockbackChecks {
    static void run()throws Exception {
        int cases=0;
        for(String line:Files.readAllLines(Path.of("docs/reference/extra_knockback.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;var f=line.split(" ");
            int era=Integer.parseInt(f[0]),enchant=Integer.parseInt(f[3]);boolean sprint=Boolean.parseBoolean(f[2]),hit=Boolean.parseBoolean(f[4]);
            var impulse=HistoricalExtraKnockback.impulse(era,Float.parseFloat(f[1]),sprint,enchant);
            boolean applied=hit&&impulse.applied();
            double[] actual={.25+(applied?impulse.x():0),.75+(applied?impulse.y():0),-.5+(applied?impulse.z():0),applied?2*.6:2,-.25,applied?-3*.6:-3};
            for(int i=0;i<6;i++)if(Double.doubleToLongBits(actual[i])!=Double.doubleToLongBits(Double.valueOf(f[i+5])))throw new AssertionError("Original extra impulse differs: "+line+" component="+i);
            if((sprint&&!applied)!=Boolean.parseBoolean(f[11]))throw new AssertionError("Sprint reset differs");cases++;
        }
        if(cases!=352)throw new AssertionError("Incomplete knockback reference");
        for(int era=0;era<5;era++)if(HistoricalExtraKnockback.impulse(era,0,true,5).applied())throw new AssertionError("Early extra impulse");
        System.out.println("HISTORICAL_EXTRA_KNOCKBACK SUCCESS: 352 original impulse/drag/sprint cases and pre-Adventure exclusion");
    }
}
