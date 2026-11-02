package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.world.InfdevChunkGenerator;
import br.com.mura.betaminecraftmod.progression.CampaignProgress;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class CampaignLevelGenerationMixin {
    @Shadow @Final private PersistentEntitySectionManager<Entity> entityManager;

    /** Native spawn readiness includes a three-chunk square, which can cross the locked frontier. */
    @Inject(method="waitForEntities",at=@At("HEAD"),cancellable=true)
    private void campaign$waitForPlayableEntities(ChunkPos center,int radius,CallbackInfo ci) {
        var level=(ServerLevel)(Object)this;
        if(!(level.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.CampaignGenerationGate generator)) return;
        var chunks=ChunkPos.rangeClosed(center,radius).toList();
        level.getServer().managedBlock(()->{
            entityManager.processPendingLoads();
            for(var pos:chunks)
                if(!generator.locked(pos.x(),pos.z()) && !level.areEntitiesLoaded(pos.pack())) return false;
            return true;
        });
        ci.cancel();
    }

    @Inject(method="<init>",at=@At("TAIL"))
    private void campaign$restoreUnlockedGeneration(CallbackInfo ci) {
        var level=(ServerLevel)(Object)this;
        if(level.getChunkSource().getGenerator() instanceof InfdevChunkGenerator generator){
            generator.attachLevel(level);
            generator.unlockThrough(level.getDataStorage().computeIfAbsent(CampaignProgress.TYPE).era());
        }
    }
}
