package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignProgress;
import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Persistent portal tickets must see saved dimension gates before initial chunk readiness waits. */
@Mixin(MinecraftServer.class)
public abstract class CampaignBootstrapMixin {
    @Inject(method="prepareLevels",at=@At("HEAD"))
    private void campaign$restoreBeforePersistentTickets(CallbackInfo ci) {
        var server=(MinecraftServer)(Object)this;var overworld=server.overworld();
        if(!CampaignRules.isCampaign(overworld))return;
        var progress=CampaignProgress.get(overworld);progress.applyBorder(overworld);
        br.com.mura.betaminecraftmod.BetaMinecraftMod.LOGGER.debug("Campaign dimension gates restored to era {} before persistent ticket activation",progress.era());
    }
}
