package br.com.mura.betaminecraftmod.world;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Era-specific decoration; no modern biome features, structures or ores are used. */
final class InfdevPopulation {
    private static final ThreadLocal<Integer> BAND = new ThreadLocal<>();
    static void populate(WorldGenLevel level, int cx, int cz, InfdevTerrain terrain) {
        inBand(cx,cz,()->populateBand(level,cx,cz,terrain));
    }
    static void inBand(int cx,int cz,Runnable action) {
        Integer previous=BAND.get();
        BAND.set(br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(cx,cz));
        try {action.run();}finally {if(previous==null)BAND.remove();else BAND.set(previous);}
    }
    private static void populateBand(WorldGenLevel level, int cx, int cz, InfdevTerrain terrain) {
        Random random = new Random(cx * 318279123L + cz * 919871212L);
        if (terrain.late) {
            random.setSeed(level.getSeed());
            long a = random.nextLong() / 2 * 2 + 1, b = random.nextLong() / 2 * 2 + 1;
            random.setSeed(cx * a + cz * b ^ level.getSeed());
        }
        int x = cx * 16, z = cz * 16;
        if (terrain.late) for (int i = 0; i < (terrain.alpha ? 8 : 4); i++) dungeon(level, random, x + random.nextInt(16) + 8, random.nextInt(128), z + random.nextInt(16) + 8, terrain.alpha);
        if (terrain.alpha) ores(level, random, x, z, Blocks.CLAY, 10, 32, 128);
        ores(level, random, x, z, Blocks.DIRT, 20, 32, 128);
        ores(level, random, x, z, Blocks.GRAVEL, 10, 32, 128);
        ores(level, random, x, z, Blocks.COAL_ORE, 20, 16, 128);
        ores(level, random, x, z, Blocks.IRON_ORE, 20, 8, 64);
        if (!terrain.alpha) random.nextInt(1); // The historical guaranteed gold attempt still advances the RNG.
        ores(level, random, x, z, Blocks.GOLD_ORE, terrain.alpha ? 2 : 1, 8, 32);
        if (terrain.alpha) ores(level, random, x, z, Blocks.REDSTONE_ORE, 8, 7, 16);
        if (terrain.alpha || random.nextInt(4) == 0) ores(level, random, x, z, Blocks.DIAMOND_ORE, 1, terrain.alpha ? 7 : 8, 16);
        int count = Math.max(0, (int) ((terrain.trees.sample(x * .5, z * .5, 0) / 8 + random.nextDouble() * 4 + 4) / (terrain.late ? 3 : 1)));
        if (random.nextInt(10) == 0) count++;
        for (int i = 0; i < count; i++) {
            int tx = x + random.nextInt(16) + 8, tz = z + random.nextInt(16) + 8;
            tree(level, random, tx, level.getHeight(Heightmap.Types.MOTION_BLOCKING, tx, tz), tz);
        }
        for (int i = 0; i < 2; i++) flowers(level, random, x, z, Blocks.DANDELION);
        if (random.nextInt(2) == 0) flowers(level, random, x, z, Blocks.POPPY);
        if (random.nextInt(4) == 0) flowers(level, random, x, z, Blocks.BROWN_MUSHROOM);
        if (random.nextInt(8) == 0) flowers(level, random, x, z, Blocks.RED_MUSHROOM);
        if (terrain.alpha) {
            for (int i = 0; i < 10; i++) reeds(level, random, x + random.nextInt(16) + 8, random.nextInt(128), z + random.nextInt(16) + 8);
            for (int i = 0; i < 10; i++) {
                BlockPos pos = new BlockPos(x + random.nextInt(16) + 8, random.nextInt(128), z + random.nextInt(16) + 8);
                if (level.isEmptyBlock(pos) && Blocks.CACTUS.defaultBlockState().canSurvive(level,pos))
                    setBlock(level,pos,Blocks.CACTUS.defaultBlockState(),2);
            }
        }
        for (int i = 0; i < 50; i++) spring(level, random, x + random.nextInt(16) + 8,
            random.nextInt(random.nextInt(120) + 8), z + random.nextInt(16) + 8, Blocks.WATER);
        for (int i = 0; i < 20; i++) spring(level, random, x + random.nextInt(16) + 8,
            random.nextInt(random.nextInt(random.nextInt(112) + 8) + 8), z + random.nextInt(16) + 8, Blocks.LAVA);
    }

    static void reeds(WorldGenLevel level, Random r, int x, int y, int z) {
        for (int i=0;i<20;i++) {
            int bx=x+r.nextInt(4)-r.nextInt(4), bz=z+r.nextInt(4)-r.nextInt(4);
            int height=2+r.nextInt(r.nextInt(3)+1);
            for(int dy=0;dy<height;dy++) {
                BlockPos pos=new BlockPos(bx,y+dy,bz);
                if (y+dy < 128 && level.isEmptyBlock(pos) && Blocks.SUGAR_CANE.defaultBlockState().canSurvive(level,pos))
                    setBlock(level,pos,Blocks.SUGAR_CANE.defaultBlockState(),2);
            }
        }
    }
    static void dungeon(WorldGenLevel level, Random r, int x, int y, int z, boolean alpha) {
        int rx = r.nextInt(2) + 2, rz = r.nextInt(2) + 2, openings = 0;
        if (y < 1 || y + 4 >= 128) return;
        // A room must fit its own historical band; decoration never touches the older terrain.
        for (int bx = x-rx-1; bx <= x+rx+1; bx++) for (int bz = z-rz-1; bz <= z+rz+1; bz++) {
            if (br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(Math.floorDiv(bx,16), Math.floorDiv(bz,16)) != BAND.get()) return;
            if (!level.getBlockState(new BlockPos(bx,y-1,bz)).isSolidRender() || !level.getBlockState(new BlockPos(bx,y+4,bz)).isSolidRender()) return;
            if ((bx == x-rx-1 || bx == x+rx+1 || bz == z-rz-1 || bz == z+rz+1)
                    && level.isEmptyBlock(new BlockPos(bx,y,bz)) && level.isEmptyBlock(new BlockPos(bx,y+1,bz))) openings++;
        }
        if (openings < 1 || openings > 5) return;
        for (int bx = x-rx-1; bx <= x+rx+1; bx++) for (int by=y+3; by>=y-1; by--) for (int bz=z-rz-1; bz<=z+rz+1; bz++) {
            BlockPos pos = new BlockPos(bx,by,bz);
            boolean edge = bx == x-rx-1 || bx == x+rx+1 || bz == z-rz-1 || bz == z+rz+1 || by == y-1;
            if (!edge || !level.getBlockState(pos.below()).isSolidRender()) setBlock(level,pos,Blocks.AIR.defaultBlockState(),2);
            else if (level.getBlockState(pos).isSolidRender()) setBlock(level,pos,(by == y-1 && r.nextInt(4) != 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE).defaultBlockState(),2);
        }
        for (int chest=0; chest<2; chest++) for (int attempt=0; attempt<3; attempt++) {
            BlockPos pos = new BlockPos(x+r.nextInt(rx*2+1)-rx,y,z+r.nextInt(rz*2+1)-rz);
            if (!level.isEmptyBlock(pos)) continue;
            int walls=0;
            for (var dir : new net.minecraft.core.Direction[]{net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.WEST})
                if (level.getBlockState(pos.relative(dir)).isSolidRender()) walls++;
            if (walls != 1) continue;
            setBlock(level,pos,Blocks.CHEST.defaultBlockState(),2);
            if (level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity container)
                for (int roll=0;roll<8;roll++) {
                    int choice=r.nextInt(alpha ? 11 : 10);
                    var item = switch(choice) {
                        case 0 -> net.minecraft.world.item.Items.SADDLE; case 1 -> net.minecraft.world.item.Items.IRON_INGOT;
                        case 2 -> net.minecraft.world.item.Items.BREAD; case 3 -> net.minecraft.world.item.Items.WHEAT;
                        case 4 -> net.minecraft.world.item.Items.GUNPOWDER; case 5 -> net.minecraft.world.item.Items.STRING;
                        case 6 -> net.minecraft.world.item.Items.BUCKET; case 7 -> r.nextInt(100)==0 ? net.minecraft.world.item.Items.GOLDEN_APPLE : null;
                        case 8 -> alpha && r.nextInt(2)==0 ? net.minecraft.world.item.Items.REDSTONE : null;
                        case 9 -> alpha && r.nextInt(10)==0 ? (r.nextBoolean() ? net.minecraft.world.item.Items.MUSIC_DISC_13 : net.minecraft.world.item.Items.MUSIC_DISC_CAT) : null;
                        default -> null;
                    };
                    if (item != null) container.setItem(r.nextInt(container.getContainerSize()), new net.minecraft.world.item.ItemStack(item, choice==1 || choice>=3 && choice<=5 ? r.nextInt(4)+1 : 1));
                }
            break;
        }
        BlockPos pos = new BlockPos(x,y,z);
        setBlock(level,pos,Blocks.SPAWNER.defaultBlockState(),2);
        int mob = r.nextInt(4);
        if (level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity spawner)
            InfdevSpawners.configure(spawner.getSpawner(), mob==0 ? net.minecraft.world.entity.EntityTypes.SKELETON : mob==3 ? net.minecraft.world.entity.EntityTypes.SPIDER : net.minecraft.world.entity.EntityTypes.ZOMBIE,
                net.minecraft.util.RandomSource.create(r.nextLong()), pos);
    }
    static void ores(WorldGenLevel level, Random r, int x, int z, Block block, int count, int size, int height) {
        for (int i = 0; i < count; i++) {
            int bx=x+r.nextInt(16), by=r.nextInt(height), bz=z+r.nextInt(16);
            if (block == Blocks.CLAY && !level.getBlockState(new BlockPos(bx,by,bz)).is(Blocks.WATER)) continue;
            vein(level,r,bx,by,bz,block,size);
        }
    }
    static void vein(WorldGenLevel level, Random r, int x, int y, int z, Block block, int size) {
        float angle = r.nextFloat() * (float) Math.PI;
        double x0 = x + 8 + InfdevCaves.sin(angle) * size / 8F, x1 = x + 8 - InfdevCaves.sin(angle) * size / 8F;
        double z0 = z + 8 + InfdevCaves.cos(angle) * size / 8F, z1 = z + 8 - InfdevCaves.cos(angle) * size / 8F;
        double y0 = y + r.nextInt(3) + 2, y1 = y + r.nextInt(3) + 2;
        for (int step = 0; step <= size; step++) {
            double px = x0 + (x1 - x0) * step / size, py = y0 + (y1 - y0) * step / size, pz = z0 + (z1 - z0) * step / size;
            double jitter = r.nextDouble() * size / 16;
            double radius = ((InfdevCaves.sin(step * (float) Math.PI / size) + 1F) * jitter + 1) / 2;
            for (int bx = (int) (px - radius); bx <= (int) (px + radius); bx++)
                for (int by = (int) (py - radius); by <= (int) (py + radius); by++)
                    for (int bz = (int) (pz - radius); bz <= (int) (pz + radius); bz++) {
                        double dx = (bx + .5 - px) / radius, dy = (by + .5 - py) / radius, dz = (bz + .5 - pz) / radius;
                        BlockPos pos = new BlockPos(bx, by, bz);
                        if (by >= 0 && by < 128 && dx * dx + dy * dy + dz * dz < 1 && level.getBlockState(pos).is(block == Blocks.CLAY ? Blocks.SAND : Blocks.STONE))
                            setBlock(level, pos, block.defaultBlockState(), 2);
                    }
        }
    }

    static void tree(WorldGenLevel level, Random r, int x, int y, int z) {
        int height = r.nextInt(3) + 4;
        if (y <= 0 || y + height + 1 > 128) return;
        for (int by = y; by <= y + height + 1; by++) {
            int radius = by == y ? 0 : by >= y + height - 1 ? 2 : 1;
            for (int bx = x - radius; bx <= x + radius; bx++) for (int bz = z - radius; bz <= z + radius; bz++) {
                BlockState state = level.getBlockState(new BlockPos(bx, by, bz));
                if (!state.isAir() && !state.is(Blocks.OAK_LEAVES)) return;
            }
        }
        BlockPos ground = new BlockPos(x, y - 1, z);
        if (!level.getBlockState(ground).is(Blocks.GRASS_BLOCK) && !level.getBlockState(ground).is(Blocks.DIRT)) return;
        setBlock(level, ground, Blocks.DIRT.defaultBlockState(), 2);
        for (int by = y - 3 + height; by <= y + height; by++) {
            int dy = by - (y + height), radius = 1 - dy / 2;
            for (int bx = x - radius; bx <= x + radius; bx++) for (int bz = z - radius; bz <= z + radius; bz++) {
                if (Math.abs(bx - x) == radius && Math.abs(bz - z) == radius && !(r.nextInt(2) != 0 && dy != 0)) continue;
                BlockPos pos = new BlockPos(bx, by, bz);
                if (!level.getBlockState(pos).isSolidRender()) setBlock(level, pos, Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1), 2);
            }
        }
        for (int dy = 0; dy < height; dy++) {
            BlockPos pos = new BlockPos(x, y + dy, z);
            if (level.getBlockState(pos).isAir() || level.getBlockState(pos).is(Blocks.OAK_LEAVES)) setBlock(level, pos, Blocks.OAK_LOG.defaultBlockState(), 2);
        }
    }

    static void flowers(WorldGenLevel level, Random r, int x, int z, Block block) {
        int bx = x + r.nextInt(16) + 8, by = r.nextInt(128), bz = z + r.nextInt(16) + 8;
        for (int i = 0; i < 64; i++) {
            BlockPos pos = new BlockPos(bx + r.nextInt(8) - r.nextInt(8), by + r.nextInt(4) - r.nextInt(4), bz + r.nextInt(8) - r.nextInt(8));
            if (pos.getY() <= 0 || pos.getY() >= 128) continue;
            BlockState state = block.defaultBlockState();
            if (level.isEmptyBlock(pos) && state.canSurvive(level, pos)) setBlock(level, pos, state, 2);
        }
    }
    static void spring(WorldGenLevel level, Random r, int x, int y, int z, Block fluid) {
        if (y <= 0 || y >= 127) return;
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.getBlockState(pos.above()).is(Blocks.STONE) || !level.getBlockState(pos.below()).is(Blocks.STONE)) return;
        if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).is(Blocks.STONE)) return;
        int stone = 0, air = 0;
        for (BlockPos side : new BlockPos[]{pos.west(), pos.east(), pos.north(), pos.south()}) {
            if (level.getBlockState(side).is(Blocks.STONE)) stone++;
            if (level.isEmptyBlock(side)) air++;
        }
        if (stone == 3 && air == 1) {
            if(setBlock(level, pos, fluid.defaultBlockState(), 2))
                level.scheduleTick(pos, fluid == Blocks.WATER ? net.minecraft.world.level.material.Fluids.WATER : net.minecraft.world.level.material.Fluids.LAVA, 1);
        }
    }
    static boolean setBlock(WorldGenLevel level, BlockPos pos, BlockState state, int flags) {
        if(br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(Math.floorDiv(pos.getX(),16),Math.floorDiv(pos.getZ(),16))==6
                &&level.getLevel().getChunkSource().getGenerator() instanceof InfdevChunkGenerator generator&&generator.protectsReleasePortal(pos))return false;
        // The generator authorizes the source chunk before dispatch; this scope limits all writes
        // to that same band. Readiness belongs to dispatch, not to a reusable/detached decorator.
        if (BAND.get() != null && BAND.get() == br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(Math.floorDiv(pos.getX(), 16), Math.floorDiv(pos.getZ(), 16)))
            return level.setBlock(pos, state, flags);
        return false;
    }}
