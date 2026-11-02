package br.com.mura.betaminecraftmod.world;

import java.util.Random;
import static br.com.mura.betaminecraftmod.world.InfdevNoise.lerp;

/** Independently verified June 17 / June 30 Infdev terrain profiles, without Minecraft APIs. */
public final class InfdevTerrain {
    private final long seed;
    final boolean late, alpha;
    private final InfdevNoise low, high, blend, beaches, thickness, scale, depth;
    final InfdevNoise trees;

    public InfdevTerrain(long seed) {
        this(seed, false);
    }
    public InfdevTerrain(long seed, boolean late) {
        this(seed, late ? 1 : 0);
    }
    public InfdevTerrain(long seed, int profile) {
        this.seed = seed;
        this.late = profile >= 1;
        this.alpha = profile >= 2;
        Random random = new Random(seed);
        low = new InfdevNoise(random, 16);
        high = new InfdevNoise(random, 16);
        blend = new InfdevNoise(random, 8);
        beaches = new InfdevNoise(random, 4);
        thickness = new InfdevNoise(random, 4);
        scale = new InfdevNoise(random, 10);
        depth = new InfdevNoise(random, 16);
        trees = new InfdevNoise(random, 8);
    }

    public byte[] generate(int cx, int cz) {
        byte[] blocks = new byte[32768];
        double[] density = density(cx * 4, cz * 4);
        for (int gx = 0; gx < 4; gx++) for (int gz = 0; gz < 4; gz++) for (int gy = 0; gy < 16; gy++) {
            int p = (gx * 5 + gz) * 17 + gy;
            for (int y = 0; y < 8; y++) {
                double ty = y / 8.0;
                double a = lerp(ty, density[p], density[p + 1]);
                double b = lerp(ty, density[p + 17], density[p + 18]);
                double c = lerp(ty, density[p + 85], density[p + 86]);
                double d = lerp(ty, density[p + 102], density[p + 103]);
                for (int x = 0; x < 4; x++) {
                    double tx = x / 4.0;
                    double e = lerp(tx, a, c), f = lerp(tx, b, d);
                    int by = gy * 8 + y;
                    for (int z = 0; z < 4; z++) blocks[index(gx * 4 + x, by, gz * 4 + z)] =
                        (byte) (lerp(z / 4.0, e, f) > 0 ? 1 : by < 64 ? 9 : 0);
                }
            }
        }
        surface(blocks, cx, cz);
        new InfdevCaves(seed, late).carve(blocks, cx, cz);
        return blocks;
    }

    private double[] density(int x, int z) {
        double[] s = scale.grid(x, z, 5, 1, 5, 1, 0, 1);
        double[] d = depth.grid(x, z, 5, 1, 5, 100, 0, 100);
        double[] mix = blend.grid(x, z, 5, 17, 5, 8.555150000000001, 4.277575000000001, 8.555150000000001);
        double[] a = low.grid(x, z, 5, 17, 5, 684.412, 684.412, 684.412);
        double[] b = high.grid(x, z, 5, 17, 5, 684.412, 684.412, 684.412);
        double[] out = new double[425];
        for (int column = 0; column < 25; column++) {
            double stretch = Math.min(1, (s[column] + 256) / 512);
            double offset = Math.abs(d[column] / 8000) * 3 - 3;
            if (offset < 0) { offset = Math.max(-1, offset / 2) / 1.4 / 2; stretch = 0; }
            else offset = Math.min(1, offset) / 6;
            stretch += .5;
            double center = 8.5 + offset * 17 / 16 * 4;
            for (int y = 0; y < 17; y++) {
                int i = column * 17 + y;
                double gradient = (y - center) * 12 / stretch;
                if (gradient < 0) gradient *= 4;
                double alpha = (mix[i] / 10 + 1) / 2;
                out[i] = (alpha < 0 ? a[i] / 512 : alpha > 1 ? b[i] / 512 : lerp(alpha, a[i] / 512, b[i] / 512)) - gradient;
                if (late && y > 13) { double fade = (y - 13) / 3.0F; out[i] = out[i] * (1 - fade) - 10 * fade; }
            }
        }
        return out;
    }

    private void surface(byte[] blocks, int cx, int cz) {
        Random random = new Random(cx * 341873128712L + cz * 132897987541L);
        double[] sandGrid = late ? beaches.grid3d(cx * 16, cz * 16, 0, 16, 16, 1, .03125, .03125, 1) : null;
        double[] gravelGrid = late ? beaches.grid3d(cz * 16, 109.0134, cx * 16, 16, 1, 16, .03125, 1, .03125) : null;
        double[] layerGrid = late ? thickness.grid3d(cx * 16, cz * 16, 0, 16, 16, 1, .0625, .0625, .0625) : null;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            double wx = cx * 16 + x, wz = cz * 16 + z;
            boolean sand = (late ? sandGrid[x + z * 16] : beaches.sample(wx * .03125, wz * .03125, 0)) + random.nextDouble() * .2 > 0;
            boolean gravel = (late ? gravelGrid[x + z * 16] : beaches.sample(wz * .03125, 109.0134, wx * .03125)) + random.nextDouble() * .2 > 3;
            int layers = (int) ((late ? layerGrid[x + z * 16] : thickness.sample(wx * .0625, wz * .0625, 0)) / 3 + 3 + random.nextDouble() * .25);
            int remaining = -1, top = 2, filler = 3;
            for (int y = 127; y >= 0; y--) {
                int i = index(x, y, z);
                if (y <= (late ? random.nextInt(6) - 1 : random.nextInt(5))) blocks[i] = 7;
                else if (blocks[i] == 0) remaining = -1;
                else if (blocks[i] == 1) {
                    if (remaining == -1) {
                        if (layers <= 0) { top = 0; filler = 1; }
                        else if (y >= 60 && y <= 65) {
                            top = 2; filler = 3;
                            if (gravel) { top = 0; filler = 13; }
                            if (sand) { top = 12; filler = 12; }
                        }
                        if (y < 64 && top == 0) top = 9;
                        remaining = layers;
                        blocks[i] = (byte) (y >= 63 ? top : filler);
                    } else if (remaining > 0) { remaining--; blocks[i] = (byte) filler; }
                }
            }
        }
    }

    public static int index(int x, int y, int z) { return (x << 11) | (z << 7) | y; }
}
