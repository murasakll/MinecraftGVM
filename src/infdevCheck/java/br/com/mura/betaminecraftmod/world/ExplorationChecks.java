package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.ExplorationBands;
import br.com.mura.betaminecraftmod.progression.GenerationRouter;

public final class ExplorationChecks {
    public static void run() {
        if (ExplorationBands.diameter(0) != 1000 || ExplorationBands.diameter(1) != 2024) throw new AssertionError("Wrong ring width");
        if(ExplorationBands.terrainDiameter(0)!=1024 || ExplorationBands.terrainDiameter(1)!=2048) throw new AssertionError("Terrain identity changed with playable border");
        for (int x = -250; x <= 250; x++) for (int z = -250; z <= 250; z++) {
            int ring = ExplorationBands.chunkBand(x, z);
            if (GenerationRouter.reserved(x, z) != (ring > 6)) throw new AssertionError("Unimplemented campaign bands must be reserved");
            if (ring != ExplorationBands.chunkBand(-x - 1, -z - 1)) throw new AssertionError("Asymmetric bands");
            if (ring != ExplorationBands.band(x * 16 + .5, z * 16 + .5, 0, 0)
                    || ring != ExplorationBands.band(x * 16 + 15.5, z * 16 + 15.5, 0, 0)) throw new AssertionError("Band crosses a chunk");
        }
        if (ExplorationBands.chunkBand(31, -32) != 0 || ExplorationBands.chunkBand(32, 0) != 1
                || ExplorationBands.chunkBand(-33, 0) != 1) throw new AssertionError("Incorrect boundary");
        if (GenerationRouter.plannedEra(0, 0) != GenerationRouter.Era.INFDEV_START
                || GenerationRouter.plannedEra(32, 0) != GenerationRouter.Era.INFDEV_LATE
                || GenerationRouter.plannedEra(100000, 0) != GenerationRouter.Era.CURRENT) throw new AssertionError("Incorrect route");
        if (GenerationRouter.availableEra(32, 0) != GenerationRouter.Era.INFDEV_LATE) throw new AssertionError("Late terrain route unavailable");
        if (GenerationRouter.availableEra(64, 0) != GenerationRouter.Era.ALPHA_EARLY) throw new AssertionError("Alpha terrain route unavailable");
        if(GenerationRouter.availableEra(96,0)!=GenerationRouter.Era.HALLOWEEN||GenerationRouter.availableEra(160,0)!=GenerationRouter.Era.ADVENTURE)throw new AssertionError("Playable campaign bases unavailable");
        try { GenerationRouter.availableEra(224, 0); throw new AssertionError("Reserved chunks must not fall back to Infdev"); }
        catch (IllegalStateException expected) { }
        System.out.println("Exploration bands: alignment, symmetry, boundary and route checks passed.");
    }
}
