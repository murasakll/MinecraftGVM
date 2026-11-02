package br.com.mura.betaminecraftmod.world;

import java.nio.file.*;
import java.util.*;

final class HistoricalSpriteChecks {
    static void run()throws Exception {
        var counts=new HashMap<String,Integer>();var targets=new HashSet<String>();
        for(var line:Files.readAllLines(Path.of("docs/reference/visual_sprites.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;var f=line.split(" ");
            if(!targets.add(f[0]+"/"+f[1])||Integer.parseInt(f[3])<0||Integer.parseInt(f[3])>=256)throw new AssertionError("Ambiguous historical sprite target");
            var path=Path.of("src/main/resources/resourcepacks/"+f[0]+"/assets/minecraft/textures/"+f[1]+".png");var image=javax.imageio.ImageIO.read(path.toFile());
            if(image.getWidth()!=16||image.getHeight()!=16)throw new AssertionError("Resampled historical sprite");
            var digest=java.security.MessageDigest.getInstance("SHA-256");for(int pixel:image.getRGB(0,0,16,16,null,0,16))digest.update(java.nio.ByteBuffer.allocate(4).putInt(pixel).array());
            if(!HexFormat.of().formatHex(digest.digest()).equals(f[7]))throw new AssertionError("Original sprite pixels changed "+path);
            counts.merge(f[0],1,Integer::sum);
            if(f[1].startsWith("item/")) {
                var root=Path.of("src/main/resources/resourcepacks/"+f[0]+"/assets/minecraft");String name=f[1].substring(5);
                var definition=com.google.gson.JsonParser.parseString(Files.readString(root.resolve("items/"+name+".json"))).getAsJsonObject().getAsJsonObject("model");
                if(definition.has("tints")||!definition.get("model").getAsString().equals("minecraft:item/"+name))throw new AssertionError("Modern tint or model leaked into an old static icon");
            }
        }
        if(!counts.equals(Map.of("visual_halloween",175,"visual_beta",232,"visual_adventure",248,"visual_release_1_0",266)))throw new AssertionError("Historical sprite manifest incomplete "+counts);
        for(var pack:counts.keySet())try(var files=Files.walk(Path.of("src/main/resources/resourcepacks/"+pack+"/assets/minecraft/textures"))){if(files.filter(p->p.toString().endsWith(".png")).count()!=counts.get(pack))throw new AssertionError("Untracked stale sprite in "+pack);}
        System.out.println("HISTORICAL_SPRITES SUCCESS: 921 exact original sprites, four isolated packs, static icon models and no stale outputs");
    }
}
