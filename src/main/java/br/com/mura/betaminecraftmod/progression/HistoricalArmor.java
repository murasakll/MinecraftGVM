package br.com.mura.betaminecraftmod.progression;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

/** Initial armor used material-independent protection, reduced as the equipment wore out. */
public final class HistoricalArmor {
    private static final String REMAINDER="betaminecraftmod:armor_remainder";
    private static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    private HistoricalArmor() { }
    public static boolean active(Player player){int era=CampaignRules.era(player);return era>=0&&era<=6;}
    public static int releaseProtection(String item) {
        int slot=item.endsWith("_helmet")?0:item.endsWith("_chestplate")?1:item.endsWith("_leggings")?2:item.endsWith("_boots")?3:-1;
        if(slot<0)return 0;
        int[] points=item.startsWith("leather_")?new int[]{1,3,2,1}:item.startsWith("chainmail_")?new int[]{2,5,4,1}:
            item.startsWith("iron_")?new int[]{2,6,5,2}:item.startsWith("golden_")?new int[]{2,5,3,1}:item.startsWith("diamond_")?new int[]{3,8,6,3}:new int[4];
        return points[slot];
    }
    public static int protection(int points,int remaining,int total){return total==0?0:(points-1)*Math.max(0,remaining)/total+1;}
    public static int protection(Player player) {
        int points=0,remaining=0,total=0;
        for(var slot:SLOTS) {
            var stack=player.getItemBySlot(slot);if(stack.isEmpty())continue;
            var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
            if(!id.getNamespace().equals("minecraft"))continue;
            String path=id.getPath();int defense=CampaignRules.era(player)==6?releaseProtection(path):path.endsWith("_helmet")?3:path.endsWith("_chestplate")?8:path.endsWith("_leggings")?6:path.endsWith("_boots")?3:0;
            var limit=HistoricalItemLimits.limits(CampaignRules.era(player),id.toString());
            if(defense==0||limit==null||limit.damage()<=0)continue;
            HistoricalItemLimits.apply(stack,CampaignRules.era(player));
            int reference=limit.damage()-1;points+=defense;total+=reference;remaining+=Math.max(0,reference-stack.getDamageValue());
        }
        return CampaignRules.era(player)==6?points:protection(points,remaining,total);
    }
    public static float absorb(Player player,float damage) {
        int amount=Math.max(0,(int)damage),armor=protection(player);
        long value=(long)amount*(25-armor)+player.getPersistentData().getIntOr(REMAINDER,0);
        for(var slot:SLOTS) {
            var stack=player.getItemBySlot(slot);var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
            var limit=HistoricalItemLimits.limits(CampaignRules.era(player),id.toString());
            if(!stack.isEmpty()&&limit!=null&&limit.damage()>0)stack.hurtAndBreak(CampaignRules.era(player)==6?Math.max(1,amount/4):amount,player,slot);
        }
        player.getPersistentData().putInt(REMAINDER,(int)(value%25));
        return value/25;
    }
}
