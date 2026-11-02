package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Consumable.class)
public abstract class ConsumableMixin {
    @Inject(method = "startConsuming", at = @At("HEAD"), cancellable = true)
    private void campaign$instantHealing(LivingEntity user, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(user instanceof Player player) || !CampaignRules.legacyFood(player)) return;
        var food = stack.get(DataComponents.FOOD);
        if (food == null) return;
        if (!player.isHurt()) { cir.setReturnValue(InteractionResult.FAIL); return; }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.heal(CampaignRules.healing(stack, food.nutrition()));
            serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        }
        ItemStack result;
        if (stack.is(Items.MUSHROOM_STEW)) result = ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BOWL));
        else { stack.consume(1, player); result = stack; }
        cir.setReturnValue(InteractionResult.CONSUME.heldItemTransformedTo(result));
    }
}
