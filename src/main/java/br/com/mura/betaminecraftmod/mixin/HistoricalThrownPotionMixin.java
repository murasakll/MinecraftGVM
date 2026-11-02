package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.ReleaseBrewing;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.throwableitemprojectile.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractThrownPotion.class)
public abstract class HistoricalThrownPotionMixin {
    @Inject(method="affectEntitiesAround",at=@At("HEAD"),cancellable=true)
    private void campaign$noModernWater(ServerLevel level,PotionContents contents,CallbackInfo ci){if((Object)this instanceof ThrownSplashPotion&&ReleaseBrewing.active(level))ci.cancel();}
    @Inject(method="onHitBlock",at=@At("HEAD"),cancellable=true)
    private void campaign$noDousing(BlockHitResult hit,CallbackInfo ci){if((Object)this instanceof ThrownSplashPotion potion&&ReleaseBrewing.active(potion.level()))ci.cancel();}
}
