package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.*;
import java.nio.file.*;
import java.util.*;

final class ReleaseEnchantingChecks {
    static void run()throws Exception {
        for(String file:List.of("items.tsv","levels.tsv","compat.tsv"))if(!Arrays.equals(Files.readAllBytes(Path.of("docs/reference/enchanting_1_0_"+file)),Files.readAllBytes(Path.of("src/main/resources/data/betaminecraftmod/historical_enchanting/"+file))))throw new AssertionError("Enchanting runtime/reference differs");
        int costs=0,selections=0;
        for(String line:Files.readAllLines(Path.of("docs/reference/enchanting_costs_1_0.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;var f=line.split(" ");var random=new Random(Long.parseLong(f[4]));var item=ReleaseEnchantingCatalog.item(f[0]);
            int cost=ReleaseEnchantingRules.cost(random,Integer.parseInt(f[3]),Integer.parseInt(f[2]),item.enchantability());
            if(item.enchantability()!=Integer.parseInt(f[1])||cost!=Integer.parseInt(f[5])||random.nextLong()!=Long.parseLong(f[6]))throw new AssertionError("Original enchanting cost/RNG differs: "+line);costs++;
        }
        for(String line:Files.readAllLines(Path.of("docs/reference/enchanting_selection_1_0.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;var f=line.split(" ");var random=new Random(Long.parseLong(f[2]));
            var result=ReleaseEnchantingRules.select(random,f[0],Integer.parseInt(f[1]));String actual=result.isEmpty()?"-":String.join(",",result.stream().map(entry->entry.id()+":"+entry.level()).toList());
            if(!actual.equals(f[3])||random.nextLong()!=Long.parseLong(f[4]))throw new AssertionError("Original enchanting selection/RNG differs: "+line+" actual="+actual);selections++;
        }
        if(costs!=3168||selections!=1120)throw new AssertionError("Incomplete enchanting fixtures");
        if(ReleaseEnchantingCatalog.item("minecraft:bow").enchantability()!=0||ReleaseEnchantingCatalog.item("minecraft:book").accepts())throw new AssertionError("Later enchanting eligibility leaked");
        var witness=Files.readAllLines(Path.of("docs/reference/enchanting_offer_50_1_0.tsv")).stream().filter(line->!line.startsWith("#")&&!line.isBlank()).findFirst().orElseThrow().split(" ");
        var roll=new Random(Long.parseLong(witness[0]));roll.nextLong();for(int row=0;row<3;row++)if(ReleaseEnchantingRules.cost(roll,row,30,10)!=Integer.parseInt(witness[row+1]))throw new AssertionError("Original level-50 offer differs");if(roll.nextLong()!=Long.parseLong(witness[4]))throw new AssertionError("Offer witness RNG differs");
        if(!Arrays.equals(Files.readAllBytes(Path.of("docs/reference/enchanting_words_1_0.txt")),Files.readAllBytes(Path.of("src/main/resources/assets/betaminecraftmod/enchanting_words_1_0.txt"))))throw new AssertionError("Enchanting words differ");
        for(String line:Files.readAllLines(Path.of("docs/reference/enchanting_labels_1_0.tsv"))){if(line.startsWith("#")||line.isBlank())continue;var f=line.split("\\|");var random=new Random(Long.parseLong(f[0]));for(int row=0;row<3;row++)if(!ReleaseEnchantingNames.next(random).equals(f[row+1]))throw new AssertionError("Original labels differ");if(random.nextLong()!=Long.parseLong(f[4]))throw new AssertionError("Label RNG differs");}
        for(String line:Files.readAllLines(Path.of("docs/reference/enchanting_assets_1_0.tsv"))){if(line.startsWith("#")||line.isBlank())continue;var f=line.split(" ");var file=Path.of("src/main/resources/"+f[1]);String hash=HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));var image=javax.imageio.ImageIO.read(file.toFile());if(!hash.equals(f[2])||image.getWidth()!=Integer.parseInt(f[3])||image.getHeight()!=Integer.parseInt(f[4]))throw new AssertionError("Original enchanting pixels changed: "+f[0]);}
        var font=com.google.gson.JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/betaminecraftmod/font/enchanting_1_0.json"))).getAsJsonObject().getAsJsonArray("providers").get(0).getAsJsonObject();
        if(font.get("height").getAsInt()!=8||font.get("ascent").getAsInt()!=7||font.getAsJsonArray("chars").size()!=16||font.getAsJsonArray("chars").get(6).getAsString().charAt(1)!='a')throw new AssertionError("Original glyph mapping differs");
        var mixins=com.google.gson.JsonParser.parseString(Files.readString(Path.of("src/main/resources/betaminecraftmod.mixins.json"))).getAsJsonObject();boolean common=false,clientOnly=false;
        for(var entry:mixins.getAsJsonArray("mixins"))if(entry.getAsString().equals("SlotPositionAccessor"))common=true;
        for(var entry:mixins.getAsJsonArray("client"))if(entry.getAsString().equals("SlotPositionAccessor"))clientOnly=true;
        if(!common||clientOnly)throw new AssertionError("Server menu depends on a client-only slot accessor");
        System.out.println("RELEASE_ENCHANTING SUCCESS: 3168 original costs and 1120 selections, eligibility/compatibility and RNG consumption");
        System.out.println("RELEASE_ENCHANTING_VISUALS SUCCESS: original level-50 witness, seven label sequences, unchanged GUI/book/font and glyph mapping");
    }
}
