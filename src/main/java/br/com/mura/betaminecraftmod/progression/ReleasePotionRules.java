package br.com.mura.betaminecraftmod.progression;

/** Independent arithmetic checked against original 1.0 effect methods and melee prologue. */
public final class ReleasePotionRules {
    private ReleasePotionRules(){}
    public static int instant(int amplifier){return 6<<amplifier;}
    public static int instant(int amplifier,double scale){return (int)(scale*instant(amplifier)+.5);}
    public static boolean periodic(int remaining,int amplifier){int interval=25>>amplifier;return interval<=0||remaining%interval==0;}
    public static int strength(int amplifier){return 3<<amplifier;}
    public static int weakness(int amplifier){return -(2<<amplifier);}
    public static int melee(int base,int strength,int weakness){return base+(strength<0?0:strength(strength))+(weakness<0?0:weakness(weakness));}
    public static double splashScale(double squaredDistance,boolean direct){return squaredDistance>=16?-1:direct?1:1-Math.sqrt(squaredDistance)/4;}
    public static int splashDuration(int duration,double scale){int result=(int)(scale*duration+.5);return result>20?result:-1;}
}
