package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.world.*;
import br.com.mura.betaminecraftmod.progression.LockedGeneration;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.*;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GenerationChunkHolder.class)
public abstract class CampaignGenerationHolderMixin {
    @Inject(method="scheduleChunkGenerationTask",at=@At("HEAD"),cancellable=true)
    private void campaign$waitForUnlock(ChunkStatus status,ChunkMap map,CallbackInfoReturnable<CompletableFuture<ChunkResult<ChunkAccess>>> cir) {
        var holder=(GenerationChunkHolder)(Object)this;
        var level=((CampaignChunkMapAccess)map).campaign$level();
        var pos=holder.getPos();
        if (level.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.CampaignGenerationGate generator && generator.locked(pos.x(),pos.z()))
            cir.setReturnValue(LockedGeneration.waitFor(level,holder,status,map));
    }
    @Inject(method="applyStep",at=@At("HEAD"),cancellable=true)
    private void campaign$airDependency(ChunkStep step,GeneratingChunkMap map,StaticCache2D<GenerationChunkHolder> cache,CallbackInfoReturnable<CompletableFuture<ChunkResult<ChunkAccess>>> cir) {
        if ((Object)this instanceof LockedChunkDependency air)
            cir.setReturnValue(CompletableFuture.completedFuture(ChunkResult.of(air.getLatestChunk())));
    }
}
