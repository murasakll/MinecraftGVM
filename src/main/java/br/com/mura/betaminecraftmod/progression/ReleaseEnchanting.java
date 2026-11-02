package br.com.mura.betaminecraftmod.progression;

import java.lang.ref.WeakReference;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ReleaseEnchanting {
    private static final Map<Container,WeakReference<Player>> OWNERS=Collections.synchronizedMap(new WeakHashMap<>());
    private ReleaseEnchanting(){}
    public static void register(Container container,Player owner){OWNERS.put(container,new WeakReference<>(owner));}
    public static boolean historical(Container container){var reference=OWNERS.get(container);var owner=reference==null?null:reference.get();return owner!=null&&ReleaseBrewing.active(owner.level());}
    public static String key(ItemStack stack){return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();}
    public static boolean accepts(ItemStack stack){var info=ReleaseEnchantingCatalog.item(key(stack));return !stack.isEmpty()&&info!=null&&info.accepts()&&!stack.isEnchanted();}
    public static int books(net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos) {
        int count=0;
        for(int z=-1;z<=1;z++)for(int x=-1;x<=1;x++) {
            if((x==0&&z==0)||!level.isEmptyBlock(pos.offset(x,0,z))||!level.isEmptyBlock(pos.offset(x,1,z)))continue;
            for(int y=0;y<=1;y++) {
                if(level.getBlockState(pos.offset(x*2,y,z*2)).is(net.minecraft.world.level.block.Blocks.BOOKSHELF))count++;
                if(x!=0&&z!=0) {
                    if(level.getBlockState(pos.offset(x*2,y,z)).is(net.minecraft.world.level.block.Blocks.BOOKSHELF))count++;
                    if(level.getBlockState(pos.offset(x,y,z*2)).is(net.minecraft.world.level.block.Blocks.BOOKSHELF))count++;
                }
            }
        }
        return count;
    }
}
