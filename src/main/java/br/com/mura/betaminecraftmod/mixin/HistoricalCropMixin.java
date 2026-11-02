package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.InfdevContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public abstract class HistoricalCropMixin {
    @Shadow protected static float getGrowthSpeed(BlockState state,BlockGetter level,BlockPos pos){throw new AssertionError();}
    @Inject(method="randomTick",at=@At("HEAD"),cancellable=true)
    private void campaign$oldWheatGrowth(BlockState state,ServerLevel level,BlockPos pos,RandomSource random,CallbackInfo ci) {
        if(!InfdevContent.legacy(level)||!state.is(Blocks.WHEAT))return;
        ci.cancel();if(!level.isAreaLoaded(pos,1)||level.getRawBrightness(pos.above(),0)<9)return;
        var crop=(CropBlock)(Object)this;int age=crop.getAge(state);
        if(age>=crop.getMaxAge())return;
        float speed=getGrowthSpeed(state,level,pos);
        if(net.neoforged.neoforge.common.CommonHooks.canCropGrow(level,pos,state,random.nextInt(Math.max(1,(int)(100F/speed)))==0)) {
            level.setBlock(pos,crop.getStateForAge(age+1),2);
            net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level,pos,state);
        }
    }
}
