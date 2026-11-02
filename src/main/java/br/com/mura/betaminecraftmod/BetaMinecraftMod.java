package br.com.mura.betaminecraftmod;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(BetaMinecraftMod.MOD_ID)
public final class BetaMinecraftMod {
    public static final String MOD_ID = "betaminecraftmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final net.neoforged.neoforge.registries.DeferredRegister<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.chunk.ChunkGenerator>> GENERATORS =
        net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.CHUNK_GENERATOR, MOD_ID);
    private static final net.neoforged.neoforge.registries.DeferredRegister<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.biome.BiomeSource>> BIOME_SOURCES =
        net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.BIOME_SOURCE, MOD_ID);
    static {
        BIOME_SOURCES.register("historical_climate", () -> br.com.mura.betaminecraftmod.world.HistoricalBiomeSource.CODEC);
        GENERATORS.register("infdev_20100617", () -> br.com.mura.betaminecraftmod.world.InfdevChunkGenerator.CODEC);
        GENERATORS.register("historical_nether", () -> br.com.mura.betaminecraftmod.world.HistoricalNetherChunkGenerator.CODEC);
        GENERATORS.register("historical_end", () -> br.com.mura.betaminecraftmod.world.HistoricalEndChunkGenerator.CODEC);
    }

    public BetaMinecraftMod(IEventBus modEventBus) {
        GENERATORS.register(modEventBus);
        BIOME_SOURCES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Beta Minecraft Mod carregado.");
    }
}
