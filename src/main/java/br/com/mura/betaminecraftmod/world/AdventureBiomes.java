package br.com.mura.betaminecraftmod.world;

/** Independent Beta 1.8.1 / release 1.0 biome and climate layers; no registry or global mutable RNG. */
public final class AdventureBiomes {
    @FunctionalInterface private interface Layer {int[] grid(int x,int z,int width,int depth);}
    private final long seed;
    private final Layer coarse,blocks,heat,wet;
    private final boolean release;
    public AdventureBiomes(long seed){this(seed,false);}
    /** Release branch is the 1.0 layer graph; it does not select later 1.1/1.2.x revisions. */
    public AdventureBiomes(long seed,boolean release){
        this.release=release;
        this.seed=seed;
        Layer land=island();land=zoom(land,2000,true);land=addLand(land,1);
        if(release){
            land=zoom(land,2001,false);land=addLand(land,2);land=map(land,2,(rng,v)->v==0?0:rng.next(5)==0?12:1);
            land=zoom(land,2002,false);land=addLand(land,3);land=zoom(land,2003,false);land=addLand(land,4);land=mushroom(land);
        }else for(int i=1;i<=4;i++){land=zoom(land,2000+i,false);land=addLand(land,i==1?2:3);}
        Layer river=map(land,100,(rng,v)->v>0?rng.next(2)+2:0);
        for(int i=0;i<6;i++)river=zoom(river,1000+i,false);
        river=cross(river,1,(rng,c,w,e,n,s)->c==0||w==0||e==0||n==0||s==0||c!=w||c!=e||c!=n||c!=s?7:-1);
        river=smooth(river,1000);
        int[] pool={2,4,3,6,1,5};Layer biomes=map(land,200,(rng,v)->!release?v>0?pool[rng.next(pool.length)]:0:v==0||v==14?v:v==1?pool[rng.next(pool.length)]:12);
        for(int i=0;i<2;i++)biomes=zoom(biomes,1000+i,false);
        Layer temperatures=map(biomes,0,(rng,v)->climateValue(v,true)),rainfall=map(biomes,0,(rng,v)->climateValue(v,false));
        for(int i=0;i<4;i++){
            biomes=zoom(biomes,1000+i,false);if(i==0){biomes=addLand(biomes,3);if(release)biomes=cross(biomes,1000,(rng,c,w,e,n,s)->c==14&&(w==0||e==0||n==0||s==0)?15:c);}
            temperatures=climateBlend(climateZoom(temperatures,1000+i,false),biomes,i*2+1,true);
            rainfall=climateBlend(climateZoom(rainfall,1000+i,false),biomes,i+1,false);
        }
        for(int i=0;i<2;i++){temperatures=climateZoom(temperatures,1000+i,true);rainfall=climateZoom(rainfall,1000+i,true);}
        heat=temperatures;wet=rainfall;
        biomes=smooth(biomes,1000);final Layer b=biomes,r=river;
        coarse=(x,z,w,h)->{int[] out=b.grid(x,z,w,h),overlay=r.grid(x,z,w,h);for(int i=0;i<out.length;i++)if(out[i]!=0&&overlay[i]>=0)out[i]=!release?overlay[i]:out[i]==12?11:out[i]==14||out[i]==15?15:overlay[i];return out;};
        blocks=voronoi(coarse,10);
    }
    public int[] coarse(int x,int z,int width,int depth){return coarse.grid(x,z,width,depth);}
    public int[] blocks(int x,int z,int width,int depth){return blocks.grid(x,z,width,depth);}
    public record ClimateGrid(int[] temperature,int[] rainfall) { }
    public ClimateGrid climate(int x,int z,int width,int depth){return new ClimateGrid(heat.grid(x,z,width,depth),wet.grid(x,z,width,depth));}
    private static int climateValue(int biome,boolean heat){
        float value=heat?switch(biome){case 1,6->.8F;case 2,8->2F;case 3->.2F;case 4->.7F;case 5->.3F;case 10,11,12,13->0F;case 14,15->.9F;default->.5F;}:switch(biome){case 1->.4F;case 2,8->0F;case 3->.3F;case 4,5->.8F;case 6->.9F;case 14,15->1F;default->.5F;};
        return (int)(value*65536F);
    }
    private Layer climateBlend(Layer interpolated,Layer biomes,int divisor,boolean heat){return(x,z,w,h)->{int[] a=interpolated.grid(x,z,w,h),b=biomes.grid(x,z,w,h);for(int i=0;i<a.length;i++)a[i]+=(climateValue(b[i],heat)-a[i])/divisor;return a;};}
    private Layer climateZoom(Layer parent,long salt,boolean seeded){return(x,z,w,h)->{
        int px=x>>1,pz=z>>1,pw=(w>>1)+3,ph=(h>>1)+3,stride=pw*2;int[] a=parent.grid(px,pz,pw,ph),out=new int[stride*ph*2];
        for(int dz=0;dz<ph-1;dz++)for(int dx=0;dx<pw-1;dx++){
            int nx=(px+dx)<<1,nz=(pz+dz)<<1;var rng=seeded?random(salt,nx,nz):unseeded(nx,nz);
            int nw=a[dx+dz*pw],ne=a[dx+1+dz*pw],sw=a[dx+(dz+1)*pw],se=a[dx+1+(dz+1)*pw],i=dx*2+dz*2*stride;
            out[i]=nw;out[i+stride]=nw+(sw-nw)*rng.next(256)/256;out[i+1]=nw+(ne-nw)*rng.next(256)/256;
            int north=nw+(ne-nw)*rng.next(256)/256,south=sw+(se-sw)*rng.next(256)/256;out[i+stride+1]=north+(south-north)*rng.next(256)/256;
        }return crop(out,stride,x&1,z&1,w,h);
    };}
    // Original climate blend seeds only its biome parent. Its secondary interpolation branch
    // retains worldSeed=0; only the two final interpolation layers receive the world seed.
    private static Rand unseeded(int x,int z){long cell=0;cell=advance(cell,x);cell=advance(cell,z);cell=advance(cell,x);cell=advance(cell,z);return new Rand(0,cell);}
    private Layer mushroom(Layer parent){return(x,z,w,h)->{int stride=w+2;int[] a=parent.grid(x-1,z-1,stride,h+2),out=new int[w*h];for(int dz=0;dz<h;dz++)for(int dx=0;dx<w;dx++){int i=dx+1+(dz+1)*stride;out[dx+dz*w]=a[i]==0&&a[i-stride-1]==0&&a[i-stride+1]==0&&a[i+stride-1]==0&&a[i+stride+1]==0&&random(5,x+dx,z+dz).next(100)==0?14:a[i];}return out;};}
    private static long advance(long v,long add){return v*(v*6364136223846793005L+1442695040888963407L)+add;}
    private Rand random(long salt,int x,int z){
        long base=salt;for(int i=0;i<3;i++)base=advance(base,salt);
        long world=seed;for(int i=0;i<3;i++)world=advance(world,base);
        long cell=world;cell=advance(cell,x);cell=advance(cell,z);cell=advance(cell,x);cell=advance(cell,z);return new Rand(world,cell);
    }
    private static final class Rand {
        final long world;long cell;
        Rand(long world,long cell){this.world=world;this.cell=cell;}
        int next(int bound){int v=(int)((cell>>24)%bound);if(v<0)v+=bound;cell=advance(cell,world);return v;}
        int choose(int a,int b){return next(2)==0?a:b;}
        int mode(int a,int b,int c,int d){
            int[] values={a,b,c,d};int best=0,winner=0,ties=0;
            for(int i=0;i<4;i++){
                boolean seen=false;for(int j=0;j<i;j++)if(values[j]==values[i])seen=true;if(seen)continue;
                int count=0;for(int v:values)if(v==values[i])count++;
                if(count>best){best=count;winner=values[i];ties=1;}else if(count==best)ties++;
            }
            return best>=2&&ties==1?winner:values[next(4)];
        }
    }
    private Layer island(){return(x,z,w,h)->{int[] out=new int[w*h];for(int dz=0;dz<h;dz++)for(int dx=0;dx<w;dx++)out[dx+dz*w]=random(1,x+dx,z+dz).next(10)==0?1:0;if(x>-w&&x<=0&&z>-h&&z<=0)out[-x-z*w]=1;return out;};}
    @FunctionalInterface private interface Mapping {int apply(Rand random,int value);}
    private Layer map(Layer parent,long salt,Mapping mapper){return(x,z,w,h)->{int[] a=parent.grid(x,z,w,h);for(int dz=0;dz<h;dz++)for(int dx=0;dx<w;dx++){int i=dx+dz*w;a[i]=mapper.apply(random(salt,x+dx,z+dz),a[i]);}return a;};}
    @FunctionalInterface private interface Cross {int apply(Rand random,int c,int w,int e,int n,int s);}
    private Layer cross(Layer parent,long salt,Cross operation){return(x,z,w,h)->{
        int stride=w+2;int[] a=parent.grid(x-1,z-1,stride,h+2),out=new int[w*h];
        for(int dz=0;dz<h;dz++)for(int dx=0;dx<w;dx++){int i=dx+1+(dz+1)*stride;out[dx+dz*w]=operation.apply(random(salt,x+dx,z+dz),a[i],a[i-1],a[i+1],a[i-stride],a[i+stride]);}return out;
    };}
    private Layer addLand(Layer parent,long salt){return(x,z,w,h)->{
        int stride=w+2;int[] a=parent.grid(x-1,z-1,stride,h+2),out=new int[w*h];
        for(int dz=0;dz<h;dz++)for(int dx=0;dx<w;dx++){
            int i=dx+1+(dz+1)*stride,c=a[i],nw=a[i-stride-1],ne=a[i-stride+1],sw=a[i+stride-1],se=a[i+stride+1];var rng=random(salt,x+dx,z+dz);
            if(release){
                if(c==0&&(nw!=0||ne!=0||sw!=0||se!=0)){int n=1,chosen=1;for(int v:new int[]{nw,ne,sw,se})if(v!=0&&rng.next(n++)==0)chosen=v;out[dx+dz*w]=rng.next(3)==0?chosen:chosen==12?10:0;}
                else out[dx+dz*w]=c>0&&(nw==0||ne==0||sw==0||se==0)&&rng.next(5)==0?c==12?10:0:c;
                continue;
            }
            out[dx+dz*w]=c==0&&(nw!=0||ne!=0||sw!=0||se!=0)?rng.next(3)/2:c==1&&(nw!=1||ne!=1||sw!=1||se!=1)?1-rng.next(5)/4:c;
        }return out;
    };}
    private Layer smooth(Layer parent,long salt){return cross(parent,salt,(rng,c,w,e,n,s)->w==e&&n==s?rng.choose(w,n):n==s?n:w==e?w:c);}
    private Layer zoom(Layer parent,long salt,boolean fuzzy){return(x,z,w,h)->{
        int px=x>>1,pz=z>>1,pw=(w>>1)+3,ph=(h>>1)+3,stride=pw*2;int[] a=parent.grid(px,pz,pw,ph),expanded=new int[stride*ph*2];
        for(int dz=0;dz<ph-1;dz++)for(int dx=0;dx<pw-1;dx++){
            int nw=a[dx+dz*pw],ne=a[dx+1+dz*pw],sw=a[dx+(dz+1)*pw],se=a[dx+1+(dz+1)*pw],i=dx*2+dz*2*stride;var rng=random(salt,(px+dx)<<1,(pz+dz)<<1);
            expanded[i]=nw;expanded[i+stride]=rng.choose(nw,sw);expanded[i+1]=rng.choose(nw,ne);expanded[i+stride+1]=fuzzy?new int[]{nw,ne,sw,se}[rng.next(4)]:rng.mode(nw,ne,sw,se);
        }return crop(expanded,stride,x&1,z&1,w,h);
    };}
    private Layer voronoi(Layer parent,long salt){return(x,z,w,h)->{
        x-=2;z-=2;int px=x>>2,pz=z>>2,pw=(w>>2)+3,ph=(h>>2)+3,stride=pw*4;int[] a=parent.grid(px,pz,pw,ph),out=new int[stride*ph*4];
        for(int dz=0;dz<ph-1;dz++)for(int dx=0;dx<pw-1;dx++){
            double[] vx=new double[4],vz=new double[4];int[] values={a[dx+dz*pw],a[dx+1+dz*pw],a[dx+(dz+1)*pw],a[dx+1+(dz+1)*pw]};
            for(int corner=0;corner<4;corner++){int cx=corner&1,cz=corner>>1;var rng=random(salt,(px+dx+cx)<<2,(pz+dz+cz)<<2);vx[corner]=(rng.next(1024)/1024.0-.5)*3.6+cx*4;vz[corner]=(rng.next(1024)/1024.0-.5)*3.6+cz*4;}
            for(int bz=0;bz<4;bz++)for(int bx=0;bx<4;bx++){
                double[] d=new double[4];for(int j=0;j<4;j++)d[j]=(bz-vz[j])*(bz-vz[j])+(bx-vx[j])*(bx-vx[j]);
                int nearest=d[0]<d[1]&&d[0]<d[2]&&d[0]<d[3]?0:d[1]<d[0]&&d[1]<d[2]&&d[1]<d[3]?1:d[2]<d[0]&&d[2]<d[1]&&d[2]<d[3]?2:3;
                out[(dz*4+bz)*stride+dx*4+bx]=values[nearest];
            }
        }return crop(out,stride,x&3,z&3,w,h);
    };}
    private static int[] crop(int[] data,int stride,int x,int z,int width,int depth){int[] out=new int[width*depth];for(int row=0;row<depth;row++)System.arraycopy(data,(row+z)*stride+x,out,row*width,width);return out;}
}
