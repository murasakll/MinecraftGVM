package br.com.mura.betaminecraftmod.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/** Serialized generator, so loading a save never falls back to modern terrain. */
public final class InfdevChunkGenerator extends ChunkGenerator implements CampaignGenerationGate {
    public static final MapCodec<InfdevChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Biome.CODEC.fieldOf("biome").forGetter(g -> g.biome),
        Codec.LONG.fieldOf("seed").forGetter(g -> g.seed),
        net.minecraft.resources.RegistryOps.retrieveGetter(net.minecraft.core.registries.Registries.BIOME),
        ModernClimateContext.CODEC.forGetter(g->g.modernClimate),
        Codec.STRING.optionalFieldOf("first_release","1.2.5").forGetter(g->g.firstRelease),
        Codec.BOOL.optionalFieldOf("release_landmarks",false).forGetter(g->g.releaseLandmarks)
    ).apply(i, InfdevChunkGenerator::new));
    private final Holder<Biome> biome;
    private final long seed;
    private final String firstRelease;
    private boolean releaseLandmarks;
    private final ReleasePortalPlan releasePortalPlan;
    public String firstRelease(){return firstRelease;}
    public boolean hasReleasePortal(){return releaseLandmarks&&firstRelease.equals("1.0");}
    public ReleasePortalPlan releasePortalPlan(){return releasePortalPlan;}
    public boolean protectsReleasePortal(BlockPos pos){return hasReleasePortal()&&releasePortalPlan.protectedCell(pos.getX(),pos.getY(),pos.getZ());}
    private ModernClimateContext modernClimate;
    private volatile int unlockedEra;
    public void unlockThrough(int era) { unlockedEra = era; }
    public boolean locked(int x, int z) {
        return br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(x,z) > unlockedEra
            || br.com.mura.betaminecraftmod.progression.GenerationRouter.reserved(x,z);
    }
    private final TerrainBackends backends;
    private final GenerationBases generationBases;
    private HalloweenPopulation halloweenPopulation;
    private HalloweenPopulation betaPopulation;
    private final java.util.Map<Integer,AdventurePopulation> adventurePopulations=new java.util.HashMap<>();
    private synchronized AdventurePopulation adventurePopulation(int era){return adventurePopulations.computeIfAbsent(era,id->new AdventurePopulation(seed,id,firstRelease));}
    private synchronized HalloweenPopulation betaPopulation(){
        if(betaPopulation==null)betaPopulation=new HalloweenPopulation(seed,4);
        return betaPopulation;
    }
    private synchronized HalloweenPopulation halloweenPopulation(){
        if(halloweenPopulation==null)halloweenPopulation=new HalloweenPopulation(seed);
        return halloweenPopulation;
    }
    private net.minecraft.server.level.ServerLevel attachedLevel;
    private ModernTerrainBase modernBase;
    public void attachLevel(net.minecraft.server.level.ServerLevel level){this.attachedLevel=level;}
    private synchronized ModernTerrainBase modernBase(){
        if(attachedLevel==null)throw new IllegalStateException("Modern generation requested before level attachment");
        if(modernBase==null)modernBase=new ModernTerrainBase(attachedLevel,seed);
        return modernBase;
    }
    private final java.util.Map<Long,TerrainVolume> volumeSamples=java.util.Collections.synchronizedMap(new java.util.LinkedHashMap<>(16,.75F,true){
        @Override protected boolean removeEldestEntry(java.util.Map.Entry<Long,TerrainVolume> entry){return size()>16;}
    });
    private final java.util.Map<Long, byte[]> samples = java.util.Collections.synchronizedMap(new java.util.LinkedHashMap<>(64, .75F, true) {
        @Override protected boolean removeEldestEntry(java.util.Map.Entry<Long, byte[]> entry) { return size() > 64; }
    });

    public InfdevChunkGenerator(Holder<Biome> biome,long seed,net.minecraft.core.HolderGetter<Biome> registry,ModernClimateContext context){
        this(biome,seed,registry,context,"1.2.5");
    }
    public InfdevChunkGenerator(Holder<Biome> biome,long seed,net.minecraft.core.HolderGetter<Biome> registry,ModernClimateContext context,String firstRelease){
        this(biome,seed,registry,context,firstRelease,false);
    }
    public InfdevChunkGenerator(Holder<Biome> biome,long seed,net.minecraft.core.HolderGetter<Biome> registry,ModernClimateContext context,String firstRelease,boolean releaseLandmarks){
        this(biome,seed,new HistoricalBiomeSource(biome,seed,registry,context,firstRelease),firstRelease);
        this.modernClimate=context;
        this.releaseLandmarks=releaseLandmarks;
    }
    public InfdevChunkGenerator(Holder<Biome> biome,long seed){this(biome,seed,new FixedBiomeSource(biome),"1.2.5");}
    private InfdevChunkGenerator(Holder<Biome> biome,long seed,BiomeSource source,String firstRelease){
        super(source, ignored -> BiomeGenerationSettings.EMPTY);
        this.biome = biome;
        this.seed = seed;
        this.firstRelease=firstRelease;
        releasePortalPlan=ReleasePortalPlan.forSeed(seed);
        backends = new TerrainBackends(seed);
        generationBases = new GenerationBases(seed,firstRelease);
    }

    @Override protected MapCodec<? extends ChunkGenerator> codec() { return CODEC; }
    @Override public int getMinY() { return 0; }
    @Override public int getGenDepth() { return 128; }
    @Override public int getSeaLevel() { return 64; }
    @Override public int getSpawnHeight(LevelHeightAccessor accessor) { return 65; }
    @Override public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structures, RandomState random, long seed) {
        return ChunkGeneratorStructureState.createForFlat(random, seed, getOrigin(random), biomeSource, java.util.stream.Stream.empty());
    }

    @Override public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState random,
            StructureManager structures, BiomeManager biomes, WorldGenRegion region, Set<Holder<Biome>> possibleBiomes) {
        if (locked(chunk.getPos().x(), chunk.getPos().z()))
            return CompletableFuture.completedFuture(chunk);
        int era=br.com.mura.betaminecraftmod.progression.GenerationRouter.plannedEra(chunk.getPos().x(),chunk.getPos().z()).ordinal();
        byte[] old=era<=5?sample(chunk.getPos().x(),chunk.getPos().z()):null;
        TerrainVolume blocks=old==null?volume(chunk.getPos().x(),chunk.getPos().z()):null;
        int minY=old!=null?0:blocks.minY(),height=old!=null?128:blocks.height();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Heightmap ocean = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = Math.max(minY,chunk.getMinY()); y < Math.min(minY+height,chunk.getMaxY()+1); y++) {
            BlockState state = old!=null?block(Byte.toUnsignedInt(old[InfdevTerrain.index(x,y,z)])):volumeBlock(blocks.block(x,y,z));
            if (state.isAir()) continue;
            chunk.setBlockState(pos.set(x, y, z), state);
            ocean.update(x, y, z, state);
            surface.update(x, y, z, state);
        }
        if(hasReleasePortal()&&era==6)ReleasePortalPlacement.build(chunk,releasePortalPlan);
        return CompletableFuture.completedFuture(chunk);
    }

    @Override public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structures) {
        if (locked(chunk.getPos().x(), chunk.getPos().z())) return;
        int populationEra=br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(chunk.getPos().x(),chunk.getPos().z());
        if(populationEra==5||populationEra==6) {
            adventurePopulation(populationEra).populate(level,chunk.getPos().x(),chunk.getPos().z());return;
        }
        if(br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(chunk.getPos().x(),chunk.getPos().z())==4) {
            betaPopulation().populate(level,chunk.getPos().x(),chunk.getPos().z());return;
        }
        if(br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(chunk.getPos().x(),chunk.getPos().z())==3) {
            halloweenPopulation().populate(level,chunk.getPos().x(),chunk.getPos().z());return;
        }
        // Future terrain bases do not inherit Infdev decoration or modern features accidentally.
        if(br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(chunk.getPos().x(),chunk.getPos().z())>2)return;
        InfdevPopulation.populate(level, chunk.getPos().x(), chunk.getPos().z(),
            backends.legacy(br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(chunk.getPos().x(), chunk.getPos().z())));
    }
    @Override public void spawnOriginalMobs(WorldGenRegion region) { }
    @Override public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor accessor, RandomState random) {
        if (locked(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) {
            BlockState[] air = new BlockState[128];
            java.util.Arrays.fill(air, Blocks.AIR.defaultBlockState());
            return new NoiseColumn(0, air);
        }
        int era=br.com.mura.betaminecraftmod.progression.GenerationRouter.plannedEra(Math.floorDiv(x,16),Math.floorDiv(z,16)).ordinal();
        if(era<=5){
            byte[] data=sample(Math.floorDiv(x,16),Math.floorDiv(z,16));BlockState[] column=new BlockState[128];
            for(int y=0;y<128;y++)column[y]=block(Byte.toUnsignedInt(data[InfdevTerrain.index(x&15,y,z&15)]));
            return new NoiseColumn(0,column);
        }
        TerrainVolume chunk=volume(Math.floorDiv(x,16),Math.floorDiv(z,16));
        BlockState[] column=new BlockState[chunk.height()];
        for(int y=0;y<column.length;y++)column[y]=volumeBlock(chunk.block(x&15,chunk.minY()+y,z&15));
        return new NoiseColumn(chunk.minY(),column);
    }

    @Override public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor accessor, RandomState random) {
        NoiseColumn column = getBaseColumn(x, z, accessor, random);
        for (int y = accessor.getMaxY(); y >= accessor.getMinY(); y--) if (type.isOpaque().test(column.getBlock(y))) return y + 1;
        return 0;
    }
    @Override public void addDebugScreenInfo(List<String> lines, RandomState random, BlockPos pos, SamplerContext sampler) {
        var definition=GenerationBases.definition(br.com.mura.betaminecraftmod.progression.GenerationRouter.plannedEra(Math.floorDiv(pos.getX(),16),Math.floorDiv(pos.getZ(),16)).ordinal());
        boolean first=definition.era()==6;int height=first&&!firstRelease.equals("1.2.5")?128:definition.height();
        lines.add((first?firstRelease:definition.reference())+" / Y "+definition.minY()+".."+(definition.minY()+height-1)+" / sea "+definition.seaLevel());
        lines.add("Campaign band: " + br.com.mura.betaminecraftmod.progression.ExplorationBands.chunkBand(Math.floorDiv(pos.getX(), 16), Math.floorDiv(pos.getZ(), 16)));
    }
    private TerrainVolume volume(int x,int z){
        int era=br.com.mura.betaminecraftmod.progression.GenerationRouter.availableEra(x,z).ordinal();
        // Retain existing byte-for-byte legacy output and its compact cache.
        if(era<=5)return TerrainVolume.legacy(sample(x,z),128);
        long key=((long)x&0xffffffffL)|((long)z<<32);
        var result=volumeSamples.get(key);
        if(result==null){result=generationBases.generate(era,x,z,era>=13?modernBase():null);volumeSamples.put(key,result);}
        return result;
    }
    private static final java.util.Map<String,BlockState> PALETTE_STATES=new java.util.concurrent.ConcurrentHashMap<>();
    private static BlockState volumeBlock(String name){
        return PALETTE_STATES.computeIfAbsent(name,id->{
            var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK,net.minecraft.resources.Identifier.parse(id));
            return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOrThrow(key).value().defaultBlockState();
        });
    }
    private byte[] sample(int x, int z) {
        long key = ((long) x & 0xffffffffL) | ((long) z << 32);
        byte[] blocks = samples.get(key);
        if (blocks == null) {
            int era = br.com.mura.betaminecraftmod.progression.GenerationRouter.availableEra(x,z).ordinal();
            blocks = backends.sampler(era).generate(x,z);
            samples.put(key, blocks);
        }
        return blocks;
    }
    private static BlockState block(int id) {
        return switch (id) {
            case 1 -> Blocks.STONE.defaultBlockState();
            case 2 -> Blocks.GRASS_BLOCK.defaultBlockState();
            case 3 -> Blocks.DIRT.defaultBlockState();
            case 7 -> Blocks.BEDROCK.defaultBlockState();
            case 8, 9 -> Blocks.WATER.defaultBlockState();
            case 10, 11 -> Blocks.LAVA.defaultBlockState();
            case 12 -> Blocks.SAND.defaultBlockState();
            case 13 -> Blocks.GRAVEL.defaultBlockState();
            case 24 -> Blocks.SANDSTONE.defaultBlockState();
            case 79 -> Blocks.ICE.defaultBlockState();
            case 80 -> Blocks.SNOW_BLOCK.defaultBlockState();
            case 110 -> Blocks.MYCELIUM.defaultBlockState();
            default -> Blocks.AIR.defaultBlockState();
        };
    }
}
