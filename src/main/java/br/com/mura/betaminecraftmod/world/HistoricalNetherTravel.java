package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import static br.com.mura.betaminecraftmod.world.HistoricalNetherPortalLayout.*;

/** Bounded adapter around the original-size search and independent historical construction kernel. */
public final class HistoricalNetherTravel {
    private HistoricalNetherTravel(){}
    public static TeleportTransition destination(ServerLevel source,Entity entity){
        if(!(entity instanceof ServerPlayer)||entity.isPassenger()||entity.isVehicle()||(source.dimension()!=Level.OVERWORLD&&source.dimension()!=Level.NETHER))return null;
        var target=source.getServer().getLevel(source.dimension()==Level.NETHER?Level.OVERWORLD:Level.NETHER);if(target==null)return null;
        var border=target.getWorldBorder();double scale=target.dimension()==Level.NETHER?.125:8;
        // Campaign adapter: reserve enough margin for the entire fallback frame and its ground clearance.
        double x=Math.clamp(entity.getX()*scale,border.getMinX()+4,border.getMaxX()-4),z=Math.clamp(entity.getZ()*scale,border.getMinZ()+4,border.getMaxZ()-4),y=entity.getY();
        var view=new DestinationView(target);if(!view.allowed((int)Math.floor(x),(int)Math.floor(z)))return null;
        var cells=view.portals(x,z);var landing=find(view,cells,x,y,z);
        if(landing==null){
            var plan=create(view,x,y,z,new Random());if(plan==null||!commit(target,view,plan))return null;
            cells=new ArrayList<>();for(var write:plan.writes())if(!write.notifyOnly()&&write.block()==90)cells.add(write.pos());
            landing=find(view,cells,x,y,z);if(landing==null)return null;
        }
        return new TeleportTransition(target,new Vec3(landing.x(),landing.y(),landing.z()),Vec3.ZERO,entity.getYRot(),0,
            TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }
    public static boolean commit(ServerLevel level,View view,Creation plan){
        // No partial frame on a locked band, even if a caller supplies a stale or foreign plan.
        var actualGate=new HistoricalPortalView(level);
        for(var write:plan.writes()){var p=write.pos();if(p.y()<0||p.y()>=128||!view.allowed(p.x(),p.z())||!actualGate.allowed(p.x(),p.z()))return false;}
        var axis=plan.frame().dx()!=0?Direction.Axis.X:Direction.Axis.Z;
        for(var write:plan.writes()){
            var p=write.pos();var pos=new BlockPos(p.x(),p.y(),p.z());
            if(write.notifyOnly()){
                var state=level.getBlockState(pos);level.updateNeighborsAt(pos,state.getBlock());state.updateNeighbourShapes(level,pos,3);
            }else {
                BlockState state=write.block()==49?Blocks.OBSIDIAN.defaultBlockState():write.block()==90?Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS,axis):Blocks.AIR.defaultBlockState();
                level.setBlock(pos,state,write.immediate()?3:18);
            }
        }
        return true;
    }
    private static final class DestinationView implements View {
        private final ServerLevel level;private final HistoricalPortalView gate;private final int era;
        private final Map<ChunkPos,LevelChunk> chunks=new HashMap<>();
        DestinationView(ServerLevel level){this.level=level;gate=new HistoricalPortalView(level);era=CampaignRules.era(level);}
        public boolean allowed(int x,int z){return gate.allowed(x,z);}
        private LevelChunk chunk(int x,int z){if(!allowed(x,z))throw new IllegalStateException("Portal requested locked chunk");return chunks.computeIfAbsent(new ChunkPos(x>>4,z>>4),p->level.getChunk(p.x(),p.z()));}
        private BlockState state(int x,int y,int z){return chunk(x,z).getBlockState(new BlockPos(x,y,z));}
        public int block(int x,int y,int z){if(y<0||y>=128)return 0;if(!allowed(x,z))return -1;var state=state(x,y,z);return state.isAir()?0:state.is(Blocks.OBSIDIAN)?49:state.is(Blocks.FIRE)?51:state.is(Blocks.NETHER_PORTAL)?90:1;}
        public boolean solid(int x,int y,int z){return y>=0&&y<128&&allowed(x,z)&&HistoricalPortalMaterials.solid(era,state(x,y,z));}
        List<Pos> portals(double x,double z){
            var result=new ArrayList<Pos>();var border=level.getWorldBorder();
            int minX=Math.max((int)Math.floor(x)-128,(int)Math.ceil(border.getMinX())),maxX=Math.min((int)Math.floor(x)+128,(int)Math.ceil(border.getMaxX())-1),minZ=Math.max((int)Math.floor(z)-128,(int)Math.ceil(border.getMinZ())),maxZ=Math.min((int)Math.floor(z)+128,(int)Math.ceil(border.getMaxZ())-1);
            for(int cx=minX>>4;cx<=maxX>>4;cx++)for(int cz=minZ>>4;cz<=maxZ>>4;cz++){
                int bx=Math.max(minX,cx*16),bz=Math.max(minZ,cz*16);if(!allowed(bx,bz))continue;
                var chunk=chunk(bx,bz);var sections=chunk.getSections();
                for(int i=0;i<sections.length;i++){
                    int bottom=chunk.getSectionYFromSectionIndex(i)*16;if(bottom<0||bottom>=128||!sections[i].maybeHas(state->state.is(Blocks.NETHER_PORTAL)))continue;
                    for(int px=bx;px<=Math.min(maxX,cx*16+15);px++)for(int pz=bz;pz<=Math.min(maxZ,cz*16+15);pz++)if(allowed(px,pz))for(int py=bottom;py<bottom+16;py++)if(sections[i].getBlockState(px&15,py&15,pz&15).is(Blocks.NETHER_PORTAL))result.add(new Pos(px,py,pz));
                }
            }
            return result;
        }
    }
}
