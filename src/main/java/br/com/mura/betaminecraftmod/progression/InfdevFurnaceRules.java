package br.com.mura.betaminecraftmod.progression;

import java.util.Map;
import java.util.Set;

/** June 17 policy plus audited pre-Adventure deltas. Registry-free historical rules. */
public final class InfdevFurnaceRules {
    public static final int COOK_TICKS=200;
    private static final Map<String,String> RESULTS=Map.of(
        "minecraft:iron_ore","minecraft:iron_ingot",
        "minecraft:gold_ore","minecraft:gold_ingot",
        "minecraft:diamond_ore","minecraft:diamond",
        "minecraft:sand","minecraft:glass",
        "minecraft:porkchop","minecraft:cooked_porkchop",
        "minecraft:cobblestone","minecraft:stone");
    // Only item IDs <256 whose original block material was wood qualified. Door/sign items
    // had IDs above 255; ladders used the decoration material, not wood.
    private static final Set<String> WOOD=Set.of("minecraft:oak_planks","minecraft:oak_log",
        "minecraft:bookshelf","minecraft:chest","minecraft:crafting_table");
    private InfdevFurnaceRules(){}
    public static String result(String input){return RESULTS.get(input);}
    public static String result(String input,int era){
        var profile=HistoricalMechanics.get(era);
        if(profile==null||!profile.historicalFurnace())return null;
        String base=result(input);if(base!=null)return base;
        if(era>=2&&input.equals("minecraft:clay_ball"))return "minecraft:brick";
        if(era>=3&&input.equals("minecraft:cod"))return "minecraft:cooked_cod";
        if(era>=4){
            if(input.equals("minecraft:cactus"))return "minecraft:green_dye";
            if(input.equals("minecraft:oak_log")||input.equals("minecraft:birch_log")||input.equals("minecraft:spruce_log"))return "minecraft:charcoal";
        }
        if(era>=5){
            if(input.equals("minecraft:beef"))return "minecraft:cooked_beef";
            if(input.equals("minecraft:chicken"))return "minecraft:cooked_chicken";
        }
        if(era>=6){
            if(input.equals("minecraft:coal_ore"))return "minecraft:coal";
            if(input.equals("minecraft:redstone_ore"))return "minecraft:redstone";
            if(input.equals("minecraft:lapis_ore"))return "minecraft:lapis_lazuli";
        }
        return null;
    }
    public static int fuelTicks(String input){
        if(WOOD.contains(input))return 300;
        return switch(input){case "minecraft:stick"->100;case "minecraft:coal"->1600;default->0;};
    }
    public static int fuelTicks(String input,int era){
        var profile=HistoricalMechanics.get(era);
        if(profile==null||!profile.historicalFurnace())return 0;
        int base=fuelTicks(input);if(base>0)return base;
        if(era>=1&&input.equals("minecraft:oak_stairs"))return 300;
        if(era>=2){
            if(input.equals("minecraft:lava_bucket"))return 20000;
            if(java.util.Set.of("minecraft:oak_fence","minecraft:jukebox").contains(input))return 300;
        }
        if(era>=4){
            if(input.equals("minecraft:charcoal"))return 1600;
            if(java.util.Set.of("minecraft:oak_sapling","minecraft:birch_sapling","minecraft:spruce_sapling").contains(input))return 100;
            if(java.util.Set.of("minecraft:birch_log","minecraft:spruce_log","minecraft:note_block","minecraft:oak_trapdoor","minecraft:oak_pressure_plate").contains(input))return 300;
        }
        if(era>=5&&input.equals("minecraft:oak_fence_gate"))return 300;
        if(era>=6&&input.equals("minecraft:blaze_rod"))return 2400;
        return 0;
    }
}
