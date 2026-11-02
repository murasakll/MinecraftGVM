package br.com.mura.betaminecraftmod.client.visual;

import br.com.mura.betaminecraftmod.client.HistoricalVegetationColors;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
public abstract class HistoricalVegetationMixin {
    @SuppressWarnings("deprecation")
    @Inject(method="createDefault",at=@At("RETURN"))
    private static void historicalSources(CallbackInfoReturnable<BlockColors> result) {
        var colors=result.getReturnValue();
        for(var block:java.util.List.of(Blocks.GRASS_BLOCK,Blocks.OAK_LEAVES,Blocks.SPRUCE_LEAVES,Blocks.BIRCH_LEAVES,Blocks.SHORT_GRASS,Blocks.FERN,Blocks.VINE)) {
            var original=colors.getTintSources(block.defaultBlockState());
            colors.register(original.stream().map(HistoricalVegetationColors::wrap).toList(),block);
        }
    }
}
