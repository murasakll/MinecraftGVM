package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.world.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Synchronous edge queries must never block the server waiting for a player decision. */
@Mixin(ServerChunkCache.class)
public abstract class CampaignChunkQueriesMixin {
    @Shadow @Final private ServerLevel level;
    @Inject(method="getChunk",at=@At("HEAD"),cancellable=true)
    private void campaign$edgeQuery(int x,int z,ChunkStatus status,boolean load,CallbackInfoReturnable<ChunkAccess> cir) {
        if(level.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.CampaignGenerationGate generator && generator.locked(x,z))
            cir.setReturnValue(new LockedChunkDependency(level,new ChunkPos(x,z)).getLatestChunk());
    }
    @Inject(method="getChunkFuture",at=@At("HEAD"),cancellable=true)
    private void campaign$edgeFuture(int x,int z,ChunkStatus status,boolean load,CallbackInfoReturnable<CompletableFuture<ChunkResult<ChunkAccess>>> cir) {
        if(level.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.CampaignGenerationGate generator && generator.locked(x,z))
            cir.setReturnValue(CompletableFuture.completedFuture(ChunkResult.of(new LockedChunkDependency(level,new ChunkPos(x,z)).getLatestChunk())));
    }
}
