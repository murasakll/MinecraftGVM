package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.client.EraWorldEntry;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldOpenFlows.class)
public abstract class EraWorldEntryMixin {
    @Unique private boolean campaign$prepared;
    @Shadow public abstract void openWorld(String id,Runnable onCancel);
    @Inject(method="openWorld",at=@At("HEAD"),cancellable=true)
    private void campaign$prepare(String id,Runnable onCancel,CallbackInfo ci) {
        if (campaign$prepared) return;
        ci.cancel();
        EraWorldEntry.open(id,() -> {
            campaign$prepared=true;
            try { openWorld(id,() -> { EraWorldEntry.cancelled(); onCancel.run(); }); } finally { campaign$prepared=false; }
        });
    }
}
