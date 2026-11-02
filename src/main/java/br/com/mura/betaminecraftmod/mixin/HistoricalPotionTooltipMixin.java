package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PotionContents.class)
public abstract class HistoricalPotionTooltipMixin {
    @WrapOperation(method="addPotionTooltip",at=@At(value="INVOKE",target="Lnet/minecraft/world/effect/MobEffect;createModifiers(ILjava/util/function/BiConsumer;)V"))
    private static void campaign$numbers(MobEffect effect,int amplifier,BiConsumer<Holder<Attribute>,AttributeModifier> consumer,Operation<Void> original){
        if(!ReleaseBrewing.clientActive()||(effect!=MobEffects.STRENGTH.value()&&effect!=MobEffects.WEAKNESS.value())){original.call(effect,amplifier,consumer);return;}
        original.call(effect,amplifier,(BiConsumer<Holder<Attribute>,AttributeModifier>)(attribute,modifier)->consumer.accept(attribute,attribute.is(Attributes.ATTACK_DAMAGE)?new AttributeModifier(modifier.id(),effect==MobEffects.STRENGTH.value()?ReleasePotionRules.strength(amplifier):ReleasePotionRules.weakness(amplifier),modifier.operation()):modifier));
    }
}
