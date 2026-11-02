package br.com.mura.betaminecraftmod.world;

import java.util.*;

/** Registry-free candidate for the Alpha large oak; placement is committed separately. */
public final class HistoricalLargeOak {
    public record Point(int x,int y,int z) {
        int axis(int axis){return axis==0?x:axis==1?y:z;}
    }
    public enum Cell { AIR, LEAVES, GRASS, DIRT, SOLID }
    public enum Part { LEAVES, LOG }
    @FunctionalInterface public interface Ground { Cell at(Point point); }
    private record Cluster(Point point,int branchY) { }
    private int height;

    public Map<Point,Part> plan(Ground ground,Random source,int x,int y,int z) {
        var random=new Random(source.nextLong());
        if(height==0)height=5+random.nextInt(12);
        var base=new Point(x,y,z);
        var soil=ground.at(new Point(x,y-1,z));
        if(y<1||y+height>128||(soil!=Cell.GRASS&&soil!=Cell.DIRT))return Map.of();
        int obstruction=obstruction(ground,base,new Point(x,y+height-1,z));
        if(obstruction>=0){if(obstruction<6)return Map.of();height=obstruction;}
        int trunk=Math.min((int)(height*.618),height-1),crownTop=y+height-5;
        int density=Math.max(1,(int)(1.382+Math.pow(height/13.0,2)));
        var clusters=new ArrayList<Cluster>();clusters.add(new Cluster(new Point(x,crownTop,z),y+trunk));
        for(int relative=height-5,layer=crownTop-1;relative>=0;relative--,layer--) {
            float radius=spread(relative,height);if(radius<0)continue;
            for(int attempt=0;attempt<density;attempt++) {
                double distance=radius*(random.nextFloat()+.328),angle=random.nextFloat()*2*3.14159;
                var point=new Point((int)(x+distance*Math.sin(angle)+.5),layer,(int)(z+distance*Math.cos(angle)+.5));
                if(obstruction(ground,point,new Point(point.x,layer+5,point.z))!=-1)continue;
                int branchY=Math.min(y+trunk,(int)(layer-Math.hypot(x-point.x,z-point.z)*.381));
                if(obstruction(ground,new Point(x,branchY,z),point)==-1)clusters.add(new Cluster(point,branchY));
            }
        }
        var result=new LinkedHashMap<Point,Part>();
        for(var cluster:clusters)for(int layer=0;layer<5;layer++) {
            int radius=layer==0||layer==4?2:3,extent=(int)(radius+.618);
            for(int dx=-extent;dx<=extent;dx++)for(int dz=-extent;dz<=extent;dz++) {
                if(Math.hypot(Math.abs(dx)+.5,Math.abs(dz)+.5)>radius)continue;
                var point=new Point(cluster.point.x+dx,cluster.point.y+layer,cluster.point.z+dz);
                var cell=ground.at(point);
                if(cell==Cell.AIR||cell==Cell.LEAVES)result.put(point,Part.LEAVES);
            }
        }
        logs(result,base,new Point(x,y+trunk,z));
        for(var cluster:clusters)if(cluster.branchY-y>=height*.2)logs(result,new Point(x,cluster.branchY,z),cluster.point);
        return Collections.unmodifiableMap(result);
    }
    private static float spread(int layer,int height) {
        if(layer<height*.3)return -1;
        float half=height/2f,offset=half-layer;
        return (offset==0?half:Math.abs(offset)>=half?0:(float)Math.sqrt(Math.pow(Math.abs(half),2)-Math.pow(Math.abs(offset),2)))*.5f;
    }
    private static int dominant(Point a,Point b) {
        int axis=0;for(int i=1;i<3;i++)if(Math.abs(b.axis(i)-a.axis(i))>Math.abs(b.axis(axis)-a.axis(axis)))axis=i;return axis;
    }
    private static int obstruction(Ground ground,Point a,Point b) {
        int axis=dominant(a,b),delta=b.axis(axis)-a.axis(axis);if(delta==0)return -1;
        int step=Integer.signum(delta);
        for(int distance=0;distance!=delta+step;distance+=step) {
            var point=pointOnLine(a,b,axis,delta,distance,false);var cell=ground.at(point);
            if(cell!=Cell.AIR&&cell!=Cell.LEAVES)return Math.abs(distance);
        }
        return -1;
    }
    private static Point pointOnLine(Point a,Point b,int axis,int delta,int distance,boolean rounded) {
        int[] p=new int[3];
        for(int i=0;i<3;i++) {
            double coordinate=a.axis(i)+(i==axis?distance:distance*((double)(b.axis(i)-a.axis(i))/delta));
            p[i]=rounded?(int)Math.floor(coordinate+.5):(int)coordinate;
        }
        return new Point(p[0],p[1],p[2]);
    }
    private static void logs(Map<Point,Part> plan,Point a,Point b) {
        int axis=dominant(a,b),delta=b.axis(axis)-a.axis(axis);if(delta==0)return;
        int step=Integer.signum(delta);for(int d=0;d!=delta+step;d+=step)plan.put(pointOnLine(a,b,axis,delta,d,true),Part.LOG);
    }
}
