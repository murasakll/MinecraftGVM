package br.com.mura.betaminecraftmod.progression;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Finite graph extracted from the original 1.0 stand, including inert intermediate metadata. */
public final class ReleaseBrewingCatalog {
    public record Effect(int id,int duration,int amplifier){}
    public record Potion(int metadata,int color,String prefix,List<Effect> effects){}
    private static final Map<Integer,Potion> POTIONS=new TreeMap<>();
    private static final Map<String,Integer> TRANSITIONS=new HashMap<>();
    private static final Set<String> INGREDIENTS=new HashSet<>();
    public static final int BREW_TICKS;
    static {
        for(String line:rows("ingredients.tsv")){var f=line.split(" ");INGREDIENTS.add(f[1]);}
        for(String line:rows("potions.tsv")){
            var f=line.split(" ");var effects=new ArrayList<Effect>();if(!f[3].equals("-"))for(String value:f[3].split(",")){var e=value.split(":");effects.add(new Effect(Integer.parseInt(e[0]),Integer.parseInt(e[1]),Integer.parseInt(e[2])));}
            int id=Integer.parseInt(f[0]);if(POTIONS.put(id,new Potion(id,Integer.parseInt(f[1]),f[2],List.copyOf(effects)))!=null)throw new IllegalStateException("Duplicate historical potion");
        }
        for(String line:rows("transitions.tsv")){var f=line.split(" ");int source=Integer.parseInt(f[0]),target=Integer.parseInt(f[2]);if(!POTIONS.containsKey(source)||!POTIONS.containsKey(target)||!INGREDIENTS.contains(f[1])||TRANSITIONS.put(source+"/"+f[1],target)!=null)throw new IllegalStateException("Invalid historical brewing edge");}
        BREW_TICKS=Integer.parseInt(rows("timing.tsv").getFirst().split(" ")[1]);
        if(BREW_TICKS!=600||POTIONS.size()!=69||TRANSITIONS.size()!=156||INGREDIENTS.size()!=11)throw new IllegalStateException("Incomplete release brewing reference");
    }
    private ReleaseBrewingCatalog(){}
    public static Potion potion(int metadata){return POTIONS.get(metadata);}
    public static Collection<Potion> potions(){return List.copyOf(POTIONS.values());}
    public static Set<String> ingredients(){return Set.copyOf(INGREDIENTS);}
    public static int result(int metadata,String ingredient){return TRANSITIONS.getOrDefault(metadata+"/"+ingredient,metadata);}
    private static List<String> rows(String name){
        try(var stream=ReleaseBrewingCatalog.class.getResourceAsStream("/data/betaminecraftmod/historical_brewing/"+name)){
            if(stream==null)throw new IllegalStateException("Missing original brewing table "+name);
            try(var reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))){return reader.lines().filter(line->!line.isBlank()&&!line.startsWith("#")).toList();}
        }catch(IOException e){throw new IllegalStateException("Cannot read original brewing table",e);}
    }
}
