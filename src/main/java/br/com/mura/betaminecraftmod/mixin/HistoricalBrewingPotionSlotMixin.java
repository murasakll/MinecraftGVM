package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets="net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public abstract class HistoricalBrewingPotionSlotMixin {
    @Inject(method="mayPlace",at=@At("HEAD"),cancellable=true)
    private void campaign$potions(ItemStack stack,CallbackInfoReturnable<Boolean> result){var slot=(Slot)(Object)this;boolean active=slot.container instanceof BlockEntity be?ReleaseBrewing.active(be.getLevel()):ReleaseBrewing.clientActive();if(active)result.setReturnValue(stack.is(Items.GLASS_BOTTLE)||ReleaseBrewing.metadata(stack)>=0);}
}
