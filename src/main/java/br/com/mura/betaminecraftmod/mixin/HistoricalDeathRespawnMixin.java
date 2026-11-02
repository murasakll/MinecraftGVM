package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.HistoricalDeathExperienceAccess;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class HistoricalDeathRespawnMixin {
    @Inject(method="restoreFrom",at=@At("HEAD"))
    private void campaign$resolveBeforeRespawn(ServerPlayer previous,boolean restoreAll,CallbackInfo ci) {
        // A fast modern respawn can replace the corpse before Beta's 21st tick.
        if(!restoreAll)((HistoricalDeathExperienceAccess)previous).campaign$resolveDeathExperience();
    }
}
