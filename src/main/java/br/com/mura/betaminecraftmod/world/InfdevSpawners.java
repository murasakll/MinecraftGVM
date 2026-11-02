package br.com.mura.betaminecraftmod.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;

/** Worldgen must configure data without requesting FULL chunks or sending block updates. */
public final class InfdevSpawners {
    private InfdevSpawners() { }
    public static void configure(BaseSpawner spawner, EntityType<?> entity, RandomSource random, BlockPos pos) {
        // Passing ServerLevel here invokes SpawnerBlockEntity's update callback, which asks
        // for the same chunk while its FEATURES task is still running: a cyclic wait.
        spawner.setEntityId(entity, null, random, pos);
    }
}
