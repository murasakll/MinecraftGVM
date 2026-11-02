package br.com.mura.betaminecraftmod.world;

import java.util.Random;
import static br.com.mura.betaminecraftmod.world.InfdevTerrain.index;
import static br.com.mura.betaminecraftmod.world.InfdevNoise.lerp;

/** Alpha 1.2.6 base terrain. Pure generation only; decoration/content integration is a separate stage. */
public class HalloweenTerrain {
    private final long seed;
    private final boolean beta;
    private final HalloweenClimate climate;
    private final InfdevNoise low,high,blend,beaches,thickness,scale,depth;
    public HalloweenTerrain(long seed){this(seed,false);}
    protected HalloweenTerrain(long seed,boolean beta){
        this.beta=beta;
        this.seed=seed;climate=new HalloweenClimate(seed);var random=new Random(seed);
        low=new InfdevNoise(random,16);high=new InfdevNoise(random,16);blend=new InfdevNoise(random,8);
        beaches=new InfdevNoise(random,4);thickness=new InfdevNoise(random,4);scale=new InfdevNoise(random,10);depth=new InfdevNoise(random,16);
    }
    public byte[] generate(int cx,int cz){
        byte[] blocks=new byte[32768];var weather=climate.sample(cx*16,cz*16,16,16);double[] density=density(cx*4,cz*4,weather);
        for(int gx=0;gx<4;gx++)for(int gz=0;gz<4;gz++)for(int gy=0;gy<16;gy++){
            int p=(gx*5+gz)*17+gy;
            double a=density[p],b=density[p+17],c=density[p+85],d=density[p+102];
            double da=(density[p+1]-a)*.125,db=(density[p+18]-b)*.125,dc=(density[p+86]-c)*.125,dd=(density[p+103]-d)*.125;
            for(int y=0;y<8;y++){
                double e=a,f=b,de=(c-a)*.25,df=(d-b)*.25;
                for(int x=0;x<4;x++){
                    double value=e,step=(f-e)*.25;int bx=gx*4+x,by=gy*8+y;
                    for(int z=0;z<4;z++){
                        int bz=gz*4+z,block=by<64?(weather.temperature()[bx*16+bz]<.5&&by>=63?79:9):0;
                        if(value>0)block=1;blocks[index(bx,by,bz)]=(byte)block;value+=step;
                    }
                    e+=de;f+=df;
                }
                a+=da;b+=db;c+=dc;d+=dd;
            }
        }
        surface(blocks,cx,cz,weather);new InfdevCaves(seed,true).carve(blocks,cx,cz);return blocks;
    }
    private double[] density(int x,int z,HalloweenClimate.Grid climate){
        double[] s=scale.flat(x,z,5,5,1.121,1.121),d=depth.flat(x,z,5,5,200,200);
        double[] mix=blend.grid(x,z,5,17,5,684.412/80,684.412/160,684.412/80);
        double[] lo=low.grid(x,z,5,17,5,684.412,684.412,684.412),hi=high.grid(x,z,5,17,5,684.412,684.412,684.412),out=new double[425];
        for(int gx=0;gx<5;gx++)for(int gz=0;gz<5;gz++){
            int col=gx*5+gz,climateIndex=(gx*3+1)*16+gz*3+1;
            double heat=climate.temperature()[climateIndex],wet=climate.rainfall()[climateIndex]*heat;
            double climateScale=1-wet;climateScale*=climateScale;climateScale*=climateScale;climateScale=1-climateScale;
            double stretch=Math.min(1,(s[col]+256)/512*climateScale),offset=d[col]/8000;
            if(offset<0)offset=-offset*.3;offset=offset*3-2;
            if(offset<0){offset=Math.max(-1,offset/2)/1.4/2;stretch=0;}else offset=Math.min(1,offset)/8;
            stretch=Math.max(0,stretch)+.5;double center=17/2.0+offset*17/16*4;
            for(int y=0;y<17;y++){
                int i=col*17+y;double gradient=(y-center)*12/stretch;if(gradient<0)gradient*=4;
                double amount=(mix[i]/10+1)/2;
                double value=(amount<0?lo[i]/512:amount>1?hi[i]/512:lerp(amount,lo[i]/512,hi[i]/512))-gradient;
                if(y>13){double fade=(y-13)/3.0F;value=value*(1-fade)-10*fade;}out[i]=value;
            }
        }
        return out;
    }
    private void surface(byte[] blocks,int cx,int cz,HalloweenClimate.Grid climate){
        var random=new Random(cx*341873128712L+cz*132897987541L);
        double[] sand=beaches.grid3d(cx*16,cz*16,0,16,16,1,.03125,.03125,1);
        double[] gravel=beaches.flat((beta?cx:cz)*16,(beta?cz:cx)*16,16,16,.03125,.03125);
        double[] layers=thickness.grid3d(cx*16,cz*16,0,16,16,1,.0625,.0625,.0625);
        for(int x=0;x<16;x++)for(int z=0;z<16;z++){
            var biome=climate.biomes()[beta?z*16+x:x*16+z];int biomeTop=biome==HalloweenClimate.Biome.DESERT?12:2,biomeFiller=biomeTop==12?12:3;
            boolean sandy=sand[x+z*16]+random.nextDouble()*.2>0,gravelly=gravel[x+z*16]+random.nextDouble()*.2>3;
            int thickness=(int)(layers[x+z*16]/3+3+random.nextDouble()*.25),remaining=-1,top=biomeTop,filler=biomeFiller;
            for(int y=127;y>=0;y--){int i=beta?index(z,y,x):index(x,y,z);
                if(y<=random.nextInt(5))blocks[i]=7;
                else if(blocks[i]==0)remaining=-1;
                else if(blocks[i]==1){
                    if(remaining==-1){
                        if(thickness<=0){top=0;filler=1;}
                        else if(y>=60&&y<=65){top=biomeTop;filler=biomeFiller;if(gravelly){top=0;filler=13;}if(sandy){top=12;filler=12;}}
                        if(y<64&&top==0)top=9;remaining=thickness;blocks[i]=(byte)(y>=63?top:filler);
                    }else if(remaining>0){remaining--;blocks[i]=(byte)filler;
                        if(beta&&remaining==0&&filler==12){remaining=random.nextInt(4);filler=24;}
                    }
                }
            }
        }
    }
}
