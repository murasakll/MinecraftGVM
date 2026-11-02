package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.ReleasePotionEffects;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class HistoricalPotionAttributesMixin {
    @WrapOperation(method={"onEffectAdded","onEffectUpdated"},at=@At(value="INVOKE",target="Lnet/minecraft/world/effect/MobEffect;addAttributeModifiers(Lnet/minecraft/world/entity/ai/attributes/AttributeMap;I)V"))
    private void campaign$attributes(MobEffect effect,AttributeMap attributes,int amplifier,Operation<Void> original){original.call(effect,attributes,amplifier);ReleasePotionEffects.refresh((LivingEntity)(Object)this);}
    @Inject(method="tick",at=@At("HEAD"))
    private void campaign$eraChange(CallbackInfo ci){ReleasePotionEffects.refresh((LivingEntity)(Object)this);}
}
