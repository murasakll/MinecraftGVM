package br.com.mura.betaminecraftmod.progression;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = "betaminecraftmod")
public final class InfdevContentEvents {
    private static final Set<String> ENTITIES = Set.of("player", "item", "painting", "tnt", "falling_block", "arrow", "pig", "sheep", "zombie", "skeleton", "spider", "creeper", "giant");
    private static final Set<String> ALPHA_ENTITIES = Set.of("cow", "chicken", "slime", "oak_boat", "chest_minecart", "furnace_minecart", "snowball");
    @SubscribeEvent public static void spawn(EntityJoinLevelEvent event) {
        if (!InfdevContent.legacy(event.getLevel())) return;
        var entity = event.getEntity();
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (!id.getNamespace().equals("minecraft") || !(ENTITIES.contains(id.getPath()) || id.getPath().equals("minecart") && CampaignProgress.get((net.minecraft.server.level.ServerLevel)event.getLevel()).era() >= 1 || ALPHA_ENTITIES.contains(id.getPath()) && CampaignProgress.get((net.minecraft.server.level.ServerLevel)event.getLevel()).era() == 2)
                || entity instanceof ItemEntity item && !InfdevContent.allowed(item.getItem(), CampaignProgress.get((net.minecraft.server.level.ServerLevel)event.getLevel()).era())) { event.setCanceled(true); return; }
        if (entity instanceof net.minecraft.world.entity.animal.sheep.Sheep sheep) sheep.setColor(net.minecraft.world.item.DyeColor.WHITE);
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFFHAND}) mob.setItemSlot(slot, ItemStack.EMPTY);
            if (entity instanceof net.minecraft.world.entity.monster.zombie.Zombie) mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            if (entity instanceof net.minecraft.world.entity.monster.skeleton.Skeleton) mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
    }
    @SubscribeEvent public static void drops(BlockDropsEvent event) {
        if (!InfdevContent.legacy(event.getLevel())) return;
        event.setDroppedExperience(0);
        for (var drop : event.getDrops()) {
            if (drop.getItem().is(Items.RAW_IRON)) drop.setItem(new ItemStack(Items.IRON_ORE, drop.getItem().getCount()));
            if (drop.getItem().is(Items.RAW_GOLD)) drop.setItem(new ItemStack(Items.GOLD_ORE, drop.getItem().getCount()));
        }
        event.getDrops().removeIf(drop -> !InfdevContent.allowed(drop.getItem(), CampaignProgress.get((net.minecraft.server.level.ServerLevel)drop.level()).era()));
        if (event.getState().getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)
            event.getDrops().removeIf(drop -> drop.getItem().is(Items.STICK) || drop.getItem().is(Items.APPLE));
        if(event.getState().is(Blocks.WHEAT)) {
            event.getDrops().clear();int age=event.getState().getValue(net.minecraft.world.level.block.CropBlock.AGE);
            int seeds=0;for(int i=0;i<3;i++)if(event.getLevel().getRandom().nextInt(15)<=age)seeds++;
            if(age==7)event.getDrops().add(new ItemEntity(event.getLevel(),event.getPos().getX()+.5,event.getPos().getY()+.5,event.getPos().getZ()+.5,new ItemStack(Items.WHEAT)));
            if(seeds>0)event.getDrops().add(new ItemEntity(event.getLevel(),event.getPos().getX()+.5,event.getPos().getY()+.5,event.getPos().getZ()+.5,new ItemStack(Items.WHEAT_SEEDS,seeds)));
        }
    }
    @SubscribeEvent public static void mobDrops(LivingDropsEvent event) {
        if (!InfdevContent.legacy(event.getEntity().level())) return;
        for (var drop : event.getDrops()) if (drop.getItem().is(Items.ROTTEN_FLESH)) drop.setItem(new ItemStack(Items.FEATHER, drop.getItem().getCount()));
        event.getDrops().removeIf(drop -> !InfdevContent.allowed(drop.getItem(), CampaignProgress.get((net.minecraft.server.level.ServerLevel)drop.level()).era()));
    }
    @SubscribeEvent public static void place(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof net.minecraft.world.level.Level level && InfdevContent.legacy(level)
                && !InfdevContent.allowed(new ItemStack(event.getPlacedBlock().getBlock()), CampaignProgress.get((net.minecraft.server.level.ServerLevel)level).era())) event.setCanceled(true);
    }
    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickItem event) {
        int era = CampaignRules.era(event.getEntity());
        if (era >= 0 && era <= 2 && (event.getItemStack().is(Items.FISHING_ROD) || event.getItemStack().is(Items.EGG) || event.getItemStack().is(Items.MILK_BUCKET))) {
            event.setCancellationResult(InteractionResult.FAIL); event.setCanceled(true); return;
        }
        if (CampaignRules.legacyFood(event.getEntity()) && !HistoricalContent.allowed(event.getItemStack(), CampaignRules.era(event.getEntity()))) {
            event.setCancellationResult(InteractionResult.FAIL); event.setCanceled(true);
        }
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.RightClickBlock event) {
        if (CampaignRules.legacyFood(event.getEntity()) && !HistoricalContent.allowed(event.getItemStack(), CampaignRules.era(event.getEntity()))) {
            event.setCancellationResult(InteractionResult.FAIL); event.setCanceled(true);
        }
    }
    @SubscribeEvent public static void travel(EntityTravelToDimensionEvent event) {
        if (InfdevContent.legacy(event.getEntity().level()) && !event.getDimension().equals(net.minecraft.world.level.Level.OVERWORLD)) event.setCanceled(true);
    }
}
