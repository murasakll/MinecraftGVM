package br.com.mura.betaminecraftmod.progression;

import br.com.mura.betaminecraftmod.world.InfdevChunkGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CampaignRules {
    public static final String ERA_TAG = "betaminecraftmod_era";
    public static final int ADVENTURE_UPDATE = 5;
    private static volatile int clientEra = -1;
    public static int clientEra(){return clientEra;}
    private static volatile String clientFirstRelease="1.0";
    public static void clientEra(int era) { clientEra = era;if(era<0)clientFirstRelease="1.0"; }
    public static void clientFirstRelease(String value){clientFirstRelease=java.util.Set.of("1.0","1.1","1.2.5").contains(value)?value:"1.0";}
    public static String firstRelease(net.minecraft.world.level.Level level) {
        if(level instanceof ServerLevel server){if(server.getServer().overworld().getChunkSource().getGenerator() instanceof InfdevChunkGenerator generator)return generator.firstRelease();return "1.0";}
        return clientFirstRelease;
    }
    public static int era(net.minecraft.world.level.BlockGetter view) {
        if(view==net.minecraft.world.level.EmptyBlockGetter.INSTANCE)return -1;
        return view instanceof net.minecraft.world.level.ServerLevelAccessor accessor?era(accessor.getLevel()):clientEra;
    }
    public static String firstRelease(net.minecraft.world.level.BlockGetter view){return view instanceof net.minecraft.world.level.ServerLevelAccessor accessor?firstRelease(accessor.getLevel()):clientFirstRelease;}
    public static boolean cubeChest(net.minecraft.world.level.BlockGetter view){return HistoricalConstruction.cubeChest(era(view));}
    public static boolean chestHitbox(net.minecraft.world.level.BlockGetter view){return HistoricalConstruction.chestHitbox(era(view));}
    public static boolean infdev(net.minecraft.world.level.BlockGetter view) {
        if (view instanceof net.minecraft.world.level.ServerLevelAccessor accessor)
            return InfdevContent.legacy(accessor.getLevel());
        return clientEra >= 0 && clientEra <= 2;
    }
    private CampaignRules() { }
    public static boolean openingInfdev(net.minecraft.world.level.Level level) {
        if(level==null)return false;
        return level instanceof ServerLevel server?isCampaign(server)&&CampaignProgress.get(server).era()==0:clientEra==0;
    }
    public static int era(net.minecraft.world.level.Level level){
        if(level==null)return -1;
        return level instanceof ServerLevel server?isCampaign(server)?CampaignProgress.get(server).era():-1:clientEra;
    }
    public static boolean instantBow(net.minecraft.world.level.Level level){
        var profile=HistoricalMechanics.get(era(level));return profile!=null&&profile.instantBow();
    }
    public static boolean historicalFurnace(net.minecraft.world.level.Level level){
        var profile=HistoricalMechanics.get(era(level));return profile!=null&&profile.historicalFurnace();
    }
    public static int era(Player player) {
        if (player.level() instanceof ServerLevel level) return isCampaign(level) ? CampaignProgress.get(level).era() : -1;
        return player.getPersistentData().getIntOr(ERA_TAG, -1);
    }
    public static boolean legacyCombat(Player player) { var profile = EraCatalog.get(era(player)); return profile != null && !profile.rules().attackCooldown(); }
    public static boolean isCampaign(ServerLevel level) {
        var overworld = level.getServer().overworld();
        return overworld != null && overworld.getChunkSource().getGenerator() instanceof InfdevChunkGenerator;
    }
    public static boolean legacyFood(Player player) {
        int era;
        if (player.level() instanceof ServerLevel level) {
            if (!isCampaign(level)) return false;
            era = CampaignProgress.get(level).era();
        } else era = player.getPersistentData().getIntOr(ERA_TAG, -1);
        var profile = EraCatalog.get(era);
        return profile != null && !profile.rules().hunger();
    }
    public static int healing(ItemStack stack, int fallback) {
        if (stack.is(Items.PORKCHOP)) return 3;
        if (stack.is(Items.COOKED_PORKCHOP)) return 8;
        if (stack.is(Items.BREAD)) return 5;
        if (stack.is(Items.APPLE)) return 4;
        if (stack.is(Items.GOLDEN_APPLE)) return 20;
        if (stack.is(Items.MUSHROOM_STEW)) return 10;
        return fallback;
    }
}
