package br.com.mura.betaminecraftmod.world;

import java.nio.file.*;
import java.util.*;
import static br.com.mura.betaminecraftmod.world.HistoricalNetherPortalLayout.*;

/** Adversarial fixed-frame fixtures also supplied to the unchanged reference methods. */
public final class HistoricalNetherPortalChecks {
    public static final class Fixture implements View {
        public final Map<Pos,Integer> blocks=new HashMap<>();
        public int limit=1000;
        public int block(int x,int y,int z){if(!allowed(x,z))throw new AssertionError("Read outside gate");return blocks.getOrDefault(new Pos(x,y,z),0);}
        public boolean solid(int x,int y,int z){return block(x,y,z)!=0;}
        public boolean allowed(int x,int z){return Math.abs(x)<limit&&Math.abs(z)<limit;}
    }
    public static Fixture fixture(int mode,boolean xAxis,boolean filled){
        var view=new Fixture();int dx=xAxis?1:0,dz=1-dx,width=mode==1?3:2,height=mode==2?4:3;
        for(int w=-1;w<=width;w++)for(int h=-1;h<=height;h++){
            boolean side=w==-1||w==width,cap=h==-1||h==height;
            if(side&&cap)continue;
            if(side||cap)view.blocks.put(new Pos(w*dx,70+h,w*dz),49);
            else if(filled)view.blocks.put(new Pos(w*dx,70+h,w*dz),90);
        }
        if(mode==3)view.blocks.remove(new Pos(-dx,71,-dz));
        if(mode==4)view.blocks.put(new Pos(dx,71,dz),1);
        if(mode==5&&!filled)view.blocks.put(new Pos(0,70,0),51);
        if(mode==6)view.blocks.put(new Pos(xAxis?0:1,70,xAxis?1:0),49);
        if(mode==7)view.blocks.remove(new Pos(0,69,0));
        if(mode==8)view.blocks.put(new Pos(xAxis?0:1,70,xAxis?1:0),90);
        if(mode==9)view.blocks.put(new Pos(-dx,69,-dz),1); // Corners are deliberately ignored.
        return view;
    }
    public static String result(int mode,boolean axis,boolean filled){
        var view=fixture(mode,axis,filled);var pos=new Pos(0,70,0);
        return filled?Boolean.toString(survives(view,pos)):Boolean.toString(ignite(view,pos)!=null);
    }
    static void run()throws Exception {
        int count=0;
        for(String line:Files.readAllLines(Path.of("docs/reference/nether_portal_frames.tsv"))){if(line.startsWith("#")||line.isBlank())continue;var f=line.split(" ");if(!result(Integer.parseInt(f[1]),Boolean.parseBoolean(f[2]),Boolean.parseBoolean(f[3])).equals(f[4]))throw new AssertionError("Original portal differs: "+line);count++;}
        if(count!=160)throw new AssertionError("Incomplete original portal fixtures");
        var locked=fixture(0,true,false);locked.limit=1;if(ignite(locked,new Pos(0,70,0))!=null)throw new AssertionError("Portal crosses locked area");
        var portals=new Fixture();portals.blocks.put(new Pos(-2,70,0),90);portals.blocks.put(new Pos(2,70,0),90);
        var landing=find(portals,List.of(new Pos(2,70,0),new Pos(-2,70,0)),.5,70.5,.5);
        if(landing.x()!=-1.5)throw new AssertionError("Search tie order changed");
        var empty=new Fixture();var created=create(empty,0,1,0,new Random(0));if(created==null||!created.fallback()||created.frame().origin().y()!=70||created.writes().size()!=184)throw new AssertionError("Fallback floor/clearance");
        empty.limit=1;if(create(empty,0,70,0,new Random(0))!=null)throw new AssertionError("Creation writes outside gate");
        System.out.println("HISTORICAL_NETHER_PORTAL_FRAMES SUCCESS: 160 original ignition/integrity cases; locked reads/writes and candidate search checks");
    }
}
