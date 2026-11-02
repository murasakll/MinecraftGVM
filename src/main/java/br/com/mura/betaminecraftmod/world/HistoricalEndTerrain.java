package br.com.mura.betaminecraftmod.world;

import java.util.Random;
import static br.com.mura.betaminecraftmod.world.InfdevTerrain.index;

/** Release 1.0 central island only: no later outer islands, cities, chorus or gateways. */
public final class HistoricalEndTerrain {
    private final InfdevNoise low,high,blend;
    public HistoricalEndTerrain(long seed){var random=new Random(seed);low=new InfdevNoise(random,16);high=new InfdevNoise(random,16);blend=new InfdevNoise(random,8);}
    public byte[] generate(int cx,int cz) {
        int x=cx*2,z=cz*2;var a=low.wrapped(x,0,z,3,33,3,1368.824,684.412,1368.824);
        var b=high.wrapped(x,0,z,3,33,3,1368.824,684.412,1368.824);
        var mix=blend.wrapped(x,0,z,3,33,3,1368.824/80,684.412/160,1368.824/80);
        var density=new double[a.length];
        for(int gx=0;gx<3;gx++)for(int gz=0;gz<3;gz++) {
            float px=x+gx,pz=z+gz;float island=Math.clamp(100-(float)Math.sqrt(px*px+pz*pz)*8,-100,80);
            for(int y=0;y<33;y++) {
                int at=(gx*3+gz)*33+y;double weight=(mix[at]/10+1)/2;
                double value=weight<0?a[at]/512:weight>1?b[at]/512:a[at]/512+(b[at]/512-a[at]/512)*weight;
                value=value-8+island;
                if(y>14){double fade=Math.clamp((y-14)/64f,0,1);value=value*(1-fade)-3000*fade;}
                if(y<8){double fade=(8-y)/7f;value=value*(1-fade)-30*fade;}
                density[at]=value;
            }
        }
        var blocks=new byte[32768];
        for(int gx=0;gx<2;gx++)for(int gz=0;gz<2;gz++)for(int gy=0;gy<32;gy++) {
            int at=(gx*3+gz)*33+gy;double v00=density[at],v01=density[at+33],v10=density[at+99],v11=density[at+132];
            double d00=(density[at+1]-v00)*.25,d01=(density[at+34]-v01)*.25,d10=(density[at+100]-v10)*.25,d11=(density[at+133]-v11)*.25;
            for(int dy=0;dy<4;dy++) {
                double left=v00,right=v01,dxLeft=(v10-v00)*.125,dxRight=(v11-v01)*.125;
                for(int dx=0;dx<8;dx++) {
                    double value=left,step=(right-left)*.125;
                    for(int dz=0;dz<8;dz++){blocks[index(gx*8+dx,gy*4+dy,gz*8+dz)]=(byte)(value>0?121:0);value+=step;}
                    left+=dxLeft;right+=dxRight;
                }
                v00+=d00;v01+=d01;v10+=d10;v11+=d11;
            }
        }
        return blocks;
    }
}
