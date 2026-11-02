package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.world.HistoricalPortalView;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Entity.class)
public abstract class HistoricalPortalCooldownMixin {
    @Inject(method="getDimensionChangingDelay",at=@At("HEAD"),cancellable=true)
    private void campaign$delay(CallbackInfoReturnable<Integer> result){var self=(Entity)(Object)this;if(HistoricalPortalView.historical(self.level())&&self.portalProcess!=null&&self.portalProcess.isSamePortal((NetherPortalBlock)Blocks.NETHER_PORTAL))result.setReturnValue(10);}
    @Inject(method="setAsInsidePortal",at=@At("HEAD"),cancellable=true)
    private void campaign$refresh(Portal portal,BlockPos pos,CallbackInfo ci){var self=(Entity)(Object)this;if(portal==Blocks.NETHER_PORTAL&&HistoricalPortalView.historical(self.level())&&self.isOnPortalCooldown()){self.setPortalCooldown(10);ci.cancel();}}
}
