package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrb.class)
public abstract class HistoricalExperienceOrbMergeMixin {
    @Inject(method="tryMergeToExisting",at=@At("HEAD"),cancellable=true)
    private static void campaign$individualAwards(ServerLevel level,Vec3 pos,int value,CallbackInfoReturnable<Boolean> result) {
        if(HistoricalExperienceRules.audited(CampaignRules.era(level),CampaignRules.firstRelease(level)))result.setReturnValue(false);
    }
    @Inject(method="canMerge(Lnet/minecraft/world/entity/ExperienceOrb;)Z",at=@At("HEAD"),cancellable=true)
    private void campaign$individualOrbs(ExperienceOrb other,CallbackInfoReturnable<Boolean> result) {
        var orb=(ExperienceOrb)(Object)this;
        if(HistoricalExperienceRules.audited(CampaignRules.era(orb.level()),CampaignRules.firstRelease(orb.level())))result.setReturnValue(false);
    }
}
