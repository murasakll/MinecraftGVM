package br.com.mura.betaminecraftmod.progression;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Reference recipe facts translated to native shaped recipes, isolated to campaign lookups. */
public final class HistoricalCrafting {
    private static final Map<Integer,List<RecipeHolder<CraftingRecipe>>> CACHE=new ConcurrentHashMap<>();
    private HistoricalCrafting(){}
    public static List<RecipeHolder<CraftingRecipe>> recipes(int era){
        if(era<0||era>6)throw new IllegalArgumentException("Crafting reference pending for era "+era);
        return CACHE.computeIfAbsent(era,HistoricalCrafting::load);
    }
    public static Optional<RecipeHolder<CraftingRecipe>> find(int era,CraftingInput input,Level level){
        if(input.isEmpty()||input.width()>3||input.height()>3)return Optional.empty();
        if(era==6&&ReleaseOneRepair.HOLDER.value().matches(input,level))return Optional.of(ReleaseOneRepair.HOLDER);
        for(var recipe:recipes(era))if(recipe.value().matches(input,level))return Optional.of(recipe);
        return Optional.empty();
    }
    private static List<RecipeHolder<CraftingRecipe>> load(int era){
        // Index 6 is the 1.0 reference, not the entire saved chapter 1.0–1.2.
        String path="/data/betaminecraftmod/historical_crafting/"+(era==6?"release_1_0":"era_"+era)+".json";
        try(var stream=HistoricalCrafting.class.getResourceAsStream(path)){
            if(stream==null)throw new IllegalStateException("Missing historical recipes "+path);
            var json=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
            var result=new ArrayList<RecipeHolder<CraftingRecipe>>();
            for(var element:json.getAsJsonArray("recipes")){
                var row=element.getAsJsonObject();int width=row.get("width").getAsInt(),height=row.get("height").getAsInt();
                var cells=row.getAsJsonArray("ingredients");
                if(row.has("kind")&&row.get("kind").getAsString().equals("shapeless")){
                    if(cells.isEmpty()||cells.size()>9)throw new IllegalStateException("Invalid historical shapeless recipe");
                    var ingredients=new ArrayList<Ingredient>();for(var cell:cells)ingredients.add(ingredient(cell));
                    var recipe=new ShapelessRecipe(new Recipe.CommonInfo(false),new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC,""),
                        new ItemStackTemplate(item(row.get("result").getAsString()),row.get("count").getAsInt()),ingredients){
                        @Override public boolean isSpecial(){return true;}
                        @Override public ItemStack assemble(CraftingInput input){var stack=super.assemble(input);HistoricalItemLimits.apply(stack,era);return stack;}
                    };
                    result.add(holder(era,row,recipe));continue;
                }
                if(width<1||height<1||width>3||height>3||cells.size()!=width*height)throw new IllegalStateException("Invalid historical recipe grid");
                var symbols=new LinkedHashMap<String,Character>();var key=new HashMap<Character,Ingredient>();var pattern=new ArrayList<String>();
                for(int y=0;y<height;y++){
                    var line=new StringBuilder();
                    for(int x=0;x<width;x++){
                        var cell=cells.get(y*width+x);
                        if(cell.isJsonNull()){line.append(' ');continue;}
                        String name=cell.toString();Character symbol=symbols.get(name);
                        if(symbol==null){symbol=(char)('A'+symbols.size());symbols.put(name,symbol);key.put(symbol,ingredient(cell));}
                        line.append(symbol);
                    }
                    pattern.add(line.toString());
                }
                var recipe=new ShapedRecipe(new Recipe.CommonInfo(false),new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC,""),
                    ShapedRecipePattern.of(key,pattern),new ItemStackTemplate(item(row.get("result").getAsString()),row.get("count").getAsInt())){
                    // These isolated reference recipes are not entries in the modern recipe book.
                    @Override public boolean isSpecial(){return true;}
                    @Override public ItemStack assemble(CraftingInput input){
                        var stack=super.assemble(input);HistoricalItemLimits.apply(stack,era);return stack;
                    }
                };
                result.add(holder(era,row,recipe));
            }
            return List.copyOf(result);
        }catch(IOException ex){throw new IllegalStateException("Could not load historical crafting "+era,ex);}
    }
    private static RecipeHolder<CraftingRecipe> holder(int era,JsonObject row,CraftingRecipe recipe){
        return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE,Identifier.fromNamespaceAndPath("betaminecraftmod","historical/"+era+"/"+row.get("id").getAsString())),recipe);
    }
    private static Ingredient ingredient(JsonElement cell){
        if(!cell.isJsonArray())return Ingredient.of(item(cell.getAsString()));
        var items=new ArrayList<Item>();for(var variant:cell.getAsJsonArray())items.add(item(variant.getAsString()));
        if(items.isEmpty())throw new IllegalStateException("Empty ingredient alternatives");
        return Ingredient.of(items.toArray(Item[]::new));
    }
    private static Item item(String name){return BuiltInRegistries.ITEM.getOrThrow(ResourceKey.create(Registries.ITEM,Identifier.parse(name))).value();}
}
