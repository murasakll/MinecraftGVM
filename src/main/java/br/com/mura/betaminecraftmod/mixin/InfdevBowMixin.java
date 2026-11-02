package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.sounds.*;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class InfdevBowMixin {
    @Inject(method="use",at=@At("HEAD"),cancellable=true)
    private void campaign$instantShot(Level level,Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir){
        if(!CampaignRules.instantBow(level))return;
        int ammo=-1;
        for(int slot=0;slot<player.getInventory().getContainerSize();slot++)if(player.getInventory().getItem(slot).is(Items.ARROW)){ammo=slot;break;}
        if(ammo<0&&!player.hasInfiniteMaterials()){cir.setReturnValue(InteractionResult.FAIL);return;}
        if(!level.isClientSide()){
            var arrow=new Arrow(level,player,new ItemStack(Items.ARROW),null);
            float yaw=player.getYRot()*(float)Math.PI/180,pitch=player.getXRot()*(float)Math.PI/180;
            arrow.setPos(player.getX()-Mth.cos(yaw)*.16f,player.getEyeY()-.1f,player.getZ()-Mth.sin(yaw)*.16f);
            double x=-Mth.sin(yaw)*Mth.cos(pitch),y=-Mth.sin(pitch),z=Mth.cos(yaw)*Mth.cos(pitch);
            float length=Mth.sqrt((float)(x*x+y*y+z*z));
            arrow.setDeltaMovement((x/length+player.getRandom().nextGaussian()*.0075f)*1.5f,
                (y/length+player.getRandom().nextGaussian()*.0075f)*1.5f,(z/length+player.getRandom().nextGaussian()*.0075f)*1.5f);
            arrow.setYRot((float)(Math.atan2(arrow.getDeltaMovement().x,arrow.getDeltaMovement().z)*180/Math.PI));
            arrow.setXRot((float)(Math.atan2(arrow.getDeltaMovement().y,arrow.getDeltaMovement().horizontalDistance())*180/Math.PI));
            if(player.hasInfiniteMaterials())arrow.pickup=net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.CREATIVE_ONLY;
            if(level.addFreshEntity(arrow)){
                if(!player.hasInfiniteMaterials())player.getInventory().removeItem(ammo,1);
                level.playSound(null,player.getX(),player.getY(),player.getZ(),SoundEvents.ARROW_SHOOT,SoundSource.PLAYERS,1,1/(player.getRandom().nextFloat()*.4f+.8f));
            }
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
