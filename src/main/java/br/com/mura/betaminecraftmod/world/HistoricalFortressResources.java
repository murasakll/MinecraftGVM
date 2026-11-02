package br.com.mura.betaminecraftmod.world;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;

/** Campaign resource policy, deliberately distinct from the original seed's natural structures. */
public final class HistoricalFortressResources {
    public record Plan(boolean required,int x,int z,BlockPos naturalBlaze,BlockPos naturalWart) {
        public Plan(boolean required,int x,int z){this(required,x,z,null,null);}
        public BlockPos blaze(){return naturalBlaze!=null?naturalBlaze:new BlockPos(x,59,z-24);}
        public BlockPos wart(){return naturalWart!=null?naturalWart:new BlockPos(x-3,56,z+16);}
        public List<StructurePiece> pieces() {
            if(!required)return List.of();
            var pieces=new ArrayList<StructurePiece>();
            pieces.add(new NetherFortressPieces.MonsterThrone(0,new BoundingBox(x-3,54,z-27,x+3,61,z-19),Direction.NORTH));
            for(int i=0;i<6;i++)pieces.add(new NetherFortressPieces.CastleSmallCorridorPiece(0,new BoundingBox(x-2,54,z-18+i*5,x+2,60,z-14+i*5),Direction.SOUTH));
            pieces.add(new NetherFortressPieces.CastleStalkRoom(0,new BoundingBox(x-6,51,z+12,x+6,64,z+24),Direction.SOUTH));
            return List.copyOf(pieces);
        }
    }
    private HistoricalFortressResources() { }
    public static Plan assess(long seed,HistoricalNetherChunkGenerator gate) {
        int outer=224/gate.ringScale();BlockPos blaze=null,wart=null;
        for(int rx=Math.floorDiv(-outer-8,16);rx<=Math.floorDiv(outer+7,16);rx++)for(int rz=Math.floorDiv(-outer-8,16);rz<=Math.floorDiv(outer+7,16);rz++) {
            var start=HistoricalFortressPlacement.inRegion(seed,rx,rz);if(start==null)continue;
            var random=new LegacyRandomSource(start.regionSeed());random.nextInt();random.nextInt(3);random.nextInt(8);random.nextInt(8);
            for(var piece:HistoricalFortressLayout.assemble(random,start.x(),start.z()).pieces()) {
                if(blaze==null&&piece instanceof NetherFortressPieces.MonsterThrone){var pos=position(piece,3,5,5);if(available(gate,start,pos))blaze=pos;}
                if(wart==null&&piece instanceof NetherFortressPieces.CastleStalkRoom)for(int x:new int[]{3,4,8,9})for(int z=4;z<=8;z++){var pos=position(piece,x,5,z);if(available(gate,start,pos))wart=pos;}
                if(blaze!=null&&wart!=null)return new Plan(false,0,0,blaze,wart);
            }
        }
        int x=(192/gate.ringScale()+outer)*8;
        int z=new Random(seed^0x4F4C44414745534CL).nextInt(257)-128;
        return new Plan(true,x,z);
    }
    private static boolean available(HistoricalNetherChunkGenerator gate,HistoricalFortressPlacement.Start start,BlockPos pos) {
        int cx=Math.floorDiv(pos.getX(),16),cz=Math.floorDiv(pos.getZ(),16);
        return pos.getY()>1&&pos.getY()<127&&gate.bandAt(cx,cz)==6&&Math.abs(start.x()-cx)<=8&&Math.abs(start.z()-cz)<=8;
    }
    private static BlockPos position(StructurePiece piece,int x,int y,int z) {
        var b=piece.getBoundingBox();var d=piece.getOrientation();
        int wx=switch(d){case NORTH,SOUTH->b.minX()+x;case WEST->b.maxX()-z;case EAST->b.minX()+z;default->throw new IllegalStateException();};
        int wz=switch(d){case NORTH->b.maxZ()-z;case SOUTH->b.minZ()+z;case WEST,EAST->b.minZ()+x;default->throw new IllegalStateException();};
        return new BlockPos(wx,b.minY()+y,wz);
    }
}
