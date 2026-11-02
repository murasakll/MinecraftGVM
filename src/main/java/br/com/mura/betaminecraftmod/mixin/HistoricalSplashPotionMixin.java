package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownSplashPotion.class)
public abstract class HistoricalSplashPotionMixin {
    @Inject(method="onHitAsPotion",at=@At("HEAD"),cancellable=true)
    private void campaign$impact(ServerLevel level,ItemStack item,HitResult hit,CallbackInfo ci){
        if(!ReleaseBrewing.active(level))return;ci.cancel();var self=(ThrownSplashPotion)(Object)this;
        var contents=item.getOrDefault(DataComponents.POTION_CONTENTS,PotionContents.EMPTY);
        for(var entity:level.getEntitiesOfClass(LivingEntity.class,self.getBoundingBox().inflate(4,2,4))){
            double scale=ReleasePotionRules.splashScale(self.distanceToSqr(entity),hit instanceof EntityHitResult direct&&direct.getEntity()==entity);if(scale<0)continue;
            for(var effect:contents.getAllEffects()){
                if(effect.getEffect().value().isInstantaneous())effect.getEffect().value().applyInstantaneousEffect(level,self,self.getOwner(),entity,effect.getAmplifier(),scale);
                else {int duration=ReleasePotionRules.splashDuration(effect.getDuration(),scale);if(duration>0)entity.addEffect(new MobEffectInstance(effect.getEffect(),duration,effect.getAmplifier()),self.getOwner()==null?self:self.getOwner());}
            }
        }
    }
}
