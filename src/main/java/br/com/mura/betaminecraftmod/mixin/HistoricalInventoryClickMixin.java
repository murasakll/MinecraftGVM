package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class HistoricalInventoryClickMixin {
    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void campaign$noOffhandSwap(int slot, int button, ContainerInput input, Player player, CallbackInfo ci) {
        if (input == ContainerInput.SWAP && button == 40 && CampaignRules.legacyCombat(player)) ci.cancel();
    }
}
