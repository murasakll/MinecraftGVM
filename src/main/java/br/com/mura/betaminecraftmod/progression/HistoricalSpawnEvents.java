package br.com.mura.betaminecraftmod.progression;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid="betaminecraftmod")
public final class HistoricalSpawnEvents {
    @SubscribeEvent public static void candidates(LevelEvent.PotentialSpawns event) {
        if(!(event.getLevel() instanceof ServerLevel level))return;
        int era=CampaignRules.era(level);if(era<3||era>6)return;
        String dimension=level.dimension().identifier().toString();
        String biome=level.getBiome(event.getPos()).unwrapKey().orElseThrow().identifier().getPath();
        var existing=List.copyOf(event.getSpawnerDataList());
        for(var entry:existing)event.removeSpawnerData(entry);
        if(era==6&&dimension.equals("minecraft:the_nether")&&event.getMobCategory()==net.minecraft.world.entity.MobCategory.MONSTER&&level.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.HistoricalNetherChunkGenerator generator&&generator.fortresses().contains(event.getPos())) {
            for(var entry:List.of(new HistoricalSpawnTables.Entry("blaze",10,2,3),new HistoricalSpawnTables.Entry("zombified_piglin",10,4,4),new HistoricalSpawnTables.Entry("magma_cube",3,4,4))) {
                var type=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace(entry.entity()));
                event.addSpawnerData(new Weighted<>(new MobSpawnSettings.SpawnerData(type,UniformInt.of(entry.min(),entry.max())),entry.weight()));
            }
            return;
        }
        for(var entry:HistoricalSpawnTables.pool(era,dimension,biome,event.getMobCategory().getName())) {
            var type=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace(entry.entity()));
            event.addSpawnerData(new Weighted<>(new MobSpawnSettings.SpawnerData(type,UniformInt.of(entry.min(),entry.max())),entry.weight()));
        }
        // Structure-specific pools have a separate audit; preserve only era-available native entries.
        if(era==6&&dimension.equals("minecraft:the_nether"))for(var entry:existing)
            if(BuiltInRegistries.ENTITY_TYPE.getKey(entry.value().type()).getPath().equals("blaze"))event.addSpawnerData(entry);
    }
    private HistoricalSpawnEvents() { }
}
