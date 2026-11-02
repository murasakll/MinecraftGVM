package br.com.mura.betaminecraftmod.world;

import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** Task-local read-only air dependency. Never registered, sent to players, or saved. */
public final class LockedChunkDependency extends GenerationChunkHolder {
    private final EmptyLevelChunk air;
    public LockedChunkDependency(ServerLevel level, ChunkPos pos) {
        super(pos);
        air = new EmptyLevelChunk(level, pos, level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().iterator().next());
    }
    @Override public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus status) { return air; }
    @Override public ChunkAccess getChunkIfPresent(ChunkStatus status) { return air; }
    @Override public ChunkAccess getLatestChunk() { return air; }
    @Override public ChunkStatus getPersistedStatus() { return ChunkStatus.FULL; }
    @Override protected void addSaveDependency(CompletableFuture<?> sync) { }
    @Override public int getTicketLevel() { return 0; }
    @Override public int getQueueLevel() { return 0; }
}
