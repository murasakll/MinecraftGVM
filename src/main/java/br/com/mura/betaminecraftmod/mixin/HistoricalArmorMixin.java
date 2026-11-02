package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.HistoricalArmor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class HistoricalArmorMixin {
    @Inject(method="getDamageAfterArmorAbsorb",at=@At("HEAD"),cancellable=true)
    private void campaign$armor(DamageSource source,float damage,CallbackInfoReturnable<Float> cir) {
        // Keep the native emergency/void kill path; historical armor must never prevent rescue/death.
        if((Object)this instanceof Player player&&HistoricalArmor.active(player)&&!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)&&!source.is(DamageTypeTags.BYPASSES_ARMOR))
            cir.setReturnValue(HistoricalArmor.absorb(player,damage));
    }
    @Inject(method="getArmorValue",at=@At("HEAD"),cancellable=true)
    private void campaign$armorDisplay(CallbackInfoReturnable<Integer> cir) {
        if((Object)this instanceof Player player&&HistoricalArmor.active(player))cir.setReturnValue(HistoricalArmor.protection(player));
    }
}
