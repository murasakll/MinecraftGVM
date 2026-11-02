package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.ReleaseBrewing;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(targets="net.minecraft.world.effect.HealOrHarmMobEffect")
public abstract class HistoricalInstantPotionMixin {
    @ModifyConstant(method={"applyEffectTick","applyInstantaneousEffect"},constant=@Constant(intValue=4))
    private int campaign$healing(int original,ServerLevel level){return ReleaseBrewing.active(level)?6:original;}
}
