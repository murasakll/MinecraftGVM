package br.com.mura.betaminecraftmod.world;

final class HalloweenPopulationSettingsChecks {
    static void run(){
        var ores=HalloweenPopulationSettings.ORES;
        if(ores.size()!=8||ores.stream().anyMatch(o->o.block().contains("lapis")))throw new AssertionError("Future lapis in Alpha population");
        int attempts=ores.stream().mapToInt(HalloweenPopulationSettings.Ore::attempts).sum();
        if(attempts!=91||!ores.getLast().equals(new HalloweenPopulationSettings.Ore("diamond_ore",1,7,16)))throw new AssertionError("Reference mineral budget mismatch");
        if(HalloweenPopulationSettings.trees(HalloweenClimate.Biome.FOREST,2,false)!=7
            ||HalloweenPopulationSettings.trees(HalloweenClimate.Biome.SEASONAL_FOREST,2,true)!=5
            ||HalloweenPopulationSettings.trees(HalloweenClimate.Biome.DESERT,50,true)!=0)throw new AssertionError("Tree climate policy mismatch");
        if(HalloweenPopulationSettings.snow(.5,64)||!HalloweenPopulationSettings.snow(.49,64)
            ||!HalloweenPopulationSettings.snow(.6,100)||HalloweenPopulationSettings.snow(0,0)||HalloweenPopulationSettings.snow(0,128))throw new AssertionError("Snow height/temperature boundary mismatch");
        for(long seed:new long[]{0,123456789,-987654321,Long.MIN_VALUE,Long.MAX_VALUE}){
            var a=HalloweenPopulationSettings.stream(seed,-97,127,0);var b=HalloweenPopulationSettings.stream(seed,-97,127,0);
            for(int i=0;i<32;i++)if(a.nextLong()!=b.nextLong())throw new AssertionError("Unstable feature stream");
            if(HalloweenPopulationSettings.stream(seed,96,0,0).nextLong()==HalloweenPopulationSettings.stream(seed,96,0,1).nextLong())throw new AssertionError("Collapsed feature channels");
        }
        System.out.println("HALLOWEEN_POPULATION_SETTINGS SUCCESS: reference mineral budget, climate counts, snow boundaries, signed seeds");
    }
}
