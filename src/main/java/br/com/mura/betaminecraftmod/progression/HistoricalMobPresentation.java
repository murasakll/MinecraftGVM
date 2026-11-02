package br.com.mura.betaminecraftmod.progression;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.cow.*;
import net.minecraft.world.entity.animal.chicken.*;
import net.minecraft.world.entity.animal.pig.*;
import net.minecraft.world.entity.animal.wolf.*;
import net.minecraft.world.item.*;

/** Remove later natural-spawn equipment/variants without changing the selected mob's identity. */
public final class HistoricalMobPresentation {
    public static void normalize(Entity entity,int era) {
        if(era<3||era>6)return;
        var registry=entity.registryAccess();
        if(entity instanceof Cow)entity.setComponent(DataComponents.COW_VARIANT,registry.getOrThrow(CowVariants.TEMPERATE));
        if(entity instanceof Chicken)entity.setComponent(DataComponents.CHICKEN_VARIANT,registry.getOrThrow(ChickenVariants.TEMPERATE));
        if(entity instanceof Pig)entity.setComponent(DataComponents.PIG_VARIANT,registry.getOrThrow(PigVariants.TEMPERATE));
        if(entity instanceof Wolf)entity.setComponent(DataComponents.WOLF_VARIANT,registry.getOrThrow(WolfVariants.PALE));
        if(era<6&&entity instanceof AgeableMob animal)animal.setBaby(false);
        if(era==3&&entity instanceof net.minecraft.world.entity.animal.sheep.Sheep sheep)sheep.setColor(DyeColor.WHITE);
        if(entity instanceof Mob mob) {
            for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET,EquipmentSlot.OFFHAND})mob.setItemSlot(slot,ItemStack.EMPTY);
            if(entity instanceof net.minecraft.world.entity.monster.zombie.Zombie&&!(entity instanceof net.minecraft.world.entity.monster.zombie.ZombifiedPiglin))mob.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
            if(entity instanceof net.minecraft.world.entity.monster.skeleton.Skeleton)mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));
            mob.removeAllEffects();
        }
    }
    private HistoricalMobPresentation() { }
}
