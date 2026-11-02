package br.com.mura.betaminecraftmod.world;

import java.util.Random;

/** Legacy Perlin octaves. Grid sampling retains the Infdev vertical-gradient cache. */
final class InfdevNoise {
    private final Perlin[] octaves;

    InfdevNoise(Random random, int count) {
        octaves = new Perlin[count];
        for (int i = 0; i < count; i++) octaves[i] = new Perlin(random);
    }

    double sample(double x, double y, double z) {
        double sum = 0, scale = 1;
        for (Perlin octave : octaves) {
            sum += octave.sample(x * scale, y * scale, z * scale) / scale;
            scale /= 2;
        }
        return sum;
    }

    double[] grid(int x, int z, int nx, int ny, int nz, double sx, double sy, double sz) {
        return grid3d(x, 0, z, nx, ny, nz, sx, sy, sz);
    }
    double[] grid3d(double x, double y, double z, int nx, int ny, int nz, double sx, double sy, double sz) {
        double[] result = new double[nx * ny * nz];
        double scale = 1;
        for (Perlin octave : octaves) {
            octave.addGrid(result, x, y, z, nx, ny, nz, sx * scale, sy * scale, sz * scale, scale);
            scale /= 2;
        }
        return result;
    }

    /** Later Perlin implementation has a separate Y-size-one path, ignoring Y and its offset. */
    double[] flat(double x,double z,int nx,int nz,double sx,double sz) {
        double[] out=new double[nx*nz];double scale=1;
        for(var octave:octaves){octave.addFlat(out,x,z,nx,nz,sx*scale,sz*scale,scale);scale/=2;}
        return out;
    }
    /** Beta 1.8+ octave origin wrapping; coordinates are scaled before adding sample offsets. */
    double[] wrapped(int x,int y,int z,int nx,int ny,int nz,double sx,double sy,double sz){
        double[] out=new double[nx*ny*nz];double scale=1;
        for(var octave:octaves){
            double px=x*scale*sx,pz=z*scale*sz;long ix=(long)Math.floor(px),iz=(long)Math.floor(pz);
            px=px-ix+ix%16777216L;pz=pz-iz+iz%16777216L;
            if(ny==1)octave.addFlat(out,px,pz,nx,nz,sx*scale,sz*scale,scale,true);
            else octave.addGrid(out,px,y*scale*sy,pz,nx,ny,nz,sx*scale,sy*scale,sz*scale,scale,true);
            scale/=2;
        }
        return out;
    }
    static double lerp(double t, double a, double b) { return a + t * (b - a); }
    static int floor(double v) { int i = (int) v; return v < i ? i - 1 : i; }
    private static double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private static final class Perlin {
        final int[] p = new int[512];
        final double ox, oy, oz;
        Perlin(Random random) {
            ox = random.nextDouble() * 256;
            oy = random.nextDouble() * 256;
            oz = random.nextDouble() * 256;
            for (int i = 0; i < 256; i++) p[i] = i;
            for (int i = 0; i < 256; i++) {
                int j = random.nextInt(256 - i) + i;
                int swap = p[i]; p[i] = p[j]; p[j] = swap;
                p[i + 256] = p[i];
            }
        }
        double[] gradients(int x, int y, int z, double fx, double fy, double fz) {
            int a = p[x] + y, b = p[x + 1] + y;
            int aa = p[a] + z, ab = p[a + 1] + z, ba = p[b] + z, bb = p[b + 1] + z;
            double u = fade(fx);
            return new double[]{
                lerp(u, grad(p[aa], fx, fy, fz), grad(p[ba], fx - 1, fy, fz)),
                lerp(u, grad(p[ab], fx, fy - 1, fz), grad(p[bb], fx - 1, fy - 1, fz)),
                lerp(u, grad(p[aa + 1], fx, fy, fz - 1), grad(p[ba + 1], fx - 1, fy, fz - 1)),
                lerp(u, grad(p[ab + 1], fx, fy - 1, fz - 1), grad(p[bb + 1], fx - 1, fy - 1, fz - 1))};
        }
        double sample(double x, double y, double z) {
            x += ox; y += oy; z += oz;
            int ix = floor(x), iy = floor(y), iz = floor(z);
            x -= ix; y -= iy; z -= iz;
            double[] g = gradients(ix & 255, iy & 255, iz & 255, x, y, z);
            return lerp(fade(z), lerp(fade(y), g[0], g[1]), lerp(fade(y), g[2], g[3]));
        }
        void addFlat(double[] out,double x,double z,int nx,int nz,double sx,double sz,double amplitude){
            addFlat(out,x,z,nx,nz,sx,sz,amplitude,false);
        }
        void addFlat(double[] out,double x,double z,int nx,int nz,double sx,double sz,double amplitude,boolean prescaled){
            int index=0;
            for(int dx=0;dx<nx;dx++){
                double px=(prescaled?x+dx*sx:(x+dx)*sx)+ox;int ix=floor(px);px-=ix;ix&=255;
                for(int dz=0;dz<nz;dz++){
                    double pz=(prescaled?z+dz*sz:(z+dz)*sz)+oz;int iz=floor(pz);pz-=iz;iz&=255;
                    int a=p[p[ix]]+iz,b=p[p[ix+1]]+iz;
                    double left=lerp(fade(px),grad(p[a],px,0,pz),grad(p[b],px-1,0,pz));
                    double right=lerp(fade(px),grad(p[a+1],px,0,pz-1),grad(p[b+1],px-1,0,pz-1));
                    out[index++]+=lerp(fade(pz),left,right)/amplitude;
                }
            }
        }
        void addGrid(double[] out, double x, double y, double z, int nx, int ny, int nz, double sx, double sy, double sz, double amplitude) {
            addGrid(out,x,y,z,nx,ny,nz,sx,sy,sz,amplitude,false);
        }
        void addGrid(double[] out,double x,double y,double z,int nx,int ny,int nz,double sx,double sy,double sz,double amplitude,boolean prescaled){
            int index = 0, lastY = -1;
            double[] g = null;
            for (int dx = 0; dx < nx; dx++) {
                double px = (prescaled?x+dx*sx:(x+dx)*sx)+ox;
                int ix = floor(px); px -= ix;
                for (int dz = 0; dz < nz; dz++) {
                    double pz = (prescaled?z+dz*sz:(z+dz)*sz)+oz;
                    int iz = floor(pz); pz -= iz;
                    for (int dy = 0; dy < ny; dy++) {
                        double py = (prescaled?y+dy*sy:(y+dy)*sy)+oy;
                        int iy = floor(py); py -= iy;
                        if (dy == 0 || (iy & 255) != lastY) {
                            lastY = iy & 255;
                            g = gradients(ix & 255, lastY, iz & 255, px, py, pz);
                        }
                        out[index++] += lerp(fade(pz), lerp(fade(py), g[0], g[1]), lerp(fade(py), g[2], g[3])) / amplitude;
                    }
                }
            }
        }
    }
}
