package br.com.mura.betaminecraftmod.progression;

/** Construction milestones through the first-release chapter, independent of rendering readiness. */
public final class HistoricalConstruction {
    private HistoricalConstruction() { }
    public static boolean cubeChest(int era){return era>=0&&era<=4;}
    public static boolean chestHitbox(int era){return era>=0&&era<=6;}
    public static boolean straightStairs(int era){return era>=0&&era<=6;}
    public static boolean bottomStairs(int era,String firstRelease){return era>=0&&(era<6||era==6&&!firstRelease.equals("1.2.5"));}
    public static boolean dryPlacement(int era){return era>=0&&era<=6;}
}
