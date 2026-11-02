package br.com.mura.betaminecraftmod.progression;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;

/** Per-entity corrections with native modifier IDs; no global registry changes or stacking bonuses. */
public final class ReleasePotionEffects {
    private static final String MARKER="betaminecraftmod:potion_attributes_1_0";
    private ReleasePotionEffects(){}
    public static void refresh(LivingEntity entity){
        if(!(entity.level() instanceof ServerLevel level))return;
        boolean historical=ReleaseBrewing.active(level),owned=entity.getPersistentData().getBooleanOr(MARKER,false);
        if(!historical&&!owned)return;
        boolean changed=update(entity,MobEffects.STRENGTH,historical)|update(entity,MobEffects.WEAKNESS,historical);
        if(historical){if(changed)entity.getPersistentData().putBoolean(MARKER,true);}else entity.getPersistentData().remove(MARKER);
    }
    private static boolean update(LivingEntity entity,Holder<MobEffect> effect,boolean historical){
        var current=entity.getEffect(effect);
        if(current==null){if(!historical)effect.value().removeAttributeModifiers(entity.getAttributes());return false;}
        boolean[] changed={false};int amplifier=current.getAmplifier();
        effect.value().createModifiers(amplifier,(attribute,prototype)->{
            if(!attribute.is(Attributes.ATTACK_DAMAGE))return;var instance=entity.getAttribute(attribute);if(instance==null)return;
            double amount=historical?effect==MobEffects.STRENGTH?ReleasePotionRules.strength(amplifier):ReleasePotionRules.weakness(amplifier):prototype.amount();
            var existing=instance.getModifier(prototype.id());if(existing!=null&&existing.amount()==amount&&existing.operation()==prototype.operation())return;
            instance.removeModifier(prototype.id());instance.addPermanentModifier(new AttributeModifier(prototype.id(),amount,prototype.operation()));changed[0]=true;
        });return changed[0];
    }
}
