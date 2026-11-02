package br.com.mura.betaminecraftmod.progression;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ReleaseEnchantingCatalog {
    public record Item(int enchantability,boolean accepts,Set<Integer> allowed){}
    public record Entry(int id,String key,int level,int weight,int min,int max){}
    private static final Map<String,Item> ITEMS=new HashMap<>();
    private static final List<Entry> LEVELS=new ArrayList<>();
    private static final Set<String> COMPATIBLE=new HashSet<>();
    static {
        for(String line:rows("items.tsv")){var f=line.split(" ");var allowed=new HashSet<Integer>();if(!f[3].equals("-"))for(String id:f[3].split(","))allowed.add(Integer.parseInt(id));ITEMS.put(f[0],new Item(Integer.parseInt(f[1]),Boolean.parseBoolean(f[2]),Set.copyOf(allowed)));}
        for(String line:rows("levels.tsv")){var f=line.split(" ");LEVELS.add(new Entry(Integer.parseInt(f[0]),f[1],Integer.parseInt(f[2]),Integer.parseInt(f[3]),Integer.parseInt(f[4]),Integer.parseInt(f[5])));}
        for(String line:rows("compat.tsv")){var f=line.split(" ");if(Boolean.parseBoolean(f[2]))COMPATIBLE.add(f[0]+"/"+f[1]);}
        if(ITEMS.size()!=242||LEVELS.stream().map(Entry::id).distinct().count()!=17)throw new IllegalStateException("Incomplete enchanting reference");
    }
    private ReleaseEnchantingCatalog(){}
    public static Item item(String name){return ITEMS.get(name);}
    public static List<Entry> levels(){return List.copyOf(LEVELS);}
    public static boolean compatible(int first,int second){return COMPATIBLE.contains(first+"/"+second);}
    private static List<String> rows(String name){
        try(var stream=ReleaseEnchantingCatalog.class.getResourceAsStream("/data/betaminecraftmod/historical_enchanting/"+name)) {
            if(stream==null)throw new IllegalStateException("Missing enchanting table "+name);
            try(var reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))){return reader.lines().filter(line->!line.isBlank()&&!line.startsWith("#")).toList();}
        }catch(IOException e){throw new IllegalStateException(e);}
    }
}
