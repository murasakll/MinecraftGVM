package br.com.mura.betaminecraftmod.progression;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import br.com.mura.betaminecraftmod.world.InfdevChunkGenerator;
import net.minecraft.server.level.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/** Main-thread requests wait without acquiring world-generation references or generating placeholders. */
@EventBusSubscriber(modid="betaminecraftmod")
public final class LockedGeneration {
    private record Key(GenerationChunkHolder holder,ChunkStatus status) { }
    private record Pending(ChunkMap map,CompletableFuture<ChunkResult<ChunkAccess>> future) { }
    private static final Map<ServerLevel,Map<Key,Pending>> WAITING=new IdentityHashMap<>();
    private LockedGeneration() { }
    public static CompletableFuture<ChunkResult<ChunkAccess>> waitFor(ServerLevel level,GenerationChunkHolder holder,ChunkStatus status,ChunkMap map) {
        return WAITING.computeIfAbsent(level,k->new HashMap<>()).computeIfAbsent(new Key(holder,status),k->new Pending(map,new CompletableFuture<>())).future();
    }
    @SubscribeEvent public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var requests=WAITING.get(level);
        if(requests==null) return;
        var generator=(br.com.mura.betaminecraftmod.world.CampaignGenerationGate)level.getChunkSource().getGenerator();
        var ready=new ArrayList<Map.Entry<Key,Pending>>();
        requests.entrySet().removeIf(e->{
            var key=e.getKey();var pos=key.holder().getPos();
            if(ChunkLevel.generationStatus(key.holder().getTicketLevel())==null) {e.getValue().future().complete(GenerationChunkHolder.UNLOADED_CHUNK);return true;}
            if(!generator.locked(pos.x(),pos.z())) {ready.add(Map.entry(key,e.getValue()));return true;}
            return false;
        });
        for(var entry:ready) entry.getKey().holder().scheduleChunkGenerationTask(entry.getKey().status(),entry.getValue().map()).whenComplete((result,error)->{
            if(error!=null) entry.getValue().future().completeExceptionally(error);else entry.getValue().future().complete(result);
        });
    }
    @SubscribeEvent public static void stopping(ServerStoppingEvent event) {
        WAITING.entrySet().removeIf(entry->{
            if(entry.getKey().getServer()!=event.getServer()) return false;
            entry.getValue().values().forEach(p->p.future().complete(GenerationChunkHolder.UNLOADED_CHUNK));return true;
        });
    }
}
