package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class HistoricalItemDamageMixin {
    @Inject(method="hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",at=@At("HEAD"))
    private void campaign$itemLimits(int amount,ServerLevel level,LivingEntity owner,Consumer<ItemStack> callback,CallbackInfo ci){
        HistoricalItemLimits.apply((ItemStack)(Object)this,CampaignRules.era(level));
    }
}
