package br.com.mura.betaminecraftmod.progression;

import java.util.List;

/** Union of the legacy half slab and riser; metadata 0/1/2/3 faces E/W/S/N. */
public final class HistoricalStairGeometry {
    public record Box(double x0,double y0,double z0,double x1,double y1,double z1) { }
    private HistoricalStairGeometry() { }
    public static List<Box> collision(int metadata) {
        boolean top=(metadata&4)!=0;double y0=top?0:.5,y1=top?.5:1;
        var slab=new Box(0,top?.5:0,0,1,top?1:.5,1);
        var step=switch(metadata&3){case 0->new Box(.5,y0,0,1,y1,1);case 1->new Box(0,y0,0,.5,y1,1);case 2->new Box(0,y0,.5,1,y1,1);default->new Box(0,y0,0,1,y1,.5);};
        return List.of(slab,step);
    }
    public static int occupancy(int metadata) {
        int mask=0;
        for(int x=0;x<2;x++)for(int y=0;y<2;y++)for(int z=0;z<2;z++) {
            double px=.25+x*.5,py=.25+y*.5,pz=.25+z*.5;
            for(var box:collision(metadata))if(px>=box.x0&&px<box.x1&&py>=box.y0&&py<box.y1&&pz>=box.z0&&pz<box.z1){mask|=1<<(x*4+y*2+z);break;}
        }
        return mask;
    }
}
