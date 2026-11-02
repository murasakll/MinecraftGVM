package br.com.mura.betaminecraftmod.progression;

import java.util.*;

/** Independently implemented offer/selection arithmetic, checked against the original helper. */
public final class ReleaseEnchantingRules {
    private ReleaseEnchantingRules(){}
    public static int cost(Random random,int row,int books,int enchantability) {
        if(enchantability<=0)return 0;
        books=Math.min(30,books);
        int power=1+random.nextInt((books>>1)+1)+random.nextInt(books+1);
        int offered=random.nextInt(5)+power;
        return row==0?(offered>>1)+1:row==1?offered*2/3+1:offered;
    }
    public static List<ReleaseEnchantingCatalog.Entry> select(Random random,String item,int cost) {
        var info=ReleaseEnchantingCatalog.item(item);
        if(info==null||info.enchantability()<=0)return List.of();
        int value=info.enchantability();
        int adjusted=1+random.nextInt((value>>1)+1)+random.nextInt((value>>1)+1)+cost;
        float variation=(random.nextFloat()+random.nextFloat()-1F)*.25F;
        int power=(int)(adjusted*(1F+variation)+.5F);
        var candidates=new HashMap<Integer,ReleaseEnchantingCatalog.Entry>();
        for(var entry:ReleaseEnchantingCatalog.levels())if(info.allowed().contains(entry.id())&&power>=entry.min()&&power<=entry.max())candidates.put(entry.id(),entry);
        if(candidates.isEmpty())return List.of();
        var selected=new ArrayList<ReleaseEnchantingCatalog.Entry>();selected.add(pick(random,candidates.values()));
        for(int remaining=power>>1;random.nextInt(50)<=remaining;remaining>>=1) {
            candidates.keySet().removeIf(id->selected.stream().anyMatch(entry->!ReleaseEnchantingCatalog.compatible(entry.id(),id)));
            if(!candidates.isEmpty())selected.add(pick(random,candidates.values()));
        }
        return List.copyOf(selected);
    }
    private static ReleaseEnchantingCatalog.Entry pick(Random random,Collection<ReleaseEnchantingCatalog.Entry> entries) {
        int weight=entries.stream().mapToInt(ReleaseEnchantingCatalog.Entry::weight).sum();
        int value=random.nextInt(weight);
        for(var entry:entries){value-=entry.weight();if(value<0)return entry;}
        throw new IllegalStateException("Invalid enchantment weights");
    }
}
