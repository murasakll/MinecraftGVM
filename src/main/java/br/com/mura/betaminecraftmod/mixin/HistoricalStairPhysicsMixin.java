package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.HistoricalBlockShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class HistoricalStairPhysicsMixin {
    @Inject(method="getCollisionShape",at=@At("HEAD"),cancellable=true)
    private void campaign$collisionNotOutline(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context,CallbackInfoReturnable<VoxelShape> cir){var shape=HistoricalBlockShapes.collision(state,level);if(shape!=null)cir.setReturnValue(shape);}
}
