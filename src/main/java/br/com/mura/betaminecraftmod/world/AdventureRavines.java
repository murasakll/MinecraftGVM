package br.com.mura.betaminecraftmod.world;

import java.util.Random;
import static br.com.mura.betaminecraftmod.world.InfdevNoise.floor;


final class AdventureRavines {
    private static final float[] SIN = new float[65536];
    static { for (int i = 0; i < SIN.length; i++) SIN[i] = (float) Math.sin(i * Math.PI * 2 / 65536); }
    static float sin(float v) { return SIN[(int) (v * 10430.378F) & 65535]; }
    static float cos(float v) { return SIN[(int) (v * 10430.378F + 16384F) & 65535]; }
    private final long seed;
    private final Random random = new Random();
    private final int height, ceiling;
    AdventureRavines(long seed) { this(seed, 128, 120); }
    AdventureRavines(long seed, int height, int ceiling) {
        this.seed = seed; this.height = height; this.ceiling = ceiling;
    }
    private int index(int x, int y, int z) { return (x * 16 + z) * height + y; }

    void carve(byte[] blocks, int cx, int cz) {
        random.setSeed(seed);
        long a = random.nextLong(), b = random.nextLong();
        for (int sx = cx - 8; sx <= cx + 8; sx++) for (int sz = cz - 8; sz <= cz + 8; sz++) {
            random.setSeed(sx * a ^ sz * b ^ seed);
            if (random.nextInt(50) != 0) continue;
            double x = sx * 16 + random.nextInt(16), y = random.nextInt(random.nextInt(40) + 8) + 20, z = sz * 16 + random.nextInt(16);
            float yaw = random.nextFloat() * (float) Math.PI * 2;
            float pitch = (random.nextFloat() - .5F) * 2 / 8;
            float width = (random.nextFloat() * 2 + random.nextFloat()) * 2;
            tunnel(random.nextLong(), blocks, cx, cz, x, y, z, width, yaw, pitch, 0, 0, 3);

        }
    }

    private void tunnel(long pathSeed, byte[] blocks, int cx, int cz, double x, double y, double z,
                        float width, float yaw, float pitch, int step, int length, double verticalScale) {
        Random path = new Random(pathSeed);
        if (length <= 0) length = 112 - path.nextInt(28);
        boolean room = step == -1;
        if (room) step = length / 2;
        float[] weights = new float[height];
        float profile = 1;
        for (int yLevel = 0; yLevel < height; yLevel++) {
            if (yLevel == 0 || path.nextInt(3) == 0) profile = 1 + path.nextFloat() * path.nextFloat();
            weights[yLevel] = profile * profile;
        }
        float yawDrift = 0, pitchDrift = 0;
        double centerX = cx * 16 + 8, centerZ = cz * 16 + 8;
        for (; step < length; step++) {
            double radius = 1.5 + sin(step * (float) Math.PI / length) * width;
            double ry = radius * verticalScale;
            radius *= path.nextFloat() * .25 + .75;
            ry *= path.nextFloat() * .25 + .75;
            float horizontal = cos(pitch);
            x += cos(yaw) * horizontal; y += sin(pitch); z += sin(yaw) * horizontal;
            pitch *= .7F;
            pitch += pitchDrift * .05F; yaw += yawDrift * .05F;
            pitchDrift *= .8F; yawDrift *= .5F;
            pitchDrift += (path.nextFloat() - path.nextFloat()) * path.nextFloat() * 2;
            yawDrift += (path.nextFloat() - path.nextFloat()) * path.nextFloat() * 4;
            if (!room && path.nextInt(4) == 0) continue;
            double dx = x - centerX, dz = z - centerZ, remaining = length - step, reach = width + 2F + 16F;
            if (dx * dx + dz * dz - remaining * remaining > reach * reach) return;
            if (x < centerX - 16 - radius * 2 || x > centerX + 16 + radius * 2 || z < centerZ - 16 - radius * 2 || z > centerZ + 16 + radius * 2) continue;
            int minX = Math.max(0, floor(x - radius) - cx * 16 - 1), maxX = Math.min(16, floor(x + radius) - cx * 16 + 1);
            int minZ = Math.max(0, floor(z - radius) - cz * 16 - 1), maxZ = Math.min(16, floor(z + radius) - cz * 16 + 1);
            int minY = Math.max(1, floor(y - ry) - 1), maxY = Math.min(ceiling, floor(y + ry) + 1);
            boolean water = false;
            for (int bx = minX; !water && bx < maxX; bx++) for (int bz = minZ; !water && bz < maxZ; bz++) {
                for (int by = maxY + 1; !water && by >= minY - 1; by--) {
                    if (by >= 0 && by < height) {
                        int block = blocks[index(bx, by, bz)];
                        if (block == 8 || block == 9) water = true;
                        if (by != minY - 1 && bx != minX && bx != maxX - 1 && bz != minZ && bz != maxZ - 1) by = minY;
                    }
                }
            }
            if (water) continue;
            for (int bx = minX; bx < maxX; bx++) for (int bz = minZ; bz < maxZ; bz++) {
                double nx = (bx + cx * 16 + .5 - x) / radius, nz = (bz + cz * 16 + .5 - z) / radius;
                if (nx * nx + nz * nz >= 1) continue;
                boolean grass = false;
                int i = index(bx, maxY, bz);
                // Keep the original one-block offset between the shape test and written voxel.
                for (int by = maxY - 1; by >= minY; by--, i--) {
                    double ny = (by + .5 - y) / ry;
                    if ((nx * nx + nz * nz) * weights[by] + ny * ny / 6 >= 1) continue;
                    int block = blocks[i];
                    if (block == 2) grass = true;
                    if (block == 1 || block == 2 || block == 3) {
                        blocks[i] = (byte) (by < 10 ? 10 : 0);
                        if (by >= 10 && grass && blocks[i - 1] == 3) blocks[i - 1] = 2;
                    }
                }
            }
            if (room) return;
        }
    }
}
