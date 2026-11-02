package br.com.mura.betaminecraftmod.progression;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

/** Converts historical metadata into per-stack native effects, without changing the potion registry. */
public final class ReleaseBrewing {
    private static final String MARKER="betaminecraftmod:potion_1_0";
    private static final Map<Holder<Potion>,Integer> NATIVE=new HashMap<>();
    static {
        NATIVE.put(Potions.WATER,0);NATIVE.put(Potions.AWKWARD,16);NATIVE.put(Potions.THICK,32);NATIVE.put(Potions.MUNDANE,64);
        bind(8193,Potions.REGENERATION,Potions.STRONG_REGENERATION,Potions.LONG_REGENERATION);
        bind(8194,Potions.SWIFTNESS,Potions.STRONG_SWIFTNESS,Potions.LONG_SWIFTNESS);
        bind(8195,Potions.FIRE_RESISTANCE,null,Potions.LONG_FIRE_RESISTANCE);
        bind(8196,Potions.POISON,Potions.STRONG_POISON,Potions.LONG_POISON);
        bind(8197,Potions.HEALING,Potions.STRONG_HEALING,null);
        bind(8200,Potions.WEAKNESS,null,Potions.LONG_WEAKNESS);
        bind(8201,Potions.STRENGTH,Potions.STRONG_STRENGTH,Potions.LONG_STRENGTH);
        bind(8202,Potions.SLOWNESS,null,Potions.LONG_SLOWNESS);
        bind(8204,Potions.HARMING,Potions.STRONG_HARMING,null);
    }
    private ReleaseBrewing(){}
    private static void bind(int id,Holder<Potion> base,Holder<Potion> strong,Holder<Potion> longer){NATIVE.put(base,id);if(strong!=null)NATIVE.put(strong,id+32);if(longer!=null)NATIVE.put(longer,id+64);}
    public static boolean active(Level level){return CampaignRules.era(level)==6&&CampaignRules.firstRelease(level).equals("1.0");}
    public static boolean clientActive(){return CampaignRules.clientEra()==6&&CampaignRules.firstRelease(net.minecraft.world.level.EmptyBlockGetter.INSTANCE).equals("1.0");}
    public static boolean ingredient(ItemStack stack){return !stack.isEmpty()&&ReleaseBrewingCatalog.ingredients().contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());}
    public static int metadata(ItemStack stack){
        if(!stack.is(Items.POTION)&&!stack.is(Items.SPLASH_POTION))return -1;
        int saved=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr(MARKER,-1);
        if(saved>=0){return ReleaseBrewingCatalog.potion(saved)!=null&&((saved&16384)!=0)==stack.is(Items.SPLASH_POTION)?saved:-1;}
        var contents=stack.get(DataComponents.POTION_CONTENTS);if(contents==null||!contents.customEffects().isEmpty()||contents.potion().isEmpty())return -1;
        int value=NATIVE.getOrDefault(contents.potion().get(),-1);if(value<0)return -1;
        if(stack.is(Items.SPLASH_POTION))value=(value&~8192)|16384;
        return ReleaseBrewingCatalog.potion(value)==null?-1:value;
    }
    public static int result(ItemStack potion,ItemStack reagent){int value=metadata(potion);return value<0?value:ReleaseBrewingCatalog.result(value,BuiltInRegistries.ITEM.getKey(reagent.getItem()).toString());}
    public static boolean canBrew(BrewingStandBlockEntity stand){
        if(!ingredient(stand.getItem(3)))return false;
        for(int slot=0;slot<3;slot++){var input=stand.getItem(slot);int before=metadata(input);if(before>=0&&result(input,stand.getItem(3))!=before)return true;}return false;
    }
    public static ItemStack create(ItemStack input,int metadata){
        var reference=ReleaseBrewingCatalog.potion(metadata);if(reference==null)throw new IllegalArgumentException("Unknown historical potion");
        var result=input.transmuteCopy((metadata&16384)!=0?Items.SPLASH_POTION:Items.POTION);
        var data=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();data.putInt(MARKER,metadata);CustomData.set(DataComponents.CUSTOM_DATA,result,data);
        var effects=new ArrayList<MobEffectInstance>();for(var e:reference.effects())effects.add(new MobEffectInstance(effect(e.id()),e.duration(),e.amplifier()));
        Optional<Holder<Potion>> base=effects.isEmpty()?Optional.of(metadata==0?Potions.WATER:(metadata&63)==16?Potions.AWKWARD:(metadata&63)==32?Potions.THICK:Potions.MUNDANE):Optional.empty();
        String name=effects.isEmpty()?metadata==0?"water":(metadata&63)==16?"awkward":(metadata&63)==32?"thick":"mundane":switch(reference.effects().getFirst().id()){case 1->"swiftness";case 2->"slowness";case 5->"strength";case 6->"healing";case 7->"harming";case 10->"regeneration";case 12->"fire_resistance";case 18->"weakness";case 19->"poison";default->throw new IllegalStateException("Unknown original effect");};
        result.set(DataComponents.POTION_CONTENTS,new PotionContents(base,Optional.of(reference.color()),List.copyOf(effects),Optional.of(name)));return result;
    }
    private static Holder<MobEffect> effect(int id){return switch(id){case 1->MobEffects.SPEED;case 2->MobEffects.SLOWNESS;case 5->MobEffects.STRENGTH;case 6->MobEffects.INSTANT_HEALTH;case 7->MobEffects.INSTANT_DAMAGE;case 10->MobEffects.REGENERATION;case 12->MobEffects.FIRE_RESISTANCE;case 18->MobEffects.WEAKNESS;case 19->MobEffects.POISON;default->throw new IllegalStateException("Unknown original potion effect "+id);};}
}
