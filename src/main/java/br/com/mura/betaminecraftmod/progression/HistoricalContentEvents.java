package br.com.mura.betaminecraftmod.progression;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.*;

/** Later reference gates are distinct from the older Infdev-specific behaviors. */
@EventBusSubscriber(modid="betaminecraftmod")
public final class HistoricalContentEvents {
    private static final Set<String> BASE=Set.of("player","item","painting","tnt","falling_block","arrow","pig","sheep","cow","chicken","zombie","skeleton","spider","creeper","slime","giant","minecart","chest_minecart","furnace_minecart","oak_boat","snowball","egg","fishing_bobber","ghast","zombified_piglin","fireball");
    private static final Set<String> BETA=Set.of("squid","wolf","lightning_bolt");
    private static final Set<String> ADVENTURE=Set.of("enderman","cave_spider","silverfish","experience_orb");
    private static final Set<String> RELEASE=Set.of("villager","mooshroom","snow_golem","blaze","small_fireball","magma_cube","ender_dragon","end_crystal","ender_pearl","eye_of_ender","potion","splash_potion");
    private static boolean later(int era){return era>=3&&era<=6;}
    @SubscribeEvent public static void travel(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent event) {
        int era=CampaignRules.era(event.getEntity().level());
        if(era>=0&&era<6&&event.getDimension().equals(net.minecraft.world.level.Level.END))event.setCanceled(true);
    }
    public static boolean entityAllowed(String path,int era){return later(era)&&(BASE.contains(path)||era>=4&&BETA.contains(path)||era>=5&&ADVENTURE.contains(path)||era>=6&&RELEASE.contains(path));}
    @SubscribeEvent public static void spawn(EntityJoinLevelEvent event) {
        int era=CampaignRules.era(event.getLevel());if(!later(era)||event.getLevel().isClientSide())return;
        var entity=event.getEntity();var id=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if(!id.getNamespace().equals("minecraft")||!entityAllowed(id.getPath(),era)
            ||entity instanceof ItemEntity item&&!HistoricalContent.allowed(item.getItem(),era)){event.setCanceled(true);return;}
        if(!event.loadedFromDisk())HistoricalMobPresentation.normalize(entity,era);
    }
    @SubscribeEvent public static void drops(BlockDropsEvent event) {
        int era=CampaignRules.era(event.getLevel());if(!later(era))return;
        // Ore XP was introduced after the release 1.0 reference.
        event.setDroppedExperience(0);
        for(var drop:event.getDrops()) {
            if(drop.getItem().is(Items.RAW_IRON))drop.setItem(new ItemStack(Items.IRON_ORE,drop.getItem().getCount()));
            if(drop.getItem().is(Items.RAW_GOLD))drop.setItem(new ItemStack(Items.GOLD_ORE,drop.getItem().getCount()));
        }
        event.getDrops().removeIf(d->!HistoricalContent.allowed(d.getItem(),era));
        if(event.getState().getBlock() instanceof LeavesBlock)event.getDrops().removeIf(d->d.getItem().is(Items.STICK)||d.getItem().is(Items.APPLE));
    }
    @SubscribeEvent public static void mobDrops(LivingDropsEvent event) {
        int era=CampaignRules.era(event.getEntity().level());if(!later(era))return;
        String type=BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).getPath();
        if(era<5)for(var drop:event.getDrops()) {
            if(type.equals("zombie")&&drop.getItem().is(Items.ROTTEN_FLESH))drop.setItem(new ItemStack(Items.FEATHER,drop.getItem().getCount()));
            if(type.equals("zombified_piglin")&&drop.getItem().is(Items.ROTTEN_FLESH))drop.setItem(new ItemStack(Items.COOKED_PORKCHOP,drop.getItem().getCount()));
        }
        event.getDrops().removeIf(d->!HistoricalContent.allowed(d.getItem(),era));
    }
    @SubscribeEvent public static void place(BlockEvent.EntityPlaceEvent event) {
        if(!(event.getLevel() instanceof ServerLevel level))return;
        int era=CampaignRules.era(level);if(later(era)&&!HistoricalContent.allowed(new ItemStack(event.getPlacedBlock().getBlock()),era))event.setCanceled(true);
    }
    @SubscribeEvent public static void item(PlayerInteractEvent.RightClickItem event) {
        int era=CampaignRules.era(event.getEntity());if(!later(era))return;
        if(!HistoricalContent.allowed(event.getItemStack(),era)||era==5&&event.getItemStack().is(Items.ENDER_PEARL)) {
            event.setCancellationResult(InteractionResult.FAIL);event.setCanceled(true);
        }
    }
    @SubscribeEvent public static void block(PlayerInteractEvent.RightClickBlock event) {
        int era=CampaignRules.era(event.getEntity());if(later(era)&&!HistoricalContent.allowed(event.getItemStack(),era)) {
            event.setCancellationResult(InteractionResult.FAIL);event.setCanceled(true);
        }
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract event) {
        int era=CampaignRules.era(event.getEntity());if(!later(era))return;
        boolean breeding=era<6&&event.getTarget() instanceof net.minecraft.world.entity.animal.Animal animal&&animal.isFood(event.getItemStack());
        boolean trading=event.getTarget() instanceof net.minecraft.world.entity.npc.villager.Villager;
        if(breeding||trading){event.setCancellationResult(InteractionResult.FAIL);event.setCanceled(true);}
    }
    private HistoricalContentEvents() { }
}
