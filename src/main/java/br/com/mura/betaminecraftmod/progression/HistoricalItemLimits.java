package br.com.mura.betaminecraftmod.progression;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Per-stack historical limits. Never mutates the global item registry. */
public final class HistoricalItemLimits {
    private static final String MARKER="betaminecraftmod:item_limits_v1";
    private static final Map<Integer,Map<String,Limits>> CACHE=new ConcurrentHashMap<>();
    public record Limits(int stack,int damage) { }
    private HistoricalItemLimits() { }

    public static Limits limits(int era,String item) {
        return era>=0&&era<=6?CACHE.computeIfAbsent(era,HistoricalItemLimits::load).get(item):null;
    }

    /** Preserve the fraction of wear, rounded up, so advancing cannot repair a used tool. */
    public static int translateDamage(int damage,int oldMax,int newMax) {
        if(damage<=0||oldMax<=0||newMax<=0)return 0;
        return (int)Math.min(newMax,((long)damage*newMax+oldMax-1)/oldMax);
    }

    public static void apply(ItemStack stack,int era) {
        if(stack.isEmpty())return;
        var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        var saved=data.getCompound(MARKER).orElse(null);
        var limits=era>=0&&era<=6?limits(era,BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()):null;
        if(saved==null&&limits==null)return;
        if(saved!=null&&saved.getIntOr("era",-99)==era&&limits!=null)return;
        if(saved==null) {
            saved=new CompoundTag();
            saved.putInt("stack",stack.getMaxStackSize());
            saved.putInt("max_damage",stack.getOrDefault(DataComponents.MAX_DAMAGE,0));
            saved.putBoolean("had_damage",stack.has(DataComponents.DAMAGE));
            saved.putInt("hidden_damage",stack.getDamageValue());
        }
        int previousMax=stack.getOrDefault(DataComponents.MAX_DAMAGE,0);
        int previousDamage=previousMax>0?stack.getDamageValue():saved.getIntOr("hidden_damage",0);
        int previousBasis=previousMax>0?previousMax:saved.getIntOr("max_damage",0);
        int newMax=limits==null?saved.getIntOr("max_damage",0):limits.damage();
        int newStack=limits==null?saved.getIntOr("stack",64):limits.stack();
        // Remove DAMAGE before allowing a formerly damageable item to stack.
        if(newMax==0) {
            stack.remove(DataComponents.DAMAGE);stack.remove(DataComponents.MAX_DAMAGE);
            saved.putInt("hidden_damage",previousDamage);
        } else {
            stack.set(DataComponents.MAX_STACK_SIZE,1);
            stack.set(DataComponents.MAX_DAMAGE,newMax);
            stack.set(DataComponents.DAMAGE,translateDamage(previousDamage,previousBasis,newMax));
        }
        stack.set(DataComponents.MAX_STACK_SIZE,newMax>0?1:newStack);
        if(limits==null) {
            if(newMax==0&&saved.getBooleanOr("had_damage",false))stack.set(DataComponents.DAMAGE,previousDamage);
            data.remove(MARKER);
        } else {
            saved.putInt("era",era);data.put(MARKER,saved);
        }
        CustomData.set(DataComponents.CUSTOM_DATA,stack,data);
    }

    private static Map<String,Limits> load(int era) {
        String path="/data/betaminecraftmod/historical_items/"+(era==6?"release_1_0":"era_"+era)+".json";
        try(var stream=HistoricalItemLimits.class.getResourceAsStream(path)) {
            if(stream==null)throw new IllegalStateException("Missing item limits "+path);
            var json=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
            var rows=new HashMap<String,Limits>();
            for(var element:json.getAsJsonArray("items")) {
                var row=element.getAsJsonObject();int count=row.get("max_stack").getAsInt(),damage=row.get("max_damage").getAsInt();
                if(count<1||count>99||damage<0||damage>0&&count!=1)throw new IllegalStateException("Invalid historical item limits");
                if(rows.put(row.get("item").getAsString(),new Limits(count,damage))!=null)throw new IllegalStateException("Duplicate historical item");
            }
            return Map.copyOf(rows);
        }catch(IOException error){throw new IllegalStateException("Could not load "+path,error);}
    }
}
