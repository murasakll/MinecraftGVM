package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Player.class)
public abstract class HistoricalExperienceMixin {
    @Inject(method="getXpNeededForNextLevel",at=@At("HEAD"),cancellable=true)
    private void campaign$needed(CallbackInfoReturnable<Integer> result) {
        var player=(Player)(Object)this;int era=CampaignRules.era(player.level());
        if(HistoricalExperienceRules.audited(era,CampaignRules.firstRelease(player.level()))&&player.experienceLevel>=0) {
            int needed=HistoricalExperienceRules.needed(era,player.experienceLevel);if(needed>0)result.setReturnValue(needed);
        }
    }
    // Runs after the host's XpChange event, retaining its cancellation and adjusted amount exactly once.
    @Inject(method="giveExperiencePoints",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;increaseScore(I)V"),cancellable=true)
    private void campaign$gain(int amount,CallbackInfo ci) {
        var player=(Player)(Object)this;if(player.level().isClientSide()||amount<0)return;
        int era=CampaignRules.era(player.level());
        if(era>=0&&era<5){ci.cancel();return;}
        if(HistoricalExperienceRules.audited(era,CampaignRules.firstRelease(player.level()))&&(amount==0||HistoricalExperience.grant(player,era,amount)))ci.cancel();
    }
    @Inject(method="tick",at=@At("HEAD"))
    private void campaign$clearBetaOwnership(CallbackInfo ci){HistoricalExperience.refresh((Player)(Object)this);}
}
