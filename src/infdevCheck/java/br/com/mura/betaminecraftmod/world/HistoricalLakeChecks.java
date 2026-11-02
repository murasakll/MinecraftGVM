package br.com.mura.betaminecraftmod.world;

import java.util.Arrays;
import java.util.Random;

final class HistoricalLakeChecks {
    static void run() {
        for(long seed:new long[]{0,1,-987654321,Long.MIN_VALUE,Long.MAX_VALUE}) {
            var mask=HistoricalLakeShape.create(new Random(seed));
            if(!Arrays.equals(mask,HistoricalLakeShape.create(new Random(seed))))throw new AssertionError("Unstable lake mask");
            int body=0,shell=0;
            for(int y=0;y<8;y++)for(int z=0;z<16;z++)for(int x=0;x<16;x++) {
                boolean inside=mask[HistoricalLakeShape.index(x,y,z)];
                if(inside){body++;if(x==0||x==15||z==0||z==15||y==0||y==7)throw new AssertionError("Unsealed mask");}
                boolean adjacent=false;
                for(int[] d:new int[][]{{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}}) {
                    int nx=x+d[0],ny=y+d[1],nz=z+d[2];
                    if(nx>=0&&nx<16&&ny>=0&&ny<8&&nz>=0&&nz<16)adjacent|=mask[HistoricalLakeShape.index(nx,ny,nz)];
                }
                if(HistoricalLakeShape.boundary(mask,x,y,z)!=(!inside&&adjacent))throw new AssertionError("Incorrect lake shell");
                if(!inside&&adjacent)shell++;
            }
            if(body==0||shell==0)throw new AssertionError("Empty lake");
        }
        System.out.println("HISTORICAL_LAKE_SHAPE SUCCESS: five signed/extreme seeds, sealed footprint, complete six-face shell");
    }
}
