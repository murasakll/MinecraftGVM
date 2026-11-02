package br.com.mura.betaminecraftmod.progression;

/** Coordinate-only plan: unlocking changes access, never the identity of existing chunks. */
public final class GenerationRouter {
    public enum Era {
        INFDEV_START, INFDEV_LATE, ALPHA_EARLY, HALLOWEEN, BETA_EARLY, ADVENTURE,
        RELEASE_EARLY, REDSTONE, BOUNTIFUL_COMBAT, EXPLORATION_COLOR, AQUATIC,
        VILLAGE_BEES, NETHER, CAVES_CLIFFS, WILD, TRAILS, TRICKY_BUNDLES,
        PALE_SPRING_SKIES, COPPER_MOUNTS, CURRENT;
        public boolean implemented() { return EraCatalog.get(ordinal()).terrainReady(); }
    }
    private GenerationRouter() { }
    public static Era plannedEra(int chunkX, int chunkZ) {
        return Era.values()[Math.min(Era.values().length - 1, ExplorationBands.chunkBand(chunkX, chunkZ))];
    }
    // Campaign-ready terrain may generate after its gate unlock; historical fidelity is tracked separately.
    public static boolean reserved(int chunkX, int chunkZ) { return !plannedEra(chunkX, chunkZ).implemented(); }
    public static Era availableEra(int chunkX, int chunkZ) {
        Era era = plannedEra(chunkX, chunkZ);
        if (!era.implemented()) throw new IllegalStateException("Reserved chunk has no generator yet: " + era);
        return era;
    }
}
