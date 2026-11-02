package br.com.mura.betaminecraftmod.world;
import java.util.Random;
import static br.com.mura.betaminecraftmod.world.InfdevTerrain.index;
import static br.com.mura.betaminecraftmod.world.InfdevNoise.lerp;

/** Beta 1.8.1 density, surface, caves and ravines. Structures and population remain separate. */
public class AdventureTerrain {
    private static final float[] BASE={-1,.1f,.1f,.2f,.1f,.1f,-.2f,-.5f};
    private static final float[] SCALE={.5f,.3f,.2f,1.8f,.3f,.4f,.1f,0};
    private final AdventureBiomes biomes;
    private final long seed;
    private final InfdevNoise low,high,blend,surface,depth;
    public AdventureTerrain(long seed){
        this.seed=seed;biomes=new AdventureBiomes(seed);var random=new Random(seed);
        low=new InfdevNoise(random,16);high=new InfdevNoise(random,16);blend=new InfdevNoise(random,8);surface=new InfdevNoise(random,4);
        new InfdevNoise(random,10);depth=new InfdevNoise(random,16);
    }
    public byte[] generate(int cx,int cz){
        byte[] blocks=generateBase(cx,cz);
        new AdventureCaves(seed).carve(blocks,cx,cz);
        new AdventureRavines(seed).carve(blocks,cx,cz);
        return blocks;
    }
    public byte[] generateBase(int cx,int cz){
        var blocks=new byte[32768];double[] density=density(cx*4,cz*4);
        for(int gx=0;gx<4;gx++)for(int gz=0;gz<4;gz++)for(int gy=0;gy<16;gy++){
            int p=(gx*5+gz)*17+gy;
            double a=density[p],b=density[p+17],c=density[p+85],d=density[p+102];
            double da=(density[p+1]-a)*.125,db=(density[p+18]-b)*.125,dc=(density[p+86]-c)*.125,dd=(density[p+103]-d)*.125;
            for(int y=0;y<8;y++){
                double e=a,f=b,de=(c-a)*.25,df=(d-b)*.25;
                for(int x=0;x<4;x++){
                    double value=e,step=(f-e)*.25;int by=gy*8+y;
                    for(int z=0;z<4;z++){blocks[index(gx*4+x,by,gz*4+z)]=(byte)(value>0?1:by<63?9:0);value+=step;}e+=de;f+=df;
                }a+=da;b+=db;c+=dc;d+=dd;
            }
        }
        surface(blocks,cx,cz);return blocks;
    }
    protected int[] biomeGrid(int x,int z,int w,int h,boolean coarse){return coarse?biomes.coarse(x,z,w,h):biomes.blocks(x,z,w,h);}
    protected float base(int biome){return BASE[biome];}
    protected float scale(int biome){return SCALE[biome];}
    protected int top(int biome){return biome==2?12:2;}
    protected int filler(int biome){return biome==2?12:3;}
    protected int fluid(int biome){return 9;}
    private double[] density(int x,int z){
        int[] ids=biomeGrid(x-2,z-2,10,10,true);
        double[] land=depth.wrapped(x,10,z,5,1,5,200,1,200);
        double[] mix=blend.wrapped(x,0,z,5,17,5,684.412/80,684.412/160,684.412/80),a=low.wrapped(x,0,z,5,17,5,684.412,684.412,684.412),b=high.wrapped(x,0,z,5,17,5,684.412,684.412,684.412),out=new double[425];
        for(int gx=0;gx<5;gx++)for(int gz=0;gz<5;gz++){
            int center=ids[gx+2+(gz+2)*10];float rough=0,base=0,total=0;
            for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
                int biome=ids[gx+dx+2+(gz+dz+2)*10];float weight=10f/(float)Math.sqrt(dx*dx+dz*dz+.2F)/(base(biome)+2f);
                if(base(biome)>base(center))weight/=2;rough+=scale(biome)*weight;base+=base(biome)*weight;total+=weight;
            }
            rough/=total;base/=total;rough=rough*.9f+.1f;base=(base*4f-1f)/8f;
            int col=gx*5+gz;double offset=land[col]/8000;if(offset<0)offset=-offset*.3;offset=offset*3-2;
            if(offset<0)offset=Math.max(-1,offset/2)/1.4/2;else offset=Math.min(1,offset)/8;
            double centerY=17/2.0+(base+offset*.2)*17/16*4;
            for(int y=0;y<17;y++){
                int i=col*17+y;double gradient=(y-centerY)*12*128/128/rough;if(gradient<0)gradient*=4;
                double amount=(mix[i]/10+1)/2,value=(amount<0?a[i]/512:amount>1?b[i]/512:lerp(amount,a[i]/512,b[i]/512))-gradient;
                if(y>13){double fade=(y-13)/3.0F;value=value*(1-fade)-10*fade;}out[i]=value;
            }
        }return out;
    }
    private void surface(byte[] blocks,int cx,int cz){
        var random=new Random(cx*341873128712L+cz*132897987541L);int[] ids=biomeGrid(cx*16,cz*16,16,16,false);
        double[] noise=surface.wrapped(cx*16,cz*16,0,16,16,1,.0625,.0625,.0625);
        for(int z=0;z<16;z++)for(int x=0;x<16;x++){
            int biome=ids[x+z*16],top=top(biome),filler=filler(biome);
            int thickness=(int)(noise[z+x*16]/3+3+random.nextDouble()*.25),remaining=-1;
            for(int y=127;y>=0;y--){int i=index(x,y,z);
                if(y<=random.nextInt(5))blocks[i]=7;
                else if(blocks[i]==0)remaining=-1;
                else if(blocks[i]==1){
                    if(remaining==-1){
                        if(thickness<=0){top=0;filler=1;}else if(y>=59&&y<=64){top=top(biome);filler=filler(biome);}
                        if(y<63&&top==0)top=fluid(biome);remaining=thickness;blocks[i]=(byte)(y>=62?top:filler);
                    }else if(remaining>0){remaining--;blocks[i]=(byte)filler;if(remaining==0&&filler==12){remaining=random.nextInt(4);filler=24;}}
                }
            }
        }
    }
}
