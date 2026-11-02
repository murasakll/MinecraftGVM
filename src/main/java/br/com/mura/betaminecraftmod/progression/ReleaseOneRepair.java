package br.com.mura.betaminecraftmod.progression;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** The 1.0 manager's special repair branch was not part of its tabulated recipe list. */
final class ReleaseOneRepair {
    static final RecipeHolder<CraftingRecipe> HOLDER=new RecipeHolder<>(ResourceKey.create(Registries.RECIPE,
        Identifier.fromNamespaceAndPath("betaminecraftmod","historical/release_1_0/repair")),new RepairItemRecipe(){
            @Override public boolean matches(CraftingInput input,Level level){return !repair(input).isEmpty();}
            @Override public ItemStack assemble(CraftingInput input){return repair(input);}
        });
    private ReleaseOneRepair() { }
    private static ItemStack repair(CraftingInput input) {
        if(input.ingredientCount()!=2)return ItemStack.EMPTY;
        ItemStack first=null,second=null;
        for(var stack:input.items())if(!stack.isEmpty()){if(first==null)first=stack;else second=stack;}
        if(first==null||second==null||first.getCount()!=1||second.getCount()!=1||!first.is(second.getItem()))return ItemStack.EMPTY;
        var limits=HistoricalItemLimits.limits(6,BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        if(limits==null||limits.damage()<=0)return ItemStack.EMPTY;
        int reference=limits.damage()-1;
        int remaining=(reference-first.getDamageValue())+(reference-second.getDamageValue())+reference*10/100;
        var result=new ItemStack(first.getItem());result.set(DataComponents.MAX_DAMAGE,reference+1);result.set(DataComponents.MAX_STACK_SIZE,1);
        result.set(DataComponents.DAMAGE,Math.max(0,reference-remaining));return result;
    }
}
