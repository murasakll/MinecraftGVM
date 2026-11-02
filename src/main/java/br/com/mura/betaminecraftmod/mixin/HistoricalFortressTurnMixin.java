package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.world.HistoricalFortressLayout;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Before 1.6, fortress turns neither selected chests nor consumed their selection RNG. */
@Mixin({NetherFortressPieces.CastleSmallCorridorLeftTurnPiece.class,NetherFortressPieces.CastleSmallCorridorRightTurnPiece.class})
public abstract class HistoricalFortressTurnMixin {
    @Redirect(method="<init>(ILnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/core/Direction;)V",at=@At(value="INVOKE",target="Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int campaign$noLaterChestRoll(RandomSource random,int bound){return HistoricalFortressLayout.original()?1:random.nextInt(bound);}
}
