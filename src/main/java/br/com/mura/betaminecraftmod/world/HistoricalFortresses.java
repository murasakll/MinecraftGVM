package br.com.mura.betaminecraftmod.world;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.structure.*;

/** Deterministic historical starts. Decoration writes only the current release-era chunk. */
public final class HistoricalFortresses {
    private final long seed;
    private final HistoricalNetherChunkGenerator gate;
    private volatile HistoricalFortressResources.Plan resourcePlan;
    public HistoricalFortressResources.Plan resourcePlan() {
        if(!gate.resourceGuarantee())return new HistoricalFortressResources.Plan(false,0,0);
        var result=resourcePlan;
        if(result==null)synchronized(this){result=resourcePlan;if(result==null)resourcePlan=result=HistoricalFortressResources.assess(seed,gate);}
        return result;
    }
    private final Map<Long,List<BoundingBox>> bounds=Collections.synchronizedMap(new LinkedHashMap<>(32,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<Long,List<BoundingBox>> e){return size()>32;}});
    public HistoricalFortresses(long seed,HistoricalNetherChunkGenerator gate){this.seed=seed;this.gate=gate;}
    private HistoricalFortressLayout.Layout layout(HistoricalFortressPlacement.Start start) {
        var random=new LegacyRandomSource(start.regionSeed());
        random.nextInt();random.nextInt(3);random.nextInt(8);random.nextInt(8);
        return HistoricalFortressLayout.assemble(random,start.x(),start.z());
    }
    private List<HistoricalFortressPlacement.Start> nearby(int cx,int cz) {
        var result=new ArrayList<HistoricalFortressPlacement.Start>();
        for(int rx=Math.floorDiv(cx-8,16);rx<=Math.floorDiv(cx+8,16);rx++)for(int rz=Math.floorDiv(cz-8,16);rz<=Math.floorDiv(cz+8,16);rz++) {
            var start=HistoricalFortressPlacement.inRegion(seed,rx,rz);
            if(start!=null&&Math.abs(start.x()-cx)<=8&&Math.abs(start.z()-cz)<=8)result.add(start);
        }
        return result;
    }
    public boolean contains(BlockPos pos) {
        int cx=Math.floorDiv(pos.getX(),16),cz=Math.floorDiv(pos.getZ(),16);
        if(gate.locked(cx,cz)||gate.bandAt(cx,cz)!=6)return false;
        if(resourcePlan().pieces().stream().anyMatch(piece->piece.getBoundingBox().isInside(pos)))return true;
        for(var start:nearby(cx,cz)) {
            long key=(start.x()&0xffffffffL)|((long)start.z()<<32);
            var boxes=bounds.computeIfAbsent(key,ignored->layout(start).pieces().stream().map(p->p.getBoundingBox()).toList());
            if(boxes.stream().anyMatch(box->box.isInside(pos)))return true;
        }
        return false;
    }
    public void populate(WorldGenLevel level,ChunkAccess chunk,StructureManager structures) {
        int cx=chunk.getPos().x(),cz=chunk.getPos().z();
        if(gate.locked(cx,cz)||gate.bandAt(cx,cz)!=6)return;
        var clip=new BoundingBox(cx*16,0,cz*16,cx*16+15,127,cz*16+15);
        for(var start:nearby(cx,cz)) {
            // Fresh mutable pieces: spawner flags cannot depend on which neighboring chunk ran first.
            var layout=layout(start);
            var random=RandomSource.create(seed^(long)cx*341873128712L^(long)cz*132897987541L);
            for(var piece:layout.pieces())if(piece.getBoundingBox().intersects(clip))piece.postProcess(level,structures,gate,random,clip,chunk.getPos(),new BlockPos(start.x()*16+2,64,start.z()*16+2));
        }
        var random=RandomSource.create(seed^0x5245534F55524345L);
        for(var piece:resourcePlan().pieces())if(piece.getBoundingBox().intersects(clip))piece.postProcess(level,structures,gate,random,clip,chunk.getPos(),BlockPos.ZERO);
        if(gate.resourceGuarantee()) {
            // Preserve essential anchors if two natural fortresses overlap. This runs only during
            // first decoration, after all pieces in this chunk; never on an existing player's build.
            var plan=resourcePlan();var blaze=plan.blaze();var wart=plan.wart();
            if(clip.isInside(blaze)) {
                if(!level.getBlockState(blaze).is(net.minecraft.world.level.block.Blocks.SPAWNER))level.setBlock(blaze,net.minecraft.world.level.block.Blocks.SPAWNER.defaultBlockState(),2);
                if(level.getBlockEntity(blaze) instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity entity)entity.setEntityId(net.minecraft.world.entity.EntityTypes.BLAZE,random);
            }
            if(clip.isInside(wart)) {
                if(!level.getBlockState(wart.below()).is(net.minecraft.world.level.block.Blocks.SOUL_SAND))level.setBlock(wart.below(),net.minecraft.world.level.block.Blocks.SOUL_SAND.defaultBlockState(),2);
                if(!level.getBlockState(wart).is(net.minecraft.world.level.block.Blocks.NETHER_WART))level.setBlock(wart,net.minecraft.world.level.block.Blocks.NETHER_WART.defaultBlockState(),2);
            }
        }
    }
}
