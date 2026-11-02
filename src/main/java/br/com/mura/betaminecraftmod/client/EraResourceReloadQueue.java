package br.com.mura.betaminecraftmod.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntFunction;

/** Serializes resource work; an era change cannot mutate a repository during an earlier reload. */
public final class EraResourceReloadQueue {
    private final IntFunction<CompletableFuture<Void>> load;
    private final Executor executor;
    private CompletableFuture<Void> tail = CompletableFuture.completedFuture(null);
    public EraResourceReloadQueue(IntFunction<CompletableFuture<Void>> load, Executor executor) { this.load=load; this.executor=executor; }
    public CompletableFuture<Void> request(int era) {
        tail = tail.handle((result,error) -> null).thenComposeAsync(ignored -> load.apply(era),executor);
        return tail;
    }
    public boolean busy() { return !tail.isDone(); }
}
