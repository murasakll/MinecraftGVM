package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.ReleaseBrewingCatalog;
import java.nio.file.*;
import java.util.*;

final class ReleaseBrewingCatalogChecks {
    static void run()throws Exception{
        for(String file:List.of("ingredients.tsv","potions.tsv","transitions.tsv","timing.tsv"))if(!Arrays.equals(Files.readAllBytes(Path.of("docs/reference/brewing_1_0_"+file)),Files.readAllBytes(Path.of("src/main/resources/data/betaminecraftmod/historical_brewing/"+file))))throw new AssertionError("Runtime brewing differs from original extraction");
        String expected=Files.readAllLines(Path.of("docs/reference/brewing_1_0_gui.tsv")).stream().filter(line->!line.startsWith("#")).findFirst().orElseThrow().split(" ")[0];
        String actual=HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(Path.of("src/main/resources/assets/betaminecraftmod/textures/gui/brewing_1_0.png"))));if(!actual.equals(expected))throw new AssertionError("Original brewing GUI pixels changed");
        if(ReleaseBrewingCatalog.BREW_TICKS!=600||ReleaseBrewingCatalog.result(0,"minecraft:nether_wart")!=16||ReleaseBrewingCatalog.result(0,"minecraft:fermented_spider_eye")!=8200)throw new AssertionError("Original start timing/base paths");
        if(ReleaseBrewingCatalog.result(16,"minecraft:golden_carrot")!=16||ReleaseBrewingCatalog.result(16,"minecraft:rabbit_foot")!=16||ReleaseBrewingCatalog.result(16,"minecraft:phantom_membrane")!=16)throw new AssertionError("Later potion recipes leaked");
        if(ReleaseBrewingCatalog.potion(8257).effects().getFirst().duration()!=2400||ReleaseBrewingCatalog.potion(8260).effects().getFirst().duration()!=2400||ReleaseBrewingCatalog.potion(8228).effects().getFirst().duration()!=450||ReleaseBrewingCatalog.potion(16385).effects().getFirst().duration()!=676)throw new AssertionError("Modern durations replaced original long/strong/splash variants");
        var found=new HashSet<Integer>();var queue=new ArrayDeque<Integer>();found.add(0);queue.add(0);int edges=0;
        while(!queue.isEmpty()){int value=queue.remove();for(String ingredient:ReleaseBrewingCatalog.ingredients()){int result=ReleaseBrewingCatalog.result(value,ingredient);if(value!=result){edges++;if(found.add(result))queue.add(result);}}}
        if(found.size()!=69||edges!=156)throw new AssertionError("Historical chemistry is no longer reachable from water");
        System.out.println("RELEASE_BREWING_CATALOG SUCCESS: original 600-tick graph, 69 reachable states/156 transformations, old durations and no later reagents");
    }
}
