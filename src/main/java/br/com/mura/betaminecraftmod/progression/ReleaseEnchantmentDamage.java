package br.com.mura.betaminecraftmod.progression;

import java.util.Random;
import java.util.function.IntUnaryOperator;

/** The 1.0 enchantment helper used an independent, process-wide java.util.Random. */
public final class ReleaseEnchantmentDamage {
    private static final Random RANDOM=new Random();
    private ReleaseEnchantmentDamage(){}
    public static int roll(int category,int sharpness,int smite,int bane,IntUnaryOperator random) {
        int budget=sharpness*3+(category==1?smite*4:0)+(category==2?bane*4:0);
        return budget>0?1+random.applyAsInt(budget):0;
    }
    public static int roll(int category,int sharpness,int smite,int bane) {
        return roll(category,sharpness,smite,bane,RANDOM::nextInt);
    }
}
