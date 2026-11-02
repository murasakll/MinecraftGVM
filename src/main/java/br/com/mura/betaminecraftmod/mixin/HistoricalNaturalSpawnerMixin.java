package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.InfdevContent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public abstract class HistoricalNaturalSpawnerMixin {
    @Inject(method = "spawnForChunk", at = @At("HEAD"), cancellable = true)
    private static void campaign$historical(ServerLevel level, net.minecraft.world.level.chunk.LevelChunk chunk,
            NaturalSpawner.SpawnState state, java.util.List<net.minecraft.world.entity.MobCategory> categories, CallbackInfo ci) {
        if (InfdevContent.legacy(level)) ci.cancel();
    }
}
