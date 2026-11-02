package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import br.com.mura.betaminecraftmod.progression.HistoricalCriticalRules;
import br.com.mura.betaminecraftmod.progression.HistoricalMeleeCatalog;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import org.spongepowered.asm.mixin.injection.Slice;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void campaign$eatWhenHurt(boolean always, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (CampaignRules.legacyFood(player)) cir.setReturnValue(player.isHurt());
    }
    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void campaign$noExhaustion(float amount, CallbackInfo ci) {
        if (CampaignRules.legacyFood((Player) (Object) this)) ci.cancel();
    }
    @Inject(method = {"getAttackStrengthScale", "getItemSwapScale"}, at = @At("HEAD"), cancellable = true)
    private void campaign$fullStrength(float partial, CallbackInfoReturnable<Float> cir) {
        if (CampaignRules.legacyCombat((Player) (Object) this)) cir.setReturnValue(1F);
    }
    @Inject(method = "cannotAttackWithItem", at = @At("HEAD"), cancellable = true)
    private void campaign$noAttackCharge(net.minecraft.world.item.ItemStack stack, int tolerance, CallbackInfoReturnable<Boolean> cir) {
        if (CampaignRules.legacyCombat((Player) (Object) this)) cir.setReturnValue(false);
    }
    @Inject(method = "getCurrentItemAttackStrengthDelay", at = @At("HEAD"), cancellable = true)
    private void campaign$noFullChargeIndicator(CallbackInfoReturnable<Float> cir) {
        if (CampaignRules.legacyCombat((Player)(Object)this)) cir.setReturnValue(1F);
    }
    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private float campaign$damage(float modern) {
        Player player = (Player)(Object)this;
        int era = CampaignRules.era(player);
        if (!br.com.mura.betaminecraftmod.progression.HistoricalMeleeCatalog.active(era, CampaignRules.firstRelease(player.level()))) return modern;
        String item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString();
        float base = br.com.mura.betaminecraftmod.progression.HistoricalMeleeCatalog.damage(era, item, Float.NaN);
        if (Float.isNaN(base)) return modern;
        if (era == 6) {
            var strength = player.getEffect(net.minecraft.world.effect.MobEffects.STRENGTH);
            var weakness = player.getEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
            base = br.com.mura.betaminecraftmod.progression.ReleasePotionRules.melee((int) base,
                    strength == null ? -1 : strength.getAmplifier(), weakness == null ? -1 : weakness.getAmplifier());
        }
        return base;
    }
    @WrapOperation(method="attack",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;getEnchantedDamage(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)F"))
    private float campaign$enchantmentDamage(Player player,net.minecraft.world.entity.Entity target,float base,
            net.minecraft.world.damagesource.DamageSource source,Operation<Float> original) {
        int era=CampaignRules.era(player);
        if(!HistoricalMeleeCatalog.active(era,CampaignRules.firstRelease(player.level())))return original.call(player,target,base,source);
        // The host simulates client attacks without enchanting damage. Do not consume the server's RNG.
        if(player.level().isClientSide())return base;
        if(era<6||!(target instanceof net.minecraft.world.entity.LivingEntity))return base;
        var registry=player.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var weapon=player.getMainHandItem();
        int sharp=weapon.getEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS));
        int smite=weapon.getEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SMITE));
        int bane=weapon.getEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.BANE_OF_ARTHROPODS));
        int category=target.is(net.minecraft.tags.EntityTypeTags.UNDEAD)?1:
                target.is(net.minecraft.tags.EntityTypeTags.ARTHROPOD)?2:0;
        return base+br.com.mura.betaminecraftmod.progression.ReleaseEnchantmentDamage.roll(category,sharp,smite,bane);
    }
    @WrapOperation(method="itemAttackInteraction",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/enchantment/EnchantmentHelper;doPostAttackEffectsWithItemSource(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/item/ItemStack;)V"))
    private void campaign$oldBaneEffects(net.minecraft.server.level.ServerLevel level,net.minecraft.world.entity.Entity target,
            net.minecraft.world.damagesource.DamageSource source,net.minecraft.world.item.ItemStack weapon,Operation<Void> original) {
        Player player=(Player)(Object)this;int era=CampaignRules.era(player);
        if(!HistoricalMeleeCatalog.active(era,CampaignRules.firstRelease(level))){original.call(level,target,source,weapon);return;}
        if(era<6)return;
        var bane=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.BANE_OF_ARTHROPODS);
        if(weapon.getEnchantmentLevel(bane)>0) {
            var effects=weapon.copy();
            net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(effects,
                    mutable->mutable.removeIf(holder->holder.is(net.minecraft.world.item.enchantment.Enchantments.BANE_OF_ARTHROPODS)));
            original.call(level,target,source,effects);
        } else original.call(level,target,source,weapon);
    }
    @Inject(method = "canCriticalAttack", at = @At("HEAD"), cancellable = true)
    private void campaign$criticalConditions(net.minecraft.world.entity.Entity target, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player)(Object)this;
        int era = CampaignRules.era(player);
        if (!HistoricalMeleeCatalog.active(era, CampaignRules.firstRelease(player.level()))) return;
        int flags = (player.getDeltaMovement().y < 0 ? 1 : 0) | (player.fallDistance > 0 ? 2 : 0)
                | (player.onGround() ? 4 : 0) | (player.onClimbable() ? 8 : 0) | (player.isInWater() ? 16 : 0)
                | (player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS) ? 32 : 0)
                | (player.isPassenger() ? 64 : 0) | (target instanceof net.minecraft.world.entity.LivingEntity ? 128 : 0)
                | (player.isSprinting() ? 256 : 0);
        cir.setReturnValue(HistoricalCriticalRules.critical(era, flags));
    }
    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/player/CriticalHitEvent;getDamageMultiplier()F"))
    private float campaign$criticalAmount(net.neoforged.neoforge.event.entity.player.CriticalHitEvent event,
            Operation<Float> original, @Local(ordinal = 0) float base,
            @Share("oldCritical") LocalBooleanRef enabled, @Share("oldCriticalDamage") LocalFloatRef result) {
        float multiplier = original.call(event);
        Player player = (Player)(Object)this;
        int era = CampaignRules.era(player);
        // Respect explicit multiplier changes made by other mods. Native events use 1.5.
        if (HistoricalMeleeCatalog.active(era, CampaignRules.firstRelease(player.level()))
                && era >= 5 && multiplier == 1.5F && base > 0 && base == (int)base) {
            result.set(HistoricalCriticalRules.damage(era, (int)base, player.getRandom()::nextInt));
            enabled.set(true);
        }
        return multiplier;
    }
    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "attack", at = @At("STORE"), ordinal = 0,
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/player/CriticalHitEvent;getDamageMultiplier()F"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSweepAttack(ZZZ)Z")))
    private float campaign$integerCritical(float modern, @Share("oldCritical") LocalBooleanRef enabled,
            @Share("oldCriticalDamage") LocalFloatRef result) {
        return enabled.get() ? result.get() : modern;
    }
    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeExtraKnockback(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/damagesource/DamageSource;FZ)V"))
    private void campaign$extraKnockback(Player player, net.minecraft.world.entity.Entity target, float amount,
            net.minecraft.world.phys.Vec3 oldMovement, net.minecraft.world.damagesource.DamageSource source,
            float damage, boolean effect, Operation<Void> original) {
        int era=CampaignRules.era(player);
        if(!HistoricalMeleeCatalog.active(era,CampaignRules.firstRelease(player.level()))) {
            original.call(player,target,amount,oldMovement,source,damage,effect);return;
        }
        int enchant=0;
        if(era==6 && target instanceof net.minecraft.world.entity.LivingEntity) {
            var holder=player.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK);
            enchant=player.getMainHandItem().getEnchantmentLevel(holder);
        }
        var impulse=br.com.mura.betaminecraftmod.progression.HistoricalExtraKnockback.impulse(era,player.getYRot(),player.isSprinting(),enchant);
        if(impulse.applied()) {
            target.push(impulse.x(),impulse.y(),impulse.z());
            player.setDeltaMovement(player.getDeltaMovement().multiply(.6,1,.6));
            player.setSprinting(false);
        }
        // Keep the host's explicit player-velocity packet/restoration path.
        if(target instanceof net.minecraft.server.level.ServerPlayer remote && target.syncVelocity) {
            remote.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(target));
            target.syncVelocity=false;target.setDeltaMovement(oldMovement);
        }
    }
    @Inject(method = "attackVisualEffects", at = @At("HEAD"), cancellable = true)
    private void campaign$noModernAttackEffects(CallbackInfo ci) {
        if (CampaignRules.legacyCombat((Player)(Object)this)) ci.cancel();
    }
    @Inject(method = "playServerSideSound", at = @At("HEAD"), cancellable = true)
    private void campaign$noModernAttackSound(CallbackInfo ci) {
        if (CampaignRules.legacyCombat((Player)(Object)this)) ci.cancel();
    }
    @Inject(method = "damageStatsAndHearts", at = @At("HEAD"), cancellable = true)
    private void campaign$noDamageParticles(CallbackInfo ci) {
        if (CampaignRules.legacyCombat((Player)(Object)this)) ci.cancel();
    }
}
