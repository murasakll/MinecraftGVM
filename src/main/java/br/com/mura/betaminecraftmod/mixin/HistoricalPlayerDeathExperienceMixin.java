package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Player.class)
public abstract class HistoricalPlayerDeathExperienceMixin {
    @Inject(method="getBaseExperienceReward",at=@At("HEAD"),cancellable=true)
    private void campaign$reward(ServerLevel level,CallbackInfoReturnable<Integer> result) {
        var player=(Player)(Object)this;int era=CampaignRules.era(level);
        if(!HistoricalDeathExperienceRules.audited(era,CampaignRules.firstRelease(level)))return;
        result.setReturnValue(level.getGameRules().get(GameRules.KEEP_INVENTORY)||player.isSpectator()?0:
                HistoricalDeathExperienceRules.reward(era,player.experienceLevel,player.totalExperience));
    }
    @Inject(method="tick",at=@At("HEAD"))
    private void campaign$clearRevivedPending(CallbackInfo ci) {
        var player=(Player)(Object)this;
        if(!player.level().isClientSide()&&player.isAlive())player.getPersistentData().remove(HistoricalDeathExperienceRules.PENDING);
    }
}
