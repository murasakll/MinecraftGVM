package br.com.mura.betaminecraftmod.world;

import java.util.*;

/** Independent fixed-size Alpha/Beta/1.0 portal geometry and search. World access must enforce its gate. */
public final class HistoricalNetherPortalLayout {
    public interface View {
        int block(int x,int y,int z);
        boolean solid(int x,int y,int z);
        boolean allowed(int x,int z);
    }
    public record Pos(int x,int y,int z) { }
    public record Frame(Pos origin,int dx,int dz,int portalBlocks) { }
    public record Landing(double x,double y,double z) { }
    public record Write(Pos pos,int block,boolean notifyOnly,boolean immediate) { }
    public record Creation(Frame frame,boolean fallback,List<Write> writes) { }
    private HistoricalNetherPortalLayout() { }
    private static int block(View view,int x,int y,int z){return y<0||y>=128?0:!view.allowed(x,z)?-1:view.block(x,y,z);}
    private static boolean allowed(View view,int x,int y,int z){return y>=0&&y<128&&view.allowed(x,z);}
    public static Frame ignite(View view,Pos fire) {
        int x=fire.x,y=fire.y,z=fire.z;
        boolean acrossX=block(view,x-1,y,z)==49||block(view,x+1,y,z)==49,acrossZ=block(view,x,y,z-1)==49||block(view,x,y,z+1)==49;
        if(acrossX==acrossZ)return null;int dx=acrossX?1:0,dz=1-dx;
        if(block(view,x-dx,y,z-dz)==0){x-=dx;z-=dz;}
        return frame(view,new Pos(x,y,z),dx,dz,false);
    }
    public static Frame complete(View view,Pos cell,boolean xAxis) {
        int dx=xAxis?1:0,dz=1-dx,x=cell.x,y=cell.y,z=cell.z;
        for(int i=0;i<3&&block(view,x,y-1,z)==90;i++)y--;
        for(int i=0;i<2&&block(view,x-dx,y,z-dz)==90;i++){x-=dx;z-=dz;}
        var frame=frame(view,new Pos(x,y,z),dx,dz,true);return frame!=null&&frame.portalBlocks==6?frame:null;
    }
    public static boolean survives(View view,Pos cell) {
        int x=cell.x,y=cell.y,z=cell.z;
        boolean acrossX=block(view,x-1,y,z)==90||block(view,x+1,y,z)==90;
        int dx=acrossX?1:0,dz=1-dx,bottom=y;
        while(block(view,x,bottom-1,z)==90)bottom--;
        if(block(view,x,bottom-1,z)!=49)return false;
        int height=1;while(height<4&&block(view,x,bottom+height,z)==90)height++;
        if(height!=3||block(view,x,bottom+height,z)!=49)return false;
        boolean acrossZ=block(view,x,y,z-1)==90||block(view,x,y,z+1)==90;if(acrossX&&acrossZ)return false;
        return block(view,x+dx,y,z+dz)==49&&block(view,x-dx,y,z-dz)==90||block(view,x-dx,y,z-dz)==49&&block(view,x+dx,y,z+dz)==90;
    }
    private static Frame frame(View view,Pos origin,int dx,int dz,boolean portal) {
        int count=0;
        for(int width=-1;width<=2;width++)for(int height=-1;height<=3;height++) {
            boolean side=width==-1||width==2,cap=height==-1||height==3;if(side&&cap)continue;
            int x=origin.x+width*dx,y=origin.y+height,z=origin.z+width*dz;if(!allowed(view,x,y,z))return null;
            int id=block(view,x,y,z);
            if(side||cap){if(id!=49)return null;}
            else if(portal){if(id!=90)return null;count++;}else if(id!=0&&id!=51)return null;
        }
        return new Frame(origin,dx,dz,count);
    }
    /** Sparse palette scan has the original X/Z/descending-Y tie order, without reading unrelated block cells. */
    public static Landing find(View view,Collection<Pos> cells,double x,double y,double z) {
        int cx=(int)Math.floor(x),cz=(int)Math.floor(z);var candidates=new ArrayList<>(cells);
        candidates.sort(Comparator.comparingInt(Pos::x).thenComparingInt(Pos::z).thenComparing(Comparator.comparingInt(Pos::y).reversed()));
        double distance=-1;Pos best=null;var visited=new HashSet<Pos>();
        for(var cell:candidates) {
            if(cell.x<cx-128||cell.x>cx+128||cell.z<cz-128||cell.z>cz+128||cell.y<0||cell.y>=128||!view.allowed(cell.x,cell.z)||block(view,cell.x,cell.y,cell.z)!=90)continue;
            int bottom=cell.y;while(block(view,cell.x,bottom-1,cell.z)==90)bottom--;
            var pos=new Pos(cell.x,bottom,cell.z);if(!visited.add(pos))continue;
            double dx=pos.x+.5-x,dy=pos.y+.5-y,dz=pos.z+.5-z,d=dx*dx+dy*dy+dz*dz;
            if(distance<0||d<distance){distance=d;best=pos;}
        }
        if(best==null)return null;
        double px=best.x+.5,pz=best.z+.5;
        if(block(view,best.x-1,best.y,best.z)==90)px-=.5;if(block(view,best.x+1,best.y,best.z)==90)px+=.5;
        if(block(view,best.x,best.y,best.z-1)==90)pz-=.5;if(block(view,best.x,best.y,best.z+1)==90)pz+=.5;
        return new Landing(px,best.y+.5,pz);
    }
    public static Creation create(View view,double x,double y,double z,Random random) {
        int cx=(int)Math.floor(x),cy=(int)Math.floor(y),cz=(int)Math.floor(z),start=random.nextInt(4);Site best=null;
        for(int pass=0;pass<2&&best==null;pass++) {
            for(int px=cx-16;px<=cx+16;px++)for(int pz=cz-16;pz<=cz+16;pz++) {
                if(!view.allowed(px,pz))continue;
                for(int py=127;py>=0;py--) {
                    if(block(view,px,py,pz)!=0)continue;while(py>0&&block(view,px,py-1,pz)==0)py--;
                    for(int choice=start;choice<start+(pass==0?4:2);choice++) {
                        int orientation=choice%(pass==0?4:2),dx=orientation%2,dz=1-dx;if(orientation>=2){dx=-dx;dz=-dz;}
                        if(!host(view,px,py,pz,dx,dz,pass==0?3:1))break; // Original failed orientation skips the rest of this Y candidate.
                        double ox=px+.5-x,oy=py+.5-y,oz=pz+.5-z,d=ox*ox+oy*oy+oz*oz;
                        if(best==null||d<best.distance)best=new Site(px,py,pz,orientation,d);
                    }
                }
            }
        }
        boolean fallback=best==null;if(fallback)best=new Site(cx,Math.clamp(cy,70,118),cz,0,-1);
        int dx=best.orientation%2,dz=1-dx;if(best.orientation>=2){dx=-dx;dz=-dz;}
        var writes=new ArrayList<Write>();
        if(fallback)for(int depth=-1;depth<=1;depth++)for(int width=1;width<3;width++)for(int height=-1;height<3;height++) {
            var pos=new Pos(best.x+(width-1)*dx+depth*dz,best.y+height,best.z+(width-1)*dz-depth*dx);
            writes.add(new Write(pos,height<0?49:0,false,true));
        }
        for(int repeat=0;repeat<4;repeat++) {
            for(int width=0;width<4;width++)for(int height=-1;height<4;height++) {
                var pos=new Pos(best.x+(width-1)*dx,best.y+height,best.z+(width-1)*dz);
                writes.add(new Write(pos,width==0||width==3||height==-1||height==3?49:90,false,false));
            }
            for(int width=0;width<4;width++)for(int height=-1;height<4;height++) {
                var pos=new Pos(best.x+(width-1)*dx,best.y+height,best.z+(width-1)*dz);
                writes.add(new Write(pos,width==0||width==3||height==-1||height==3?49:90,true,false));
            }
        }
        for(var write:writes)if(!allowed(view,write.pos.x,write.pos.y,write.pos.z))return null;
        return new Creation(new Frame(new Pos(best.x,best.y,best.z),dx,dz,6),fallback,List.copyOf(writes));
    }
    private record Site(int x,int y,int z,int orientation,double distance) { }
    private static boolean host(View view,int x,int y,int z,int dx,int dz,int depth) {
        // Fit the complete old frame into the campaign's 0..127 domain rather than writing above its ceiling.
        if(y<1||y>124)return false;
        for(int d=0;d<depth;d++)for(int w=0;w<4;w++)for(int h=-1;h<4;h++) {
            int px=x+(w-1)*dx+d*dz,py=y+h,pz=z+(w-1)*dz-d*dx;
            if(!allowed(view,px,py,pz)||h<0&&!view.solid(px,py,pz)||h>=0&&block(view,px,py,pz)!=0)return false;
        }
        return true;
    }
}
