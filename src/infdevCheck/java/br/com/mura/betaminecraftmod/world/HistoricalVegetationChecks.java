package br.com.mura.betaminecraftmod.world;

import java.nio.file.*;

final class HistoricalVegetationChecks {
    static void run()throws Exception {
        int checked=0;
        for(String row:Files.readAllLines(Path.of("docs/reference/vegetation_pixels.tsv"))) {
            if(row.startsWith("#")||row.isBlank())continue;var f=row.split(" ");
            Path path=Path.of("src/main/resources/assets/betaminecraftmod/textures/colormap/"+switch(f[0]){case "3"->"halloween";case "4"->"beta";case "5"->"adventure";default->"release_1_0";}+"_"+f[1]+".png");
            String hash=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
            var image=javax.imageio.ImageIO.read(path.toFile());
            if(!hash.equals(f[2])||image.getWidth()!=256||image.getHeight()!=256)throw new AssertionError("Historical colormap changed "+path);
            checked++;
        }
        if(checked!=8||HistoricalVegetationPalette.index(0,0)!=65535||HistoricalVegetationPalette.index(1,1)!=0||HistoricalVegetationPalette.index(1,0)!=65280)throw new AssertionError("Historical colormap orientation");
        var generators=new java.util.HashMap<String,AdventureBiomes>();int chunks=0;
        for(String row:Files.readAllLines(Path.of("docs/reference/layered_climate.tsv"))) {
            if(row.startsWith("#")||row.isBlank())continue;var f=row.split(" ");int era=Integer.parseInt(f[0]),x=Integer.parseInt(f[2]),z=Integer.parseInt(f[3]);
            var generator=generators.computeIfAbsent(f[0]+"/"+f[1],ignored->new AdventureBiomes(Long.parseLong(f[1]),era==6));var climate=generator.climate(x,z,16,16);
            int[][] grids={generator.coarse(x,z,16,16),generator.blocks(x,z,16,16),climate.temperature(),climate.rainfall()};
            for(int mode=0;mode<4;mode++){var bytes=java.nio.ByteBuffer.allocate(1024);for(int value:grids[mode])bytes.putInt(value);String hash=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes.array()));if(!hash.equals(f[4+mode]))throw new AssertionError("Historical climate graph changed: "+row+" layer="+mode);}chunks++;
        }
        if(chunks!=48)throw new AssertionError("Missing layered climate fixtures");
        System.out.println("HISTORICAL_VEGETATION SUCCESS: eight original colormaps and 49152 original biome/climate integers");
    }
}
