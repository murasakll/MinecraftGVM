package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.ReleasePotionRules;
import java.nio.file.*;

final class ReleasePotionEffectChecks {
    static void run()throws Exception{
        int count=0;
        for(String line:Files.readAllLines(Path.of("docs/reference/potion_effects_1_0.tsv"))){if(line.startsWith("#")||line.isBlank())continue;var f=line.split(" ");
            switch(f[0]){
                case "T","I"->{int id=Integer.parseInt(f[1]),amp=Integer.parseInt(f[3]);boolean heals=(id==7)==Boolean.parseBoolean(f[2]);int amount=f[0].equals("I")?ReleasePotionRules.instant(amp,Double.parseDouble(f[4])):ReleasePotionRules.instant(amp);int offset=f[0].equals("I")?5:4;if(Integer.parseInt(f[offset])!=(heals?amount:0)||Integer.parseInt(f[offset+1])!=(heals?0:amount))throw new AssertionError("Original instant effect differs: "+line);}
                case "C"->{if(ReleasePotionRules.periodic(Integer.parseInt(f[3]),Integer.parseInt(f[2]))!=Boolean.parseBoolean(f[4]))throw new AssertionError("Original cadence differs: "+line);}
                case "A"->{if(ReleasePotionRules.melee(Integer.parseInt(f[1]),Integer.parseInt(f[2]),Integer.parseInt(f[3]))!=Integer.parseInt(f[4]))throw new AssertionError("Original melee prologue differs: "+line);}
                default->throw new AssertionError("Unknown original effect row");
            }count++;
        }
        if(count!=3405)throw new AssertionError("Incomplete original potion application cases");
        int impacts=0;
        for(String line:Files.readAllLines(Path.of("docs/reference/splash_effects_1_0.tsv"))){if(line.startsWith("#")||line.isBlank())continue;var f=line.split(" ");int id=Integer.parseInt(f[0]);boolean queried=Boolean.parseBoolean(f[5]);double x=Double.parseDouble(f[2]),y=Double.parseDouble(f[3]),z=Double.parseDouble(f[4]);double scale=queried?ReleasePotionRules.splashScale(x*x+y*y+z*z,Boolean.parseBoolean(f[1])):-1;
            if(id==6&&Double.doubleToLongBits(scale)!=Double.doubleToLongBits(Double.parseDouble(f[6]))||id==10&&(scale<0?-1:ReleasePotionRules.splashDuration(676,scale))!=Integer.parseInt(f[7]))throw new AssertionError("Original splash impact differs: "+line);impacts++;
        }
        if(impacts!=52)throw new AssertionError("Incomplete splash reference");
        System.out.println("RELEASE_POTION_EFFECTS SUCCESS: 3405 original application, cadence and melee arithmetic cases");
        System.out.println("RELEASE_SPLASH_EFFECTS SUCCESS: 52 original query, direct-hit, range and falloff cases");
    }
}
