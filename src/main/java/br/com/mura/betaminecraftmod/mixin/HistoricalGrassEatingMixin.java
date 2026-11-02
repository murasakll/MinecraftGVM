package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.InfdevContent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EatBlockGoal.class)
public abstract class HistoricalGrassEatingMixin {
    @Shadow @Final private Mob mob;
    @Inject(method="canUse",at=@At("HEAD"),cancellable=true)
    private void campaign$noGrassEating(CallbackInfoReturnable<Boolean> cir){if(InfdevContent.legacy(mob.level()))cir.setReturnValue(false);}
}
