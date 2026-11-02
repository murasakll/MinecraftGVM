package br.com.mura.betaminecraftmod.progression;

/** Square rings measured from the campaign's fixed center, in blocks. */
public final class ExplorationBands {
    public static final int INITIAL_DIAMETER = 1000;
    /** Symmetric whole-chunk allocation; does not change the exact playable border. */
    public static final int TERRAIN_DIAMETER = ((INITIAL_DIAMETER + 31) / 32) * 32;
    public static final int RING_WIDTH = 512;
    public static final int MAX_RINGS = 19;
    private ExplorationBands() { }
    public static int diameter(int unlockedRings) {
        return INITIAL_DIAMETER + 2 * RING_WIDTH * Math.clamp(unlockedRings, 0, MAX_RINGS);
    }
    public static int terrainDiameter(int unlockedRings) {
        return TERRAIN_DIAMETER + 2 * RING_WIDTH * Math.clamp(unlockedRings,0,MAX_RINGS);
    }
    /** Terrain identity, distinct from the exact playable border inside the outer edge chunks. */
    public static int band(double x, double z, double centerX, double centerZ) {
        double distance = Math.max(Math.abs(x - centerX), Math.abs(z - centerZ));
        return Math.max(0, (int) Math.floor((distance - TERRAIN_DIAMETER / 2.0) / RING_WIDTH) + 1);
    }
    /** Whole chunks belong to exactly one ring. The origin lies between chunks -1 and 0. */
    public static int chunkBand(int chunkX, int chunkZ) {
        long x = chunkX >= 0 ? chunkX : -(long) chunkX - 1;
        long z = chunkZ >= 0 ? chunkZ : -(long) chunkZ - 1;
        return (int) (Math.max(x, z) / (RING_WIDTH / 16));
    }
}
