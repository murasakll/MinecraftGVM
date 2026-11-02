package br.com.mura.betaminecraftmod.world;

import java.util.Random;

/** Independent historical 2D simplex sampler; seed consumption and octave scaling match Alpha climate. */
final class LegacySimplex {
    private static final int[][] GRAD={{1,1},{-1,1},{1,-1},{-1,-1},{1,0},{-1,0},{1,0},{-1,0},{0,1},{0,-1},{0,1},{0,-1}};
    private static final double F=(Math.sqrt(3)-1)*.5,G=(3-Math.sqrt(3))/6;
    private final int[] permutation=new int[512];
    private final double offsetX,offsetZ;
    LegacySimplex(Random random) {
        offsetX=random.nextDouble()*256;offsetZ=random.nextDouble()*256;random.nextDouble();
        for(int i=0;i<256;i++)permutation[i]=i;
        for(int i=0;i<256;i++){int j=i+random.nextInt(256-i),v=permutation[i];permutation[i]=permutation[j];permutation[j]=v;permutation[i+256]=permutation[i];}
    }
    private static int cell(double v){return v>0?(int)v:(int)v-1;}
    private static double contribution(int gradient,double x,double z){
        double t=.5-x*x-z*z;if(t<0)return 0;t*=t;return t*t*(GRAD[gradient][0]*x+GRAD[gradient][1]*z);
    }
    double sample(double x,double z){
        x+=offsetX;z+=offsetZ;double skew=(x+z)*F;int i=cell(x+skew),j=cell(z+skew);
        double unskew=(i+j)*G,dx=x-(i-unskew),dz=z-(j-unskew);int a=dx>dz?1:0,b=1-a;
        int ix=i&255,iz=j&255;
        return 70*(contribution(permutation[ix+permutation[iz]]%12,dx,dz)
            +contribution(permutation[ix+a+permutation[iz+b]]%12,dx-a+G,dz-b+G)
            +contribution(permutation[ix+1+permutation[iz+1]]%12,dx-1+2*G,dz-1+2*G));
    }
    static final class Octaves {
        private final LegacySimplex[] layers;
        Octaves(long seed,int count){var random=new Random(seed);layers=new LegacySimplex[count];for(int i=0;i<count;i++)layers[i]=new LegacySimplex(random);}
        double[] grid(int x,int z,int width,int depth,double scale,double persistence){
            var out=new double[width*depth];double frequency=1,amplitude=1;
            for(var layer:layers){
                double step=scale/1.5*frequency;
                for(int dx=0;dx<width;dx++)for(int dz=0;dz<depth;dz++)out[dx*depth+dz]+=layer.sample((x+dx)*step,(z+dz)*step)*(.55/amplitude);
                frequency*=persistence;amplitude*=.5;
            }
            return out;
        }
    }
}
