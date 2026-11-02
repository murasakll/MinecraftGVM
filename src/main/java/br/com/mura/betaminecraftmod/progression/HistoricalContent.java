package br.com.mura.betaminecraftmod.progression;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Fail-closed content inventories through the release 1.0 reference. */
public final class HistoricalContent {
    private static final Map<Integer,Set<String>> CACHE=new ConcurrentHashMap<>();
    private HistoricalContent() { }
    public static boolean supported(int era){return era>=0&&era<=6;}
    public static boolean active(net.minecraft.world.level.Level level){return supported(CampaignRules.era(level));}
    public static boolean allowed(ItemStack stack,int era) {
        if(stack.isEmpty())return true;
        if(era<=2)return InfdevContent.allowed(stack,era);
        return supported(era)&&items(era).contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }
    public static Set<String> items(int era) {
        if(era<3||era>6)throw new IllegalArgumentException("No later historical content inventory "+era);
        return CACHE.computeIfAbsent(era,HistoricalContent::load);
    }
    private static Set<String> load(int era) {
        String reference=era==6?"release_1_0":"era_"+era;
        var result=new HashSet<String>();
        var items=read("historical_items/"+reference+".json");
        for(var item:items.getAsJsonArray("items"))result.add(item.getAsJsonObject().get("item").getAsString());
        // Crafting tables also supply block-item aliases omitted by the legacy item-limit mapper.
        var crafting=read("historical_crafting/"+reference+".json");
        for(var element:crafting.getAsJsonArray("recipes")) {
            var row=element.getAsJsonObject();result.add(row.get("result").getAsString());
            for(var ingredient:row.getAsJsonArray("ingredients")) {
                if(ingredient.isJsonNull())continue;
                if(ingredient.isJsonArray())for(var variant:ingredient.getAsJsonArray())result.add(variant.getAsString());
                else result.add(ingredient.getAsString());
            }
        }
        for(String block:List.of("air","grass_block","dirt","bedrock","gravel","coal_ore","iron_ore","gold_ore","diamond_ore","redstone_ore","mossy_cobblestone","obsidian","oak_sapling","oak_leaves","spawner","netherrack","soul_sand","glowstone","carved_pumpkin","jack_o_lantern","snow","ice","clay","sugar_cane","cactus"))result.add("minecraft:"+block);
        if(era>=4)for(String block:List.of("lapis_ore","lapis_lazuli","birch_log","birch_leaves","birch_sapling","spruce_log","spruce_leaves","spruce_sapling","short_grass","fern","dead_bush"))result.add("minecraft:"+block);
        if(era>=5)for(String block:List.of("vine","melon","melon_slice","infested_stone","infested_cobblestone","infested_stone_bricks","cobweb","brown_mushroom_block","red_mushroom_block","mushroom_stem"))result.add("minecraft:"+block);
        if(era>=6)for(String block:List.of("end_stone","nether_wart","mycelium","nether_bricks","nether_brick_fence","nether_brick_stairs","lily_pad","end_portal_frame","dragon_egg","splash_potion"))result.add("minecraft:"+block);
        return Set.copyOf(result);
    }
    private static JsonObject read(String path) {
        try(var stream=HistoricalContent.class.getResourceAsStream("/data/betaminecraftmod/"+path)) {
            if(stream==null)throw new IllegalStateException("Missing historical content source "+path);
            return JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
        }catch(IOException error){throw new IllegalStateException(path,error);}
    }
}
