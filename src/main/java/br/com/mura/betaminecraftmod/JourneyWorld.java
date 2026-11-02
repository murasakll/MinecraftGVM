package br.com.mura.betaminecraftmod;

import br.com.mura.betaminecraftmod.world.InfdevChunkGenerator;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Every start creates a separate campaign; existing saves are never used as templates. */
final class JourneyWorld {
    static boolean loading;
    static boolean creating;
    private JourneyWorld() { }

    static void openEntry(Screen parent) {
        Minecraft minecraft = Minecraft.getInstance();
        var saveRoot = minecraft.getLevelSource().getLevelPath("unused").getParent();
        boolean hasCampaign;
        try (var paths = java.nio.file.Files.list(saveRoot)) {
            hasCampaign = paths.anyMatch(path -> path.getFileName().toString().startsWith("betaminecraftmod-journey-")
                && java.nio.file.Files.isRegularFile(path.resolve("level.dat")));
        } catch (java.nio.file.NoSuchFileException missing) { hasCampaign = false; }
        catch (java.io.IOException error) {
            BetaMinecraftMod.LOGGER.warn("Could not inspect campaign saves", error);
            hasCampaign = true;
        }
        minecraft.gui.setScreen(hasCampaign ? new CampaignSelectWorldScreen(parent) : new JourneyCreationScreen(parent));
    }

    static void start(Screen parent, net.minecraft.world.Difficulty difficulty, boolean hardcore) {
        Minecraft minecraft = Minecraft.getInstance();
        if (loading) return;
        creating = true;
        loading = true;
        long seed = newSeedWithDryOrigin();
        LevelSettings settings = new LevelSettings("Go back in the days", GameType.SURVIVAL,
            new LevelSettings.DifficultySettings(hardcore ? net.minecraft.world.Difficulty.HARD : difficulty, hardcore, hardcore),
            false, WorldDataConfiguration.DEFAULT);
        String saveId = "betaminecraftmod-journey-" + java.util.UUID.randomUUID();
        br.com.mura.betaminecraftmod.client.EraWorldEntry.prepareNewWorld(() -> minecraft.createWorldOpenFlows().createFreshLevel(saveId, settings,
            new WorldOptions(seed, false, false), registries -> dimensions(registries, seed), parent));
    }

    private static long newSeedWithDryOrigin() {
        for (int attempt = 0; attempt < 256; attempt++) {
            long seed = WorldOptions.randomSeed();
            byte[] chunk = new br.com.mura.betaminecraftmod.world.InfdevTerrain(seed).generate(0, 0);
            for (int y = 125; y > 63; y--) {
                int id = chunk[br.com.mura.betaminecraftmod.world.InfdevTerrain.index(0, y, 0)];
                if (id == 0) continue;
                if (id != 8 && id != 9 && id != 10 && id != 11) return seed;
                break;
            }
        }
        throw new IllegalStateException("Could not select a safe Infdev origin seed");
    }

    private static WorldDimensions dimensions(HolderLookup.Provider registries, long seed) {
        WorldDimensions normal = WorldPresets.createNormalWorldDimensions(registries);
        Identifier id = Identifier.fromNamespaceAndPath(BetaMinecraftMod.MOD_ID, "infdev");
        Holder<DimensionType> infdev = registries.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(BetaMinecraftMod.MOD_ID, "journey")));
        Holder<Biome> biome = registries.lookupOrThrow(Registries.BIOME).getOrThrow(ResourceKey.create(Registries.BIOME, id));
        var dimensions = new HashMap<>(normal.dimensions());
        dimensions.put(LevelStem.OVERWORLD, new LevelStem(infdev, new InfdevChunkGenerator(biome, seed, registries.lookupOrThrow(Registries.BIOME), br.com.mura.betaminecraftmod.world.ModernClimateContext.from(registries),"1.0",true)));
        var netherBiome=registries.lookupOrThrow(Registries.BIOME).getOrThrow(ResourceKey.create(Registries.BIOME,Identifier.withDefaultNamespace("nether_wastes")));
        dimensions.put(LevelStem.NETHER,new LevelStem(dimensions.get(LevelStem.NETHER).type(),new br.com.mura.betaminecraftmod.world.HistoricalNetherChunkGenerator(netherBiome,seed,8,true)));
        var endBiome=registries.lookupOrThrow(Registries.BIOME).getOrThrow(ResourceKey.create(Registries.BIOME,Identifier.withDefaultNamespace("the_end")));
        dimensions.put(LevelStem.END,new LevelStem(dimensions.get(LevelStem.END).type(),new br.com.mura.betaminecraftmod.world.HistoricalEndChunkGenerator(endBiome,seed)));
        return new WorldDimensions(dimensions);
    }
}
