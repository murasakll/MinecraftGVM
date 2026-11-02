package br.com.mura.betaminecraftmod.world;

import java.nio.file.*;
import java.util.*;
import static br.com.mura.betaminecraftmod.world.HistoricalNetherPortalLayout.*;

public final class HistoricalPortalDestinationChecks {
    public static final int SIDE=289,OFFSET=144;
    public static final class Fixture implements View {
        public final byte[] data=new byte[SIDE*SIDE*128];
        public int get(int x,int y,int z){return y<0||y>=128||x< -OFFSET||x>OFFSET||z< -OFFSET||z>OFFSET?0:data[((x+OFFSET)*SIDE+z+OFFSET)*128+y]&255;}
        public void put(int x,int y,int z,int id){data[((x+OFFSET)*SIDE+z+OFFSET)*128+y]=(byte)id;}
        public int block(int x,int y,int z){return get(x,y,z);}
        public boolean solid(int x,int y,int z){return get(x,y,z)!=0;}
        public boolean allowed(int x,int z){return Math.abs(x)<=OFFSET&&Math.abs(z)<=OFFSET;}
    }
    public static Fixture creation(int mode){
        var f=new Fixture();
        for(int x=-24;x<=24;x++)for(int z=-24;z<=24;z++)for(int y=0;y<128;y++){
            boolean solid=switch(mode){case 0->true;case 1->y<=63;case 2->x!=0||y<=63;case 3->y<=30||y>=81;case 4->y<=63||x==1&&z>=-2&&z<=2&&y<72;default->y<=48+(Math.floorMod(x*7+z*11,5));};
            if(solid)f.put(x,y,z,1);
        }
        return f;
    }
    public static Fixture lookup(int mode){
        var f=new Fixture();
        switch(mode){
            case 0->{for(int x:new int[]{-2,2})for(int y=70;y<73;y++)f.put(x,y,0,90);}
            case 1->{for(int x=127;x<=129;x++)for(int y=70;y<73;y++)f.put(x,y,-1,90);}
            case 2->{for(int y:new int[]{40,41,42,90,91,92})for(int z=0;z<2;z++)f.put(-2,y,z,90);}
            case 3->{f.put(-129,75,-1,90);f.put(128,75,-1,90);f.put(-1,75,-1,90);f.put(-2,75,-1,90);}
        }
        return f;
    }
    public static List<Pos> cells(Fixture f){var result=new ArrayList<Pos>();for(int x=-OFFSET;x<=OFFSET;x++)for(int z=-OFFSET;z<=OFFSET;z++)for(int y=0;y<128;y++)if(f.get(x,y,z)==90)result.add(new Pos(x,y,z));return result;}
    public static double[] incoming(int mode){return mode==3?new double[]{-0.9,75.25,-0.1}:new double[]{.5,70.5,.5};}
    public static String writes(Creation plan){var text=new StringBuilder();for(var w:plan.writes()){var p=w.pos();text.append(w.notifyOnly()?"N":w.immediate()?"I":"B").append('/').append(p.x()).append(',').append(p.y()).append(',').append(p.z()).append('=').append(w.block()).append('\n');}return text.toString();}
    public static String hash(String text)throws Exception{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}
    static void run()throws Exception {
        int creations=0,lookups=0;
        for(String line:Files.readAllLines(Path.of("docs/reference/nether_portal_destinations.tsv"))){if(line.startsWith("#")||line.isBlank())continue;var f=line.split(" ");int mode=Integer.parseInt(f[2]);
            if(f[0].equals("C")){long seed=Long.parseLong(f[3]);var plan=create(creation(mode),.25,70.75,-.25,new Random(seed));if(plan==null||!hash(writes(plan)).equals(f[4]))throw new AssertionError("Original portal construction differs: "+line);creations++;}
            else {double[] p=incoming(mode);var fixture=lookup(mode);var landing=find(fixture,cells(fixture),p[0],p[1],p[2]);String text=landing==null?"none":landing.x()+","+landing.y()+","+landing.z();if(!text.equals(f[3]))throw new AssertionError("Original portal lookup differs: "+line);lookups++;}
        }
        if(creations!=96||lookups!=20)throw new AssertionError("Incomplete portal destination reference");
        System.out.println("HISTORICAL_PORTAL_DESTINATIONS SUCCESS: 96 ordered original creation plans and 20 original radius/tie/landing/absence searches");
    }
}
