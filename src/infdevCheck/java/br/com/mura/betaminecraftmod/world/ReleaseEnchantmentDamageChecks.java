package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.ReleaseEnchantmentDamage;
import java.nio.file.*;
import java.util.Random;

final class ReleaseEnchantmentDamageChecks {
    static void run()throws Exception {
        int cases=0;
        for(String line:Files.readAllLines(Path.of("docs/reference/enchantment_damage_1_0.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;var f=line.split(" ");var random=new Random(Long.parseLong(f[4]));
            int bonus=ReleaseEnchantmentDamage.roll(Integer.parseInt(f[0]),Integer.parseInt(f[1]),Integer.parseInt(f[2]),Integer.parseInt(f[3]),random::nextInt);
            if(bonus!=Integer.parseInt(f[5])||random.nextLong()!=Long.parseLong(f[6]))throw new AssertionError("Original enchantment damage/RNG differs: "+line);cases++;
        }
        if(cases!=2592)throw new AssertionError("Incomplete enchantment reference");
        System.out.println("RELEASE_ENCHANTMENT_DAMAGE SUCCESS: 2592 original collection/roll/RNG cases");
    }
}
