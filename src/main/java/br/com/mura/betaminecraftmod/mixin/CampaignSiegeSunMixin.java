package br.com.mura.betaminecraftmod.mixin;
import br.com.mura.betaminecraftmod.quests.CampaignSiege;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Event-only adaptation: daylight must not complete the combat challenge for the player. */
@Mixin(Mob.class)
public abstract class CampaignSiegeSunMixin {
    @Inject(method="isSunBurnTick",at=@At("HEAD"),cancellable=true)
    private void campaign$eventDaylight(CallbackInfoReturnable<Boolean> ci){if(CampaignSiege.tagged((Mob)(Object)this))ci.setReturnValue(false);}
}
