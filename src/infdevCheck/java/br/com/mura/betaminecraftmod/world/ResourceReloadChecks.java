package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.client.EraResourceReloadQueue;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public final class ResourceReloadChecks {
    public static void run() {
        var entry=new br.com.mura.betaminecraftmod.client.EraEntryState();
        if(!entry.begin() || !entry.inProgress() || entry.begin()) throw new AssertionError("Duplicate world entry accepted");
        entry.joining();
        if(!entry.inProgress()) throw new AssertionError("Internal disconnect would discard prepared packs");
        entry.clear();
        if(entry.inProgress() || !entry.begin()) throw new AssertionError("Confirmed/cancelled entry cannot restart");
        entry.clear();
        var started=new ArrayList<Integer>();
        var jobs=new ArrayList<CompletableFuture<Void>>();
        var queue=new EraResourceReloadQueue(era -> {
            started.add(era);
            var job=new CompletableFuture<Void>(); jobs.add(job); return job;
        },Runnable::run);
        var first=queue.request(0);
        var second=queue.request(1);
        var logout=queue.request(-1);
        if(!started.equals(java.util.List.of(0)) || !queue.busy()) throw new AssertionError("Overlapping era reloads");
        jobs.get(0).complete(null);
        if(!first.isDone() || !started.equals(java.util.List.of(0,1)) || logout.isDone()) throw new AssertionError("Era transition ordering");
        jobs.get(1).completeExceptionally(new IllegalStateException("broken pack"));
        if(!second.isCompletedExceptionally() || !started.equals(java.util.List.of(0,1,-1))) throw new AssertionError("Recovery blocked by failed reload");
        jobs.get(2).complete(null);
        if(queue.busy() || !logout.isDone()) throw new AssertionError("Completed reload still blocks entry");
        System.out.println("Resource loading: serialized transitions, failure propagation and logout recovery passed.");
    }
}
