package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import java.util.Optional;
import br.com.mura.betaminecraftmod.world.InfdevChunkGenerator;
import br.com.mura.betaminecraftmod.world.LockedChunkDependency;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Reserved exterior chunks are transient dependencies, never persisted as empty future terrain. */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Shadow @Final private ServerLevel level;
    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void campaign$doNotSaveReserved(ChunkAccess chunk, CallbackInfoReturnable<Boolean> cir) {
        if (level.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.CampaignGenerationGate gate && gate.locked(chunk.getPos().x(), chunk.getPos().z())) {
            chunk.tryMarkSaved();
            cir.setReturnValue(false);
        }
    }
    @Inject(method = "readChunk", at = @At("HEAD"), cancellable = true)
    private void campaign$doNotLoadReserved(ChunkPos pos, CallbackInfoReturnable<CompletableFuture<Optional<CompoundTag>>> cir) {
        if (level.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.CampaignGenerationGate gate && gate.locked(pos.x(), pos.z()))
            cir.setReturnValue(CompletableFuture.completedFuture(Optional.empty()));
    }
    @Inject(method="releaseGeneration",at=@At("HEAD"),cancellable=true)
    private void campaign$releaseAir(GenerationChunkHolder holder,CallbackInfo ci) {
        if(holder instanceof LockedChunkDependency) ci.cancel();
    }
    /** Client renderers need loaded air neighbours to mesh the last playable chunk. */
    @Inject(method="markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;)V",at=@At("HEAD"),cancellable=true)
    private void campaign$sendExteriorAir(net.minecraft.server.level.ServerPlayer player,ChunkPos pos,CallbackInfo ci) {
        if(level.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.CampaignGenerationGate generator && generator.locked(pos.x(),pos.z())) {
            var air=(net.minecraft.world.level.chunk.LevelChunk)new LockedChunkDependency(level,pos).getLatestChunk();
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket(air,level.getLightEngine(),null,null));
            ci.cancel();
        }
    }
    // Border chunks may tick and be sent without waiting for inaccessible neighbours.
    @Redirect(method="getChunkRangeFuture",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ChunkHolder;scheduleChunkGenerationTask(Lnet/minecraft/world/level/chunk/status/ChunkStatus;Lnet/minecraft/server/level/ChunkMap;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<ChunkResult<ChunkAccess>> campaign$rangeDependency(ChunkHolder holder,ChunkStatus status,ChunkMap map,ChunkHolder center,int range,java.util.function.IntFunction<ChunkStatus> distanceToStatus) {
        if(level.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.CampaignGenerationGate generator
            && !generator.locked(center.getPos().x(),center.getPos().z())
            && generator.locked(holder.getPos().x(),holder.getPos().z()))
            return CompletableFuture.completedFuture(ChunkResult.of(new LockedChunkDependency(level,holder.getPos()).getLatestChunk()));
        return holder.scheduleChunkGenerationTask(status,map);
    }
}

