package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public abstract class InfdevChestShapeMixin {
    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void campaign$fullBlock(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (state.is(Blocks.CHEST) && CampaignRules.chestHitbox(level)) cir.setReturnValue(Shapes.block());
    }
}
