package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.*;
import java.nio.file.*;

final class HistoricalConstructionChecks {
    static void run()throws Exception {
        if(!HistoricalConstruction.cubeChest(4)||HistoricalConstruction.cubeChest(5)||!HistoricalConstruction.chestHitbox(6)||HistoricalConstruction.chestHitbox(7))throw new AssertionError("Chest model and hitbox epochs collapsed");
        if(!HistoricalConstruction.bottomStairs(6,"1.0")||!HistoricalConstruction.bottomStairs(6,"1.1")||HistoricalConstruction.bottomStairs(6,"1.2.5")||!HistoricalConstruction.straightStairs(6)||HistoricalConstruction.straightStairs(7))throw new AssertionError("Stair milestone changed");
        int shapes=0;
        for(var line:Files.readAllLines(Path.of("docs/reference/stair_shapes.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;var f=line.split(" ");
            if(HistoricalStairGeometry.occupancy(Integer.parseInt(f[1]))!=Integer.parseInt(f[2]))throw new AssertionError("Original collision changed");shapes++;
        }
        if(shapes!=12)throw new AssertionError("Missing original stair shapes");
        int chests=0;
        for(var line:Files.readAllLines(Path.of("docs/reference/chest_bounds.tsv"))){if(line.isBlank()||line.startsWith("#"))continue;var f=line.split(" ");for(int i=1;i<=6;i++)if(Double.parseDouble(f[i])!=(i<=3?0:1))throw new AssertionError("Original chest bounds changed");chests++;}
        if(chests!=4)throw new AssertionError("Missing original chest physics references");
        int faces=0;
        for(var line:Files.readAllLines(Path.of("docs/reference/chest_pixels.tsv"))) {
            if(line.isBlank()||line.startsWith("#"))continue;var f=line.split(" ");
            var image=javax.imageio.ImageIO.read(Path.of("src/main/resources/resourcepacks/"+f[0]+"/assets/minecraft/textures/block/infdev_chest_"+f[1]+".png").toFile());
            var digest=java.security.MessageDigest.getInstance("SHA-256");if(image.getWidth()!=16||image.getHeight()!=16)throw new AssertionError("Resampled chest sprite");
            for(int pixel:image.getRGB(0,0,16,16,null,0,16))digest.update(java.nio.ByteBuffer.allocate(4).putInt(pixel).array());
            if(!java.util.HexFormat.of().formatHex(digest.digest()).equals(f[3]))throw new AssertionError("Historical chest pixels changed");faces++;
        }
        if(faces!=14)throw new AssertionError("Missing chest sprite references");
        System.out.println("HISTORICAL_CONSTRUCTION SUCCESS: twelve original stair unions, model/physics milestones and fourteen exact chest faces");
    }
}
