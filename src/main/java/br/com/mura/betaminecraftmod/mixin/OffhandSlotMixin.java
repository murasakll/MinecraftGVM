package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class OffhandSlotMixin {
    @Shadow @Final public Container container;
    @Shadow public abstract int getContainerSlot();
    private boolean campaign$disabled() {
        return container instanceof Inventory inventory && getContainerSlot() == 40 && CampaignRules.legacyCombat(inventory.player);
    }
    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void campaign$hide(CallbackInfoReturnable<Boolean> cir) { if (campaign$disabled()) cir.setReturnValue(false); }
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void campaign$place(net.minecraft.world.item.ItemStack stack, CallbackInfoReturnable<Boolean> cir) { if (campaign$disabled()) cir.setReturnValue(false); }
}
