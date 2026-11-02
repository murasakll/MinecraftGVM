package br.com.mura.betaminecraftmod.world;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/** Original feature geometry with an explicit, order-independent candidate scheduling stream. */
final class HistoricalEndPopulation {
    private final long seed;
    private final CampaignGenerationGate gate;
    HistoricalEndPopulation(long seed,CampaignGenerationGate gate){this.seed=seed;this.gate=gate;}
    void populate(WorldGenLevel level,int cx,int cz) {
        if(gate.locked(cx,cz))return;
        Random random=HalloweenPopulationSettings.stream(seed,cx,cz,400);
        if(random.nextInt(5)!=0)return;
        int x=cx*16+8+random.nextInt(16),z=cz*16+8+random.nextInt(16);
        if(gate.locked(Math.floorDiv(x,16),Math.floorDiv(z,16)))return;
        int y=level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z);
        place(level,random,x,y,z);
    }
    boolean place(WorldGenLevel level,Random random,int x,int y,int z) {
        if(y<1||y>=128||gate.locked(Math.floorDiv(x,16),Math.floorDiv(z,16)))return false;
        var plan=HistoricalEndSpike.plan(p->{
            if(gate.locked(Math.floorDiv(p.x(),16),Math.floorDiv(p.z(),16)))return -1;
            var block=level.getBlockState(new BlockPos(p.x(),p.y(),p.z()));
            return block.isAir()?0:block.is(Blocks.END_STONE)?121:-1;
        },random,x,y,z);
        if(plan==null)return false;
        for(var pos:plan.blocks().keySet()) {
            var at=new BlockPos(pos.x(),pos.y(),pos.z());
            if(gate.locked(Math.floorDiv(pos.x(),16),Math.floorDiv(pos.z(),16))||level.getBlockEntity(at)!=null)return false;
        }
        var crystal=EntityTypes.END_CRYSTAL.create(level.getLevel(),EntitySpawnReason.STRUCTURE);
        if(crystal==null)return false;
        crystal.snapTo(plan.crystalX(),plan.crystalY(),plan.crystalZ(),plan.yaw(),0);
        for(var entry:plan.blocks().entrySet()) {
            var p=entry.getKey();level.setBlock(new BlockPos(p.x(),p.y(),p.z()),(entry.getValue()==7?Blocks.BEDROCK:Blocks.OBSIDIAN).defaultBlockState(),2);
        }
        return level.addFreshEntity(crystal);
    }
}
