package br.com.mura.betaminecraftmod.progression;

public final class HistoricalDeathExperienceRules {
    public static final String PENDING="betaminecraftmod:death_xp_pending";
    private HistoricalDeathExperienceRules(){}
    public static boolean audited(int era,String reference){return era>=0&&(era<=5||era==6&&"1.0".equals(reference));}
    public static int reward(int era,int level,int total){if(era<5)return 0;if(era==5)return total>>1;int value=level*7;return value>100?100:value;}
    public static int ticks(int era){return era==5?21:20;}
}
