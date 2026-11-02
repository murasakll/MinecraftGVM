package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import br.com.mura.betaminecraftmod.world.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Historical arena integration. Dragon movement/attacks are audited separately. */
@Mixin(EnderDragonFight.class)
public abstract class HistoricalEndFightMixin {
    @Shadow private ServerLevel level;
    @Shadow private ServerBossEvent dragonEvent;
    @Shadow private java.util.UUID dragonUUID;
    @Shadow private BlockPos exitPortalLocation;
    @Shadow private boolean hasPreviouslyKilledDragon;
    @Shadow private boolean dragonKilled;
    @Shadow private int ticksSinceCrystalsScanned;
    @Shadow private int aliveCrystals;
    @Unique private boolean campaign$historical() {
        int era=CampaignRules.era(level);
        return era>=0&&era<=6&&level.getChunkSource().getGenerator() instanceof HistoricalEndChunkGenerator;
    }
    @Inject(method="updateCrystalCount",at=@At("HEAD"),cancellable=true)
    private void campaign$countHistoricalCrystals(CallbackInfo ci) {
        if(!campaign$historical())return;
        ticksSinceCrystalsScanned=0;
        // Entity queries inspect loaded entities; this does not generate or request terrain.
        aliveCrystals=level.getEntitiesOfClass(EndCrystal.class,new AABB(-512,0,-512,512,256,512),crystal->crystal.isAlive()).size();
        ci.cancel();
    }
    @Inject(method={"spawnNewGateway()V","spawnNewGateway(Lnet/minecraft/core/BlockPos;)V","tryRespawn"},at=@At("HEAD"),cancellable=true)
    private void campaign$noFutureEndMechanics(CallbackInfo ci){if(campaign$historical())ci.cancel();}
    @Inject(method="spawnExitPortal",at=@At("HEAD"),cancellable=true)
    private void campaign$noPrematurePodium(boolean activated,CallbackInfo ci){if(campaign$historical())ci.cancel();}
    @Inject(method="setDragonKilled",at=@At("HEAD"),cancellable=true)
    private void campaign$deathPortal(EnderDragon dragon,CallbackInfo ci) {
        if(!campaign$historical())return;
        ci.cancel();
        if(!dragon.getUUID().equals(dragonUUID))return;
        int x=net.minecraft.util.Mth.floor(dragon.getX()),z=net.minecraft.util.Mth.floor(dragon.getZ());
        if(!HistoricalEndPodium.place(level,(CampaignGenerationGate)level.getChunkSource().getGenerator(),x,z))return;
        exitPortalLocation=new BlockPos(x,64,z);
        dragonEvent.setProgress(0);dragonEvent.setVisible(false);
        hasPreviouslyKilledDragon=true;dragonKilled=true;
        ((SavedData)(Object)this).setDirty();
    }
}
