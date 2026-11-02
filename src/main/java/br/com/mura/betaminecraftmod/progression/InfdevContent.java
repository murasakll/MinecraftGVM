package br.com.mura.betaminecraftmod.progression;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Explicit era catalogue. Anything not listed remains unavailable in the opening phase. */
public final class InfdevContent {
    private static final Set<String> LATE_ITEMS = Set.of("rail", "minecart", "saddle", "oak_stairs", "cobblestone_stairs", "spawner");
    private static final Set<String> ALPHA_ITEMS = Set.of("redstone", "redstone_ore", "redstone_torch", "lever", "stone_button", "stone_pressure_plate", "oak_pressure_plate", "iron_door", "oak_boat", "chest_minecart", "furnace_minecart", "snowball", "snow", "snow_block", "ice", "cactus", "clay", "clay_ball", "brick", "sugar_cane", "paper", "book", "oak_fence", "jukebox", "music_disc_13", "music_disc_cat", "compass", "fishing_rod", "leather", "milk_bucket", "egg", "slime_ball");
    private static final Set<String> ITEMS = new HashSet<>(Set.of(
        "air", "stone", "grass_block", "dirt", "cobblestone", "oak_planks", "oak_sapling", "bedrock",
        "sand", "gravel", "gold_ore", "iron_ore", "coal_ore", "oak_log", "oak_leaves", "sponge", "glass",
        "dandelion", "poppy", "brown_mushroom", "red_mushroom", "gold_block", "iron_block", "diamond_block",
        "smooth_stone_slab", "stone_slab", "bricks", "tnt", "bookshelf", "mossy_cobblestone", "obsidian",
        "torch", "fire", "chest", "diamond_ore", "crafting_table", "wheat", "farmland", "furnace",
        "oak_sign", "oak_door", "ladder", "stick", "bowl", "mushroom_stew", "bow", "arrow", "flint_and_steel",
        "apple", "golden_apple", "coal", "diamond", "iron_ingot", "gold_ingot", "string", "feather", "gunpowder",
        "wheat_seeds", "bread", "flint", "porkchop", "cooked_porkchop", "painting", "bucket", "water_bucket", "lava_bucket"));
    static {
        for (String material : new String[]{"wooden", "stone", "iron", "diamond", "golden"})
            for (String tool : new String[]{"sword", "shovel", "pickaxe", "axe", "hoe"}) ITEMS.add(material + "_" + tool);
        for (String material : new String[]{"leather", "chainmail", "iron", "diamond", "golden"})
            for (String part : new String[]{"helmet", "chestplate", "leggings", "boots"}) ITEMS.add(material + "_" + part);
        for (String color : new String[]{"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"}) ITEMS.add(color + "_wool");
    }
    public static boolean allowed(ItemStack stack) { return allowed(stack, 0); }
    public static boolean allowed(ItemStack stack, int era) {
        if (stack.isEmpty()) return true;
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("minecraft") && (ITEMS.contains(id.getPath()) || era >= 1 && LATE_ITEMS.contains(id.getPath()) || era == 2 && ALPHA_ITEMS.contains(id.getPath()));
    }
    public static boolean legacy(net.minecraft.world.level.Level level) {
        return level instanceof net.minecraft.server.level.ServerLevel server && CampaignRules.isCampaign(server)
            && CampaignProgress.get(server).era() >= 0 && CampaignProgress.get(server).era() <= 2;
    }
    public static boolean recipeAllowed(net.minecraft.world.item.crafting.Recipe recipe, net.minecraft.world.item.crafting.RecipeInput input, int era) {
        for (int i = 0; i < input.size(); i++) if (!allowed(input.getItem(i), era)) return false;
        var result = recipe.assemble(input);
        // Saddles existed as dungeon loot, not as a craftable item.
        return !result.is(net.minecraft.world.item.Items.SADDLE) && allowed(result, era);
    }
}
