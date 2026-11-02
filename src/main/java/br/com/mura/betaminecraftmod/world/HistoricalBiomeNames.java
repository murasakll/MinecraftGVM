package br.com.mura.betaminecraftmod.world;

/** Explicit aliases for biome IDs removed from modern registries. Does not alter terrain sampling. */
public final class HistoricalBiomeNames {
    private HistoricalBiomeNames() {}
    public static String current(String name,int era) {
        return "minecraft:"+switch(name) {
            case "mountains", "mountain_edge" -> "windswept_hills";
            case "gravelly_mountains", "modified_gravelly_mountains" -> "windswept_gravelly_hills";
            case "wooded_mountains" -> "windswept_forest";
            case "snowy_tundra", "snowy_mountains" -> "snowy_plains";
            case "mushroom_field_shore" -> "mushroom_fields";
            case "desert_hills", "desert_lakes" -> "desert";
            case "wooded_hills" -> "forest";
            case "taiga" -> era<=6 ? "snowy_taiga" : "taiga";
            case "taiga_hills", "taiga_mountains" -> era<=6 ? "snowy_taiga" : "taiga";
            case "jungle_hills", "modified_jungle" -> "jungle";
            case "jungle_edge", "modified_jungle_edge" -> "sparse_jungle";
            case "stone_shore" -> "stony_shore";
            case "birch_forest_hills" -> "birch_forest";
            case "dark_forest_hills" -> "dark_forest";
            case "snowy_taiga_hills", "snowy_taiga_mountains" -> "snowy_taiga";
            case "giant_tree_taiga", "giant_tree_taiga_hills" -> "old_growth_pine_taiga";
            case "giant_spruce_taiga", "giant_spruce_taiga_hills" -> "old_growth_spruce_taiga";
            case "wooded_badlands_plateau", "modified_wooded_badlands_plateau" -> "wooded_badlands";
            case "badlands_plateau", "modified_badlands_plateau" -> "badlands";
            case "deep_warm_ocean" -> "warm_ocean";
            case "swamp_hills" -> "swamp";
            case "tall_birch_forest", "tall_birch_hills" -> "old_growth_birch_forest";
            case "shattered_savanna", "shattered_savanna_plateau" -> "windswept_savanna";
            case "bamboo_jungle_hills" -> "bamboo_jungle";
            default -> name;
        };
    }
}
