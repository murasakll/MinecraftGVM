package br.com.mura.betaminecraftmod.world;

/** Admission for modern compatibility bases. Does not claim original historical climate placement. */
public final class ModernEraBiomePolicy {
    private ModernEraBiomePolicy(){}
    public static String replacement(int era,String name){
        if(era<13||era>19)throw new IllegalArgumentException("Not a modern era");
        String path=name.startsWith("minecraft:")?name.substring(10):name;
        if(era<19&&path.equals("dappled_forest"))return "minecraft:forest";
        if(era<19&&path.equals("sulfur_caves"))return "minecraft:dripstone_caves";
        if(era<17&&path.equals("pale_garden"))return "minecraft:dark_forest";
        if(era<15&&path.equals("cherry_grove"))return "minecraft:meadow";
        if(era<14&&path.equals("mangrove_swamp"))return "minecraft:swamp";
        if(era<14&&path.equals("deep_dark"))return "minecraft:dripstone_caves";
        return name.contains(":")?name:"minecraft:"+name;
    }
}
