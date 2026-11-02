package br.com.mura.betaminecraftmod.progression;

import java.util.*;

/** Reference pools/weights; modern collision, caps and AI remain separate systems. */
public final class HistoricalSpawnTables {
    public record Entry(String entity,int weight,int min,int max) { }
    public static List<Entry> pool(int era,String dimension,String biome,String category) {
        if(era<3||era>6)return List.of();
        if(dimension.equals("minecraft:the_end"))return era==6&&category.equals("monster")?List.of(entry("enderman",10)):List.of();
        if(dimension.equals("minecraft:the_nether")) {
            if(!category.equals("monster"))return List.of();
            return era<6?List.of(entry("ghast",era==3?10:50),entry("zombified_piglin",era==3?10:100)):
                List.of(entry("ghast",50),entry("zombified_piglin",100),entry("magma_cube",1));
        }
        if(!dimension.equals("minecraft:overworld"))return List.of();
        boolean mushroom=biome.contains("mushroom");
        if(mushroom)return era==6&&category.equals("creature")?List.of(new Entry("mooshroom",8,4,8)):List.of();
        var list=new ArrayList<Entry>();
        if(category.equals("monster")) {
            for(String mob:List.of("spider","zombie","skeleton","creeper"))list.add(entry(mob,10));
            if(era>=4)list.add(entry("slime",10));
            if(era>=5)list.add(new Entry("enderman",1,1,4));
        }else if(category.equals("creature")) {
            if(era>=5&&biome.contains("desert"))return List.of();
            list.add(entry("sheep",era==3?10:12));list.add(entry("pig",10));list.add(entry("chicken",10));list.add(entry("cow",era==3?10:8));
            if(era>=4&&(biome.contains("forest")||biome.contains("taiga")))list.add(entry("wolf",era==4?2:biome.contains("taiga")?8:5));
        }else if(category.equals("water_creature")&&era>=4)list.add(entry("squid",10));
        return List.copyOf(list);
    }
    private static Entry entry(String entity,int weight){return new Entry(entity,weight,4,4);}
    private HistoricalSpawnTables() { }
}
