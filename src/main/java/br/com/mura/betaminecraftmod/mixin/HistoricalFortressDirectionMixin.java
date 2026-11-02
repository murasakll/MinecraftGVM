package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.world.HistoricalFortressLayout;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructurePiece.class)
public abstract class HistoricalFortressDirectionMixin {
    @Inject(method="getRandomHorizontalDirection",at=@At("RETURN"),cancellable=true)
    private static void campaign$originalDirectionOrder(RandomSource random,CallbackInfoReturnable<Direction> cir){if(HistoricalFortressLayout.original())cir.setReturnValue(cir.getReturnValue().getOpposite());}
}
