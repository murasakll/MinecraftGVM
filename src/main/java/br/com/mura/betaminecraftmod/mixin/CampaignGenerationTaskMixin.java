package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.world.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ChunkGenerationTask.class)
public abstract class CampaignGenerationTaskMixin {
    @Redirect(method="lambda$create$0", at=@At(value="INVOKE",target="Lnet/minecraft/server/level/GeneratingChunkMap;acquireGeneration(J)Lnet/minecraft/server/level/GenerationChunkHolder;"))
    private static GenerationChunkHolder campaign$dependency(GeneratingChunkMap map,long key) {
        if (map instanceof CampaignChunkMapAccess access) {
            var level=access.campaign$level();
            var pos=ChunkPos.unpack(key);
            if (level.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.CampaignGenerationGate generator && generator.locked(pos.x(),pos.z()))
                return new LockedChunkDependency(level,pos);
        }
        return map.acquireGeneration(key);
    }
}
