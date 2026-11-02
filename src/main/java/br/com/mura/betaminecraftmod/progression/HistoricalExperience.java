package br.com.mura.betaminecraftmod.progression;

import net.minecraft.world.entity.player.Player;

/** Native level/fraction/total remain canonical; Beta additionally owns an exact integer remainder. */
public final class HistoricalExperience {
    private static final String POINTS="betaminecraftmod:beta_xp_points",LEVEL="betaminecraftmod:beta_xp_level",BITS="betaminecraftmod:beta_xp_fraction";
    private HistoricalExperience(){}
    public static void clear(Player player){var data=player.getPersistentData();data.remove(POINTS);data.remove(LEVEL);data.remove(BITS);}
    public static int points(Player player) {
        var data=player.getPersistentData();
        if(data.contains(POINTS)&&data.getIntOr(LEVEL,-1)==player.experienceLevel&&data.getIntOr(BITS,-1)==Float.floatToIntBits(player.experienceProgress))return data.getIntOr(POINTS,0);
        // Existing native saves/commands provide a fraction, not the old Beta integer tag.
        return Math.round(player.experienceProgress*HistoricalExperienceRules.needed(5,player.experienceLevel));
    }
    public static void refresh(Player player){if(!player.level().isClientSide()&&CampaignRules.era(player.level())!=5)clear(player);}
    public static boolean grant(Player player,int era,int amount) {
        int needed=HistoricalExperienceRules.needed(era,player.experienceLevel);
        if(player.experienceLevel<0||needed<=0||!Float.isFinite(player.experienceProgress))return false;
        int points=era==5?points(player):0;
        // Unbounded command/plugin values must not enter the old float-decrement infinite-loop case.
        if(era==6&&player.experienceProgress+(float)amount/needed>=16777216F||era==5&&(long)points+amount>Integer.MAX_VALUE)return false;
        var before=new HistoricalExperienceRules.State(player.experienceLevel,points,player.experienceProgress,player.totalExperience,player.getScore());
        var after=HistoricalExperienceRules.gain(era,before,amount);
        player.experienceLevel=after.level();player.experienceProgress=after.progress();player.totalExperience=after.total();player.setScore(after.score());
        if(era==5){var data=player.getPersistentData();data.putInt(POINTS,after.points());data.putInt(LEVEL,after.level());data.putInt(BITS,Float.floatToIntBits(after.progress()));}
        else clear(player);
        return true;
    }
}
