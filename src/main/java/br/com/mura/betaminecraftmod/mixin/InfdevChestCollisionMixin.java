package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class InfdevChestCollisionMixin {
    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void campaign$cachedCollision(BlockGetter level, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        if((Object)this instanceof BlockState state){var shape=br.com.mura.betaminecraftmod.progression.HistoricalBlockShapes.collision(state,level);if(shape!=null)cir.setReturnValue(shape);}
    }
    @Inject(method="getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",at=@At("HEAD"),cancellable=true)
    private void campaign$contextCollision(BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context,CallbackInfoReturnable<VoxelShape> cir){if((Object)this instanceof BlockState state){var shape=br.com.mura.betaminecraftmod.progression.HistoricalBlockShapes.collision(state,level);if(shape!=null)cir.setReturnValue(shape);}}
}
