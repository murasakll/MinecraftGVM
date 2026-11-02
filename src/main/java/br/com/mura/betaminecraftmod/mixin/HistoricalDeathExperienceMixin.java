package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class HistoricalDeathExperienceMixin implements HistoricalDeathExperienceAccess {
    @Shadow public int deathTime;
    @Shadow protected abstract void dropExperience(ServerLevel level,Entity killer);
    @Unique private boolean campaign$releasing;
    @Unique private Entity campaign$killer;
    @Override public void campaign$resolveDeathExperience() {
        if(!((Object)this instanceof Player player)||player.level().isClientSide()||player.isAlive()||
                !player.getPersistentData().getBooleanOr(HistoricalDeathExperienceRules.PENDING,false))return;
        player.getPersistentData().remove(HistoricalDeathExperienceRules.PENDING);campaign$releasing=true;
        try{if(!player.isSpectator())dropExperience((ServerLevel)player.level(),campaign$killer);}finally{campaign$releasing=false;campaign$killer=null;}
    }
    @WrapOperation(method="getExperienceReward",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processMobExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;I)I"))
    private int campaign$noFutureMultiplier(ServerLevel level,Entity killer,Entity victim,int reward,Operation<Integer> original) {
        if(victim instanceof Player&&HistoricalDeathExperienceRules.audited(CampaignRules.era(level),CampaignRules.firstRelease(level)))return reward;
        return original.call(level,killer,victim,reward);
    }
    @Inject(method="dropExperience",at=@At("HEAD"),cancellable=true)
    private void campaign$deferPlayerDrop(ServerLevel level,Entity killer,CallbackInfo ci) {
        if(!((Object)this instanceof Player player))return;
        int era=CampaignRules.era(level);
        if(!HistoricalDeathExperienceRules.audited(era,CampaignRules.firstRelease(level)))return;
        if(era<5){player.getPersistentData().remove(HistoricalDeathExperienceRules.PENDING);ci.cancel();return;}
        if(!campaign$releasing){player.getPersistentData().putBoolean(HistoricalDeathExperienceRules.PENDING,true);campaign$killer=killer;ci.cancel();}
    }
    @Inject(method="tickDeath",at=@At("HEAD"),cancellable=true)
    private void campaign$deathClock(CallbackInfo ci) {
        if(!((Object)this instanceof Player player))return;
        int era=CampaignRules.era(player.level());boolean historical=era>=5&&HistoricalDeathExperienceRules.audited(era,CampaignRules.firstRelease(player.level()));
        boolean pending=player.getPersistentData().getBooleanOr(HistoricalDeathExperienceRules.PENDING,false);
        if(!historical&&!pending)return;
        ci.cancel();deathTime++;
        if(deathTime<HistoricalDeathExperienceRules.ticks(era)||player.level().isClientSide()||player.isRemoved())return;
        if(pending) {
            // Consume before the callback so save/reentry or a repeated tick cannot award the same death twice.
            campaign$resolveDeathExperience();
        }
        player.level().broadcastEntityEvent(player,(byte)60);player.remove(Entity.RemovalReason.KILLED);
    }
}
