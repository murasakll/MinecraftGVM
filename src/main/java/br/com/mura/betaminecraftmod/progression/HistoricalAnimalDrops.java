package br.com.mura.betaminecraftmod.progression;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid="betaminecraftmod")
public final class HistoricalAnimalDrops {
    private HistoricalAnimalDrops() { }
    @SubscribeEvent public static void feeding(PlayerInteractEvent.EntityInteract event) {
        if(InfdevContent.legacy(event.getLevel())&&event.getTarget() instanceof net.minecraft.world.entity.animal.Animal animal
                &&animal.isFood(event.getItemStack())) {
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void drops(LivingDropsEvent event) {
        var entity=event.getEntity();if(!InfdevContent.legacy(entity.level()))return;
        if(entity instanceof Sheep){event.getDrops().clear();return;}
        Item item=switch(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath()) {
            case "pig" -> Items.PORKCHOP;
            case "cow" -> Items.LEATHER;
            case "chicken", "zombie" -> Items.FEATHER;
            case "skeleton" -> Items.ARROW;
            case "spider" -> Items.STRING;
            // Creeper's special record drop is handled by the existing path, not discarded here.
            default -> null;
        };
        if(item==null)return;
        event.getDrops().clear();int count=entity.getRandom().nextInt(3);
        for(int i=0;i<count;i++) {
            var stack=new ItemStack(item);HistoricalItemLimits.apply(stack,CampaignRules.era(entity.level()));
            var drop=new ItemEntity(entity.level(),entity.getX(),entity.getY(),entity.getZ(),stack);
            drop.setDefaultPickUpDelay();event.getDrops().add(drop);
        }
    }
    @SubscribeEvent public static void experience(LivingExperienceDropEvent event) {
        int era=CampaignRules.era(event.getEntity().level());
        if(era>=0&&era<CampaignRules.ADVENTURE_UPDATE)event.setDroppedExperience(0);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void wool(LivingIncomingDamageEvent event) {
        if(!(event.getEntity() instanceof Sheep sheep)||!(sheep.level() instanceof ServerLevel level)
                ||!InfdevContent.legacy(level)||sheep.isSheared()||event.getAmount()<=0
                ||!(event.getSource().getEntity() instanceof LivingEntity))return;
        sheep.setSheared(true);
        int count=1+sheep.getRandom().nextInt(3);
        var wool=BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("minecraft:white_wool"));
        for(int i=0;i<count;i++)sheep.spawnAtLocation(level,new ItemStack(wool));
    }
}
