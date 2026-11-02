package br.com.mura.betaminecraftmod.progression;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid="betaminecraftmod")
public final class HistoricalItemEvents {
    private HistoricalItemEvents() { }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer player)normalize(player);
    }
    @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent event) {
        HistoricalItemLimits.apply(event.getCrafting(),CampaignRules.era(event.getEntity()));
    }
    @SubscribeEvent public static void dropped(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide()&&event.getEntity() instanceof ItemEntity item)
            HistoricalItemLimits.apply(item.getItem(),CampaignRules.era(event.getLevel()));
    }
    public static void normalize(ServerPlayer player) {
        int era=CampaignRules.era(player);
        var seen=Collections.newSetFromMap(new IdentityHashMap<ItemStack,Boolean>());
        var stacks=new ArrayList<ItemStack>();
        for(var slot:player.inventoryMenu.slots)if(!(slot instanceof ResultSlot))stacks.add(slot.getItem());
        if(player.containerMenu!=player.inventoryMenu)for(var slot:player.containerMenu.slots)
            if(!(slot instanceof ResultSlot))stacks.add(slot.getItem());
        stacks.add(player.containerMenu.getCarried());
        // First apply every limit; transfers must never see half-converted inventories.
        for(var stack:stacks)if(seen.add(stack))HistoricalItemLimits.apply(stack,era);
        for(var stack:seen)splitOverflow(player,stack);
    }
    /** Overflow is only removed after the server accepts an owned drop; cancellation cannot delete it. */
    private static void splitOverflow(ServerPlayer player,ItemStack stack) {
        if(stack.isEmpty())return;
        int max=stack.getMaxStackSize();
        while(stack.getCount()>max) {
            int amount=Math.min(max,stack.getCount()-max);
            var entity=new ItemEntity(player.level(),player.getX(),player.getY()+0.5,player.getZ(),stack.copyWithCount(amount));
            entity.setTarget(player.getUUID());entity.setDefaultPickUpDelay();
            if(!player.level().addFreshEntity(entity))break;
            stack.shrink(amount);
        }
    }
}
