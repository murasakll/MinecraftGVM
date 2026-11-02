package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.InfdevContent;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class HistoricalHoeMixin {
    @WrapMethod(method="useOn")
    private InteractionResult campaign$grassSeeds(UseOnContext context,Operation<InteractionResult> original) {
        var level=context.getLevel();var pos=context.getClickedPos();
        boolean grass=InfdevContent.legacy(level)&&net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(context.getItemInHand().getItem()).getPath().endsWith("_hoe")&&level.getBlockState(pos).is(Blocks.GRASS_BLOCK);
        var result=original.call(context);
        if(level instanceof ServerLevel server&&InfdevContent.legacy(server)&&grass&&result.consumesAction()
                &&level.getBlockState(pos).is(Blocks.FARMLAND)&&level.getRandom().nextInt(8)==0)
            Block.popResource(level,pos.above(),new ItemStack(Items.WHEAT_SEEDS));
        return result;
    }
}
