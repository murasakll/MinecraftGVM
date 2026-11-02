package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Shadow private int foodLevel;
    @Shadow private float saturationLevel;
    @Shadow private float exhaustionLevel;
    @Shadow private int tickTimer;
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void campaign$legacyFood(ServerPlayer player, CallbackInfo ci) {
        if (CampaignRules.legacyFood(player)) {
            foodLevel = 20; saturationLevel = 0; exhaustionLevel = 0; tickTimer = 0;
            ci.cancel();
            return;
        }
        int era=CampaignRules.era(player);
        if(era==5||era==6) {
            var tick=br.com.mura.betaminecraftmod.progression.HistoricalHunger.tick(
                new br.com.mura.betaminecraftmod.progression.HistoricalHunger.State(foodLevel,saturationLevel,exhaustionLevel,tickTimer),
                player.level().getDifficulty().getId(),player.getHealth(),player.isHurt(),
                player.level().getGameRules().get(net.minecraft.world.level.gamerules.GameRules.NATURAL_HEALTH_REGENERATION));
            var state=tick.state();foodLevel=state.food();saturationLevel=state.saturation();exhaustionLevel=state.exhaustion();tickTimer=state.timer();
            if(tick.heal())player.heal(1);
            if(tick.starve())player.hurtServer(player.level(),player.damageSources().starve(),1);
            ci.cancel();
        }
    }
}
