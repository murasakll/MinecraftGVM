package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEffectInstance.class)
public abstract class HistoricalPotionTickMixin {
    @WrapOperation(method="tickServer",at=@At(value="INVOKE",target="Lnet/minecraft/world/effect/MobEffect;shouldApplyEffectTickThisTick(II)Z"))
    private boolean campaign$cadence(MobEffect effect,int remaining,int amplifier,Operation<Boolean> original,ServerLevel level){
        return ReleaseBrewing.active(level)&&(effect==MobEffects.REGENERATION.value()||effect==MobEffects.POISON.value())?ReleasePotionRules.periodic(remaining,amplifier):original.call(effect,remaining,amplifier);
    }
}
