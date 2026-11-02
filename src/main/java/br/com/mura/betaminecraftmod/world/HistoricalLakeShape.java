package br.com.mura.betaminecraftmod.world;

import java.util.Random;

/** Registry-free ellipsoid mask, reusable by historical lake families. */
public final class HistoricalLakeShape {
    public static final int WIDTH=16,HEIGHT=8;
    private HistoricalLakeShape() { }
    public static int index(int x,int y,int z){return x+WIDTH*(z+WIDTH*y);}
    public static boolean[] create(Random random) {
        boolean[] mask=new boolean[WIDTH*WIDTH*HEIGHT];int count=4+random.nextInt(4);
        for(int shape=0;shape<count;shape++) {
            double dx=3+random.nextDouble()*6,dy=2+random.nextDouble()*4,dz=3+random.nextDouble()*6;
            double cx=1+dx/2+random.nextDouble()*(14-dx),cy=2+dy/2+random.nextDouble()*(4-dy),cz=1+dz/2+random.nextDouble()*(14-dz);
            for(int y=1;y<HEIGHT-1;y++)for(int z=1;z<WIDTH-1;z++)for(int x=1;x<WIDTH-1;x++) {
                double nx=(x-cx)/(dx/2),ny=(y-cy)/(dy/2),nz=(z-cz)/(dz/2);
                if(nx*nx+ny*ny+nz*nz<1)mask[index(x,y,z)]=true;
            }
        }
        return mask;
    }
    public static boolean boundary(boolean[] mask,int x,int y,int z) {
        if(mask[index(x,y,z)])return false;
        return x>0&&mask[index(x-1,y,z)]||x+1<WIDTH&&mask[index(x+1,y,z)]
            ||z>0&&mask[index(x,y,z-1)]||z+1<WIDTH&&mask[index(x,y,z+1)]
            ||y>0&&mask[index(x,y-1,z)]||y+1<HEIGHT&&mask[index(x,y+1,z)];
    }
}
