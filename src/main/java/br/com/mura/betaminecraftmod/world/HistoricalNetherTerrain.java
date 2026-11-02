package br.com.mura.betaminecraftmod.world;

import java.util.Random;
import static br.com.mura.betaminecraftmod.world.InfdevTerrain.index;

/** Alpha Halloween Nether density and surface candidate. Caves/population are separate. */
public final class HistoricalNetherTerrain {
    private final InfdevNoise low,high,blend,surface,thickness;
    private final long seed;
    private final int era;
    public HistoricalNetherTerrain(long seed) {
        this(seed,3);
    }
    public HistoricalNetherTerrain(long seed,int era) {
        if(era<3||era>6)throw new IllegalArgumentException("Unsupported Nether reference "+era);
        this.seed=seed;this.era=era;
        var random=new Random(seed);low=new InfdevNoise(random,16);high=new InfdevNoise(random,16);blend=new InfdevNoise(random,8);
        surface=new InfdevNoise(random,4);thickness=new InfdevNoise(random,4);
        // The original constructs two additional octave fields after these; they do not affect density.
    }
    public byte[] generate(int cx,int cz){var blocks=generateSurface(cx,cz);new InfdevCaves(seed,false,true,era>=5).carve(blocks,cx,cz);return blocks;}
    public byte[] generateBase(int cx,int cz) {
        var density=density(cx*4,cz*4);var blocks=new byte[32768];
        for(int gx=0;gx<4;gx++)for(int gz=0;gz<4;gz++)for(int gy=0;gy<16;gy++) {
            int p=(gx*5+gz)*17+gy;double a=density[p],b=density[p+17],c=density[p+85],d=density[p+102];
            double da=(density[p+1]-a)*.125,db=(density[p+18]-b)*.125,dc=(density[p+86]-c)*.125,dd=(density[p+103]-d)*.125;
            for(int dy=0;dy<8;dy++) {
                double left=a,right=b,dxLeft=(c-a)*.25,dxRight=(d-b)*.25;
                for(int dx=0;dx<4;dx++) {
                    double value=left,step=(right-left)*.25;int y=gy*8+dy;
                    for(int dz=0;dz<4;dz++){blocks[index(gx*4+dx,y,gz*4+dz)]=(byte)(value>0?87:y<32?11:0);value+=step;}
                    left+=dxLeft;right+=dxRight;
                }
                a+=da;b+=db;c+=dc;d+=dd;
            }
        }
        return blocks;
    }
    public byte[] generateSurface(int cx,int cz) {
        var blocks=generateBase(cx,cz);var random=new Random(cx*341873128712L+cz*132897987541L);
        var soul=grid(surface,cx*16,cz*16,0,16,16,1,.03125,.03125,1);
        var gravel=era>=5?surface.wrapped(cx*16,109,cz*16,16,1,16,.03125,1,.03125):surface.flat((era==3?cz:cx)*16,(era==3?cx:cz)*16,16,16,.03125,.03125);
        var depth=grid(thickness,cx*16,cz*16,0,16,16,1,.0625,.0625,.0625);
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int p=x+z*16;boolean sandy=soul[p]+random.nextDouble()*.2>0,gravelly=gravel[p]+random.nextDouble()*.2>0;
            int layers=(int)(depth[p]/3+3+random.nextDouble()*.25),remaining=-1,top=87,filler=87;
            for(int y=127;y>=0;y--) {
                int at=era>=4?index(z,y,x):index(x,y,z);
                if(y>=127-random.nextInt(5))blocks[at]=7;
                else if(y<=random.nextInt(5))blocks[at]=7;
                else if(blocks[at]==0)remaining=-1;
                else if(blocks[at]==87) {
                    if(remaining<0) {
                        if(layers<=0){top=0;filler=87;}
                        else if(y>=60&&y<=65){top=gravelly?13:87;filler=87;if(sandy){top=88;filler=88;}}
                        if(y<64&&top==0)top=11;
                        remaining=layers;blocks[at]=(byte)(y>=63?top:filler);
                    }else if(remaining>0){remaining--;blocks[at]=(byte)filler;}
                }
            }
        }
        return blocks;
    }
    private double[] density(int x,int z) {
        var a=grid(low,x,0,z,5,17,5,684.412,2053.236,684.412);
        var b=grid(high,x,0,z,5,17,5,684.412,2053.236,684.412);
        var mix=grid(blend,x,0,z,5,17,5,684.412/80,2053.236/60,684.412/80);
        var out=new double[a.length];
        for(int i=0;i<out.length;i++) {
            int y=i%17;double edge=y>8?16-y:y;
            double shape=Math.cos(y*Math.PI*6/17)*2;
            if(edge<4)shape-=Math.pow(4-edge,3)*10;
            double weight=(mix[i]/10+1)/2,value=weight<0?a[i]/512:weight>1?b[i]/512:a[i]/512+(b[i]/512-a[i]/512)*weight;
            value-=shape;
            if(y>13){double fade=(y-13)/3f;value=value*(1-fade)-10*fade;}
            out[i]=value;
        }
        return out;
    }
    private double[] grid(InfdevNoise noise,int x,int y,int z,int nx,int ny,int nz,double sx,double sy,double sz){return era>=5?noise.wrapped(x,y,z,nx,ny,nz,sx,sy,sz):noise.grid3d(x,y,z,nx,ny,nz,sx,sy,sz);}
}
