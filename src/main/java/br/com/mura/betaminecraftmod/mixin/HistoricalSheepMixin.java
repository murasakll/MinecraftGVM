package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.InfdevContent;
import net.minecraft.world.entity.animal.sheep.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheep.class)
public abstract class HistoricalSheepMixin {
    @Inject(method="ate",at=@At("HEAD"),cancellable=true)
    private void campaign$noWoolRegrowth(CallbackInfo ci) {
        if(InfdevContent.legacy(((Sheep)(Object)this).level()))ci.cancel();
    }
}
