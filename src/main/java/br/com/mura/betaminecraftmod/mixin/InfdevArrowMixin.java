package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public abstract class InfdevArrowMixin {
    @Inject(method="getDefaultGravity",at=@At("HEAD"),cancellable=true)
    private void campaign$gravity(CallbackInfoReturnable<Double> cir){
        if(CampaignRules.instantBow(((AbstractArrow)(Object)this).level()))cir.setReturnValue(.03);
    }
    @Inject(method="getWaterInertia",at=@At("HEAD"),cancellable=true)
    private void campaign$waterDrag(CallbackInfoReturnable<Float> cir){
        if(CampaignRules.instantBow(((AbstractArrow)(Object)this).level()))cir.setReturnValue(.8f);
    }
    @ModifyArg(method="onHitEntity",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),index=1)
    private float campaign$fixedDamage(float modernDamage){
        return CampaignRules.instantBow(((AbstractArrow)(Object)this).level())?4:modernDamage;
    }
}
