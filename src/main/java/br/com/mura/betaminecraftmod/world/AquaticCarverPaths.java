package br.com.mura.betaminecraftmod.world;

import java.util.Random;
import static br.com.mura.betaminecraftmod.world.AdventureCaves.sin;
import static br.com.mura.betaminecraftmod.world.AdventureCaves.cos;

/** Independent trajectory engine for the pre-1.17 configured carver family. */
final class AquaticCarverPaths {
    @FunctionalInterface interface Sink {
        void ellipsoid(long seed,double x,double y,double z,double radius,double vertical,float[] canyonWeights);
    }
    private final int targetX,targetZ;
    private final Sink sink;
    AquaticCarverPaths(int targetX,int targetZ,Sink sink){this.targetX=targetX;this.targetZ=targetZ;this.sink=sink;}

    void caves(Random r,int sourceX,int sourceZ){
        int systems=r.nextInt(r.nextInt(r.nextInt(15)+1)+1);
        for(int i=0;i<systems;i++){
            double x=sourceX*16+r.nextInt(16),y=r.nextInt(r.nextInt(120)+8),z=sourceZ*16+r.nextInt(16);
            int paths=1;
            if(r.nextInt(4)==0){
                float size=1+r.nextFloat()*6;
                long roomSeed=r.nextLong();double radius=1.5+sin((float)(Math.PI/2))*size;
                sink.ellipsoid(roomSeed,x+1,y,z,radius,radius*.5,null);
                paths+=r.nextInt(4);
            }
            for(int j=0;j<paths;j++){
                float yaw=r.nextFloat()*(float)(Math.PI*2),pitch=(r.nextFloat()-.5f)/4;
                float width=r.nextFloat()*2+r.nextFloat();
                if(r.nextInt(10)==0)width*=r.nextFloat()*r.nextFloat()*3+1;
                int length=112-r.nextInt(28);
                tunnel(r.nextLong(),x,y,z,width,yaw,pitch,0,length,1);
            }
        }
    }
    private void tunnel(long seed,double x,double y,double z,float width,float yaw,float pitch,int start,int length,double scale){
        var r=new Random(seed);int branch=r.nextInt(length/2)+length/4;boolean gentle=r.nextInt(6)==0;
        float yawDrift=0,pitchDrift=0;
        for(int step=start;step<length;step++){
            double radius=1.5+sin((float)Math.PI*step/length)*width;
            float horizontal=cos(pitch);x+=cos(yaw)*horizontal;y+=sin(pitch);z+=sin(yaw)*horizontal;
            pitch*=gentle?.92f:.7f;pitch+=pitchDrift*.1f;yaw+=yawDrift*.1f;
            pitchDrift*=.9f;yawDrift*=.75f;
            pitchDrift+=(r.nextFloat()-r.nextFloat())*r.nextFloat()*2;
            yawDrift+=(r.nextFloat()-r.nextFloat())*r.nextFloat()*4;
            if(step==branch&&width>1){
                tunnel(r.nextLong(),x,y,z,r.nextFloat()*.5f+.5f,yaw-(float)(Math.PI/2),pitch/3,step,length,1);
                tunnel(r.nextLong(),x,y,z,r.nextFloat()*.5f+.5f,yaw+(float)(Math.PI/2),pitch/3,step,length,1);
                return;
            }
            if(r.nextInt(4)==0)continue;
            if(!reachable(x,z,step,length,width))return;
            sink.ellipsoid(seed,x,y,z,radius,radius*scale,null);
        }
    }
    void canyon(Random r,int sourceX,int sourceZ){
        double x=sourceX*16+r.nextInt(16),y=r.nextInt(r.nextInt(40)+8)+20,z=sourceZ*16+r.nextInt(16);
        float yaw=r.nextFloat()*(float)(Math.PI*2),pitch=(r.nextFloat()-.5f)*2/8;
        float width=(r.nextFloat()*2+r.nextFloat())*2;
        int length=112-r.nextInt(28);long seed=r.nextLong();var path=new Random(seed);
        float[] weights=new float[256];float weight=1;
        for(int h=0;h<256;h++){
            if(h==0||path.nextInt(3)==0)weight=1+path.nextFloat()*path.nextFloat();
            weights[h]=weight*weight;
        }
        float yawDrift=0,pitchDrift=0;
        for(int step=0;step<length;step++){
            double radius=1.5+sin(step*(float)Math.PI/length)*width,vertical=radius*3;
            radius*=path.nextFloat()*.25+.75;vertical*=path.nextFloat()*.25+.75;
            float horizontal=cos(pitch);x+=cos(yaw)*horizontal;y+=sin(pitch);z+=sin(yaw)*horizontal;
            pitch*=.7f;pitch+=pitchDrift*.05f;yaw+=yawDrift*.05f;
            pitchDrift*=.8f;yawDrift*=.5f;
            pitchDrift+=(path.nextFloat()-path.nextFloat())*path.nextFloat()*2;
            yawDrift+=(path.nextFloat()-path.nextFloat())*path.nextFloat()*4;
            if(path.nextInt(4)==0)continue;
            if(!reachable(x,z,step,length,width))return;
            sink.ellipsoid(seed,x,y,z,radius,vertical,weights);
        }
    }
    private boolean reachable(double x,double z,int step,int length,float width){
        double dx=x-(targetX*16+8),dz=z-(targetZ*16+8),remaining=length-step,reach=width+2f+16f;
        return dx*dx+dz*dz-remaining*remaining<=reach*reach;
    }
}
