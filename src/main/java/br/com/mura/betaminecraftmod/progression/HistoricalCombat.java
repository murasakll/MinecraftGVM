package br.com.mura.betaminecraftmod.progression;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent;

@EventBusSubscriber(modid = "betaminecraftmod")
public final class HistoricalCombat {
    @SubscribeEvent public static void sweep(SweepAttackEvent event) {
        if (CampaignRules.legacyCombat(event.getEntity())) event.setSweeping(false);
    }
    @SubscribeEvent public static void critical(CriticalHitEvent event) {
        // Critical hits arrived with Beta 1.8, separately from the 1.9 cooldown/sweep.
        if (CampaignRules.legacyFood(event.getEntity())) { event.setCriticalHit(false); event.setDamageMultiplier(1); }
    }
}
