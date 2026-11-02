package br.com.mura.betaminecraftmod.world;

import java.util.BitSet;

/** Historical 256-high configured-carver rasterizer, independent of block registries. */
final class AquaticCarverMask {
    @FunctionalInterface interface Cell {void visit(int x,int y,int z);}
    @FunctionalInterface interface Water {boolean at(int x,int y,int z);}
    static void carve(int cx,int cz,double x,double y,double z,double radius,double vertical,
            float[] weights,BitSet visited,Water water,int highestY,Cell cell){
        double centerX=cx*16+8,centerZ=cz*16+8;
        if(Math.abs(x-centerX)>16+radius*2||Math.abs(z-centerZ)>16+radius*2)return;
        int x0=Math.max(0,(int)Math.floor(x-radius)-cx*16-1),x1=Math.min(16,(int)Math.floor(x+radius)-cx*16+1);
        int z0=Math.max(0,(int)Math.floor(z-radius)-cz*16-1),z1=Math.min(16,(int)Math.floor(z+radius)-cz*16+1);
        int y0=Math.max(1,(int)Math.floor(y-vertical)-1),y1=Math.min(248,(int)Math.floor(y+vertical)+1);
        if(water!=null){
            for(int bx=x0;bx<x1;bx++)for(int bz=z0;bz<z1;bz++)for(int by=y0-1;by<=y1+1;by++){
                if(water.at(bx,by,bz))return;
                if(by!=y1+1&&bx!=x0&&bx!=x1-1&&bz!=z0&&bz!=z1-1)by=y1;
            }
        }
        for(int bx=x0;bx<x1;bx++)for(int bz=z0;bz<z1;bz++){
            double nx=(bx+cx*16+.5-x)/radius,nz=(bz+cz*16+.5-z)/radius,radial=nx*nx+nz*nz;
            if(radial>=1)continue;
            for(int by=y1;by>y0;by--){
                double ny=(by-.5-y)/vertical;
                boolean excluded=weights==null?ny<=-.7||radial+ny*ny>=1:radial*weights[by-1]+ny*ny/6>=1;
                if(excluded||by>highestY)continue;
                int key=bx|(bz<<4)|(by<<8);
                if(visited.get(key))continue;
                visited.set(key);cell.visit(bx,by,bz);
            }
        }
    }
}
