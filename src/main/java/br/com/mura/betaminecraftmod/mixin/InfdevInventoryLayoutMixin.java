package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.client.EraVisuals;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InfdevInventoryLayoutMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void campaign$alignCrafting(CallbackInfo ci) {
        var screen = (InventoryScreen)(Object)this;
        var slots = screen.getMenu().slots;
        boolean historical = EraVisuals.active().historical();
        // Reference 20100617-1531: result (144,36), inputs (88,26), spacing 18.
        for (int index = 0; index < 5; index++) {
            var slot = (SlotPositionAccessor)slots.get(index);
            int x = index == 0 ? (historical ? 144 : 154) : (historical ? 88 : 98) + (index - 1) % 2 * 18;
            int y = index == 0 ? (historical ? 36 : 28) : (historical ? 26 : 18) + (index - 1) / 2 * 18;
            slot.campaign$setX(x);
            slot.campaign$setY(y);
        }
        var labels = (ContainerLabelAccessor)screen;
        labels.campaign$setTitleX(historical ? 86 : 97);
        labels.campaign$setTitleY(historical ? 16 : 6);
    }
}
