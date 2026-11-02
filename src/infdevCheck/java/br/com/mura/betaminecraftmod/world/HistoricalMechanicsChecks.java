package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.*;

final class HistoricalMechanicsChecks {
    static void run(){
        if(HistoricalMechanics.all().size()!=20||HistoricalMechanics.get(-1)!=null||HistoricalMechanics.get(20)!=null)throw new AssertionError("Mechanics profile boundary");
        for(int era=0;era<20;era++){
            var p=HistoricalMechanics.get(era);
            if(p.era()!=era||p.instantBow()!=(era<5)||p.historicalFurnace()!=(era<7))throw new AssertionError("Mechanics era mismatch "+era);
        }
        for(int era=0;era<7;era++){
            if(InfdevFurnaceRules.fuelTicks("minecraft:coal",era)!=1600||InfdevFurnaceRules.fuelTicks("minecraft:oak_door",era)!=0)throw new AssertionError("Old material/id fuel gate");
            if((InfdevFurnaceRules.result("minecraft:clay_ball",era)!=null)!=(era>=2))throw new AssertionError("Brick introduced too early");
            if((InfdevFurnaceRules.result("minecraft:cod",era)!=null)!=(era>=3))throw new AssertionError("Fish introduced too early");
            if((InfdevFurnaceRules.result("minecraft:oak_log",era)!=null)!=(era>=4))throw new AssertionError("Charcoal introduced too early");
            if(InfdevFurnaceRules.fuelTicks("minecraft:lava_bucket",era)!=(era>=2?20000:0))throw new AssertionError("Lava fuel gate");
            if(InfdevFurnaceRules.fuelTicks("minecraft:oak_sapling",era)!=(era>=4?100:0))throw new AssertionError("Sapling fuel gate");
            if((InfdevFurnaceRules.result("minecraft:redstone_ore",era)!=null)!=(era>=6)||InfdevFurnaceRules.result("minecraft:stone",era)!=null)throw new AssertionError("Smelting recipe boundary mismatch");
            if((InfdevFurnaceRules.result("minecraft:beef",era)!=null)!=(era>=5))throw new AssertionError("Beef before Adventure Update");
            if((InfdevFurnaceRules.result("minecraft:coal_ore",era)!=null)!=(era>=6)||(InfdevFurnaceRules.result("minecraft:lapis_ore",era)!=null)!=(era>=6))throw new AssertionError("Release 1.0 ore smelting boundary");
            if(InfdevFurnaceRules.fuelTicks("minecraft:blaze_rod",era)!=(era>=6?2400:0))throw new AssertionError("Blaze fuel boundary");
        }
        if(InfdevFurnaceRules.result("minecraft:iron_ore",7)!=null||InfdevFurnaceRules.fuelTicks("minecraft:coal",7)!=0)throw new AssertionError("Historical furnace leaked into native fallback");
        System.out.println("HISTORICAL_MECHANICS SUCCESS: 20 profiles, instant/charged bow boundary, seven furnace stages through 1.0, later fallback isolation");
    }
}
